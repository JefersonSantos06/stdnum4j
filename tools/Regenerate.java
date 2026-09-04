import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Regenerates every data file in the repository, from its own source, in one
 * command.
 *
 * <pre>
 *   javac -d tools/classes tools/*.java
 *   java -cp tools/classes Regenerate            # all of them
 *   java -cp tools/classes Regenerate isbn oui   # just these
 *   java -cp tools/classes Regenerate --check    # reproduce and compare, write nothing
 * </pre>
 *
 * <p>There is no other way to produce a data file, and that is the point.
 * Every {@link Source} declares what it needs and writes what it makes;
 * fetching, the character set, the line endings and the comparison happen
 * here, once, for all of them. Producing one file by hand and producing all
 * sixteen therefore cannot drift apart, because they are the same code — and
 * a source takes no arguments, so there is nothing to get wrong when you ask
 * for one.</p>
 *
 * <p>The sources are found, not listed: every class on the classpath that
 * implements {@code Source} is one. Adding a data file is adding a class.</p>
 *
 * <p>Each target is independent. A source that will not answer, or one that
 * refuses because its upstream changed shape, is reported and the rest carry
 * on; the exit code stays 0 unless {@code --strict} is given, because one
 * dead registry must not stop the other fifteen. Every fetch is bounded
 * twice, by the request's own timeout and by an outer one, so nothing can
 * hang the run.</p>
 *
 * <p><b>The JDK matters.</b> The postal codes take their country names from
 * the JDK's CLDR copy, and those change between releases: 17 says Turkey
 * where 25 says Türkiye. Regenerate on the version the file was made with, or
 * a hundred and eighty names silently regress. {@code tools/README.md} names
 * it.</p>
 */
public final class Regenerate {

    private Regenerate() {
    }

    /**
     * Identifying the tool honestly is not politeness — Wikipedia answers 403
     * to the JDK's default user agent, which would take four sources down.
     */
    private static final String USER_AGENT =
            "stdnum-regenerate/1.0 (https://github.com/jefersonsantos06/stdnum4j)";

    private enum Status { UNCHANGED, CHANGED, FAILED }

    private record Result(Source source, Status status, String detail, List<String> warnings,
                          byte[] payload) {
    }

    private static HttpClient http;
    private static Semaphore permits;

    public static void main(String[] args) throws Exception {
        List<String> ids = new ArrayList<>();
        boolean check = false;
        boolean strict = false;
        boolean keep = false;
        Path outDir = null;
        Path reportDir = null;
        Path offline = null;
        int jobs = 4;
        long deadlineMinutes = 30;
        LocalDate date = LocalDate.now(ZoneOffset.UTC);

        List<Source> found = discover();

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--list" -> {
                    found.forEach(s -> System.out.printf("%-16s %-28s %s%n",
                            s.id(), s.getClass().getSimpleName(), s.output()));
                    return;
                }
                case "--check" -> check = true;
                case "--strict" -> strict = true;
                case "--keep" -> keep = true;
                case "--out-dir" -> outDir = Path.of(args[++i]);
                case "--report" -> reportDir = Path.of(args[++i]);
                case "--offline" -> offline = Path.of(args[++i]);
                case "--jobs" -> jobs = Integer.parseInt(args[++i]);
                case "--deadline" -> deadlineMinutes = Long.parseLong(args[++i]);
                case "--date" -> date = LocalDate.parse(args[++i]);
                default -> {
                    if (args[i].startsWith("-")) {
                        System.err.println("unknown flag: " + args[i]);
                        System.exit(2);
                    }
                    ids.add(args[i]);
                }
            }
        }

        List<Source> chosen = found.stream()
                .filter(s -> ids.isEmpty() || ids.contains(s.id()))
                .toList();
        List<String> unknown = ids.stream()
                .filter(id -> found.stream().noneMatch(s -> s.id().equals(id)))
                .toList();
        if (!unknown.isEmpty()) {
            System.err.println("unknown source(s): " + unknown + "; known: "
                    + found.stream().map(Source::id).collect(Collectors.joining(" ")));
            System.exit(2);
        }

        permits = new Semaphore(jobs);
        http = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(20))
                // NORMAL, not ALWAYS: ALWAYS would follow https down to http
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();

        Path scratch = offline != null ? offline : Files.createTempDirectory("stdnum-regen-");
        Files.createDirectories(scratch);

        // one thread, so that a source which hangs is abandoned rather than
        // allowed to hold the run: it cannot be killed, but it can be left
        ExecutorService worker = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "generate");
            t.setDaemon(true);
            return t;
        });

        long deadline = System.nanoTime() + Duration.ofMinutes(deadlineMinutes).toNanos();
        List<Result> results = new ArrayList<>();
        for (Source source : chosen) {
            if (System.nanoTime() > deadline) {
                results.add(new Result(source, Status.FAILED, "the run ran out of time",
                        List.of(), null));
                continue;
            }
            results.add(run(source, scratch.resolve(source.id()), offline != null, date, worker));
        }

        for (Result r : results) {
            if (r.status() == Status.CHANGED && !check) {
                Path to = outDir != null ? outDir.resolve(r.source().id() + ".dat")
                        : Path.of(r.source().output());
                Files.createDirectories(to.toAbsolutePath().getParent());
                Files.write(to, r.payload());
            }
        }
        if (reportDir != null) {
            report(reportDir, results);
        }
        print(results, check);

        if (keep || offline != null) {
            System.err.println("scratch kept at " + scratch);
        } else {
            deleteTree(scratch);
        }

        boolean changed = results.stream().anyMatch(r -> r.status() == Status.CHANGED);
        boolean failed = results.stream().anyMatch(r -> r.status() == Status.FAILED);
        // an explicit exit: the HttpClient of 17 has no close() and its threads linger
        System.exit(check && (changed || failed) ? 1 : strict && failed ? 1 : 0);
    }

    /** Every class on the classpath that is a source. No list to keep. */
    private static List<Source> discover() throws Exception {
        List<Source> sources = new ArrayList<>();
        for (String entry : System.getProperty("java.class.path").split(java.io.File.pathSeparator)) {
            Path dir = Path.of(entry);
            if (!Files.isDirectory(dir)) {
                continue;
            }
            try (var files = Files.list(dir)) {
                for (Path file : files.sorted().toList()) {
                    String name = file.getFileName().toString();
                    if (!name.endsWith(".class") || name.contains("$")) {
                        continue;
                    }
                    Class<?> type = Class.forName(name.substring(0, name.length() - 6));
                    if (Source.class.isAssignableFrom(type) && !type.isInterface()) {
                        sources.add((Source) type.getDeclaredConstructor().newInstance());
                    }
                }
            }
        }
        sources.sort(Comparator.comparing(Source::id));
        return sources;
    }

    private static Result run(Source source, Path work, boolean offline, LocalDate date,
                              ExecutorService worker) {
        List<String> warnings = new ArrayList<>();
        try {
            Files.createDirectories(work);
            Map<String, String> seeds = new LinkedHashMap<>();
            for (Source.Download seed : source.seeds()) {
                seeds.put(seed.file(), new String(obtain(source, seed, work, offline),
                        StandardCharsets.UTF_8));
            }
            List<Source.Download> downloads = source.downloads(seeds);
            for (Source.Download download : downloads) {
                if (!seeds.containsKey(download.file())) {
                    obtain(source, download, work, offline);
                }
            }

            List<Path> files = downloads.stream().map(d -> work.resolve(d.file())).toList();
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            Source.Run run = new Run(files, date, warnings);
            // buffered, and adopted only when generate returns: a source that
            // refuses halfway leaves the committed file alone
            try (PrintStream out = new PrintStream(buffer, false, StandardCharsets.UTF_8)) {
                Future<?> task = worker.submit(() -> {
                    try {
                        source.generate(run, out);
                        return null;
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
                try {
                    task.get(5, TimeUnit.MINUTES);
                } catch (TimeoutException e) {
                    task.cancel(true);
                    return new Result(source, Status.FAILED,
                            "did not finish in 5 minutes", warnings, null);
                } catch (Exception e) {
                    Throwable cause = e.getCause() != null ? e.getCause() : e;
                    if (cause.getCause() != null && cause instanceof RuntimeException) {
                        cause = cause.getCause();
                    }
                    return new Result(source, Status.FAILED,
                            cause.getClass().getSimpleName() + ": " + cause.getMessage(),
                            warnings, null);
                }
            }
            byte[] payload = normalise(buffer.toByteArray());
            if (payload.length == 0) {
                return new Result(source, Status.FAILED, "produced nothing", warnings, null);
            }
            return compare(source, payload, warnings);
        } catch (Exception e) {
            return new Result(source, Status.FAILED,
                    e.getClass().getSimpleName() + ": " + e.getMessage(), warnings, null);
        }
    }

    /** What a source is given while it runs. */
    private record Run(List<Path> files, LocalDate retrievedOn, List<String> warnings)
            implements Source.Run {

        @Override
        public Path file(String name) {
            return files.stream()
                    .filter(p -> p.getFileName().toString().equals(name))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("no download named " + name));
        }

        @Override
        public void warn(String message) {
            warnings.add(message);
        }
    }

    /** LF everywhere, so a run on Windows and a run on Linux agree. */
    private static byte[] normalise(byte[] payload) {
        return new String(payload, StandardCharsets.UTF_8).replace("\r\n", "\n")
                .getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Whether the file moved, ignoring the header lines that differ on every
     * download. A volatile line is masked <em>in place</em>, so that a source
     * which stops emitting one shifts the others and is reported as a change
     * rather than quietly masked away.
     */
    private static Result compare(Source source, byte[] fresh, List<String> warnings)
            throws IOException {
        Path committed = Path.of(source.output());
        if (!Files.exists(committed)) {
            return new Result(source, Status.CHANGED, "new file", warnings, fresh);
        }
        List<String> was = mask(source, new String(normalise(Files.readAllBytes(committed)),
                StandardCharsets.UTF_8));
        List<String> now = mask(source, new String(fresh, StandardCharsets.UTF_8));
        if (was.equals(now)) {
            return new Result(source, Status.UNCHANGED, "", warnings, null);
        }
        long added = now.stream().filter(l -> !was.contains(l)).count();
        long removed = was.stream().filter(l -> !now.contains(l)).count();
        return new Result(source, Status.CHANGED,
                was.size() + " -> " + now.size() + " lines (+" + added + " -" + removed + ")",
                warnings, fresh);
    }

    private static List<String> mask(Source source, String text) {
        List<Pattern> volatiles = source.volatileLines().stream().map(Pattern::compile).toList();
        List<String> lines = new ArrayList<>(List.of(text.split("\n", -1)));
        for (int i = 0; i < lines.size(); i++) {
            for (Pattern p : volatiles) {
                if (p.matcher(lines.get(i)).find()) {
                    lines.set(i, "# <volatile>");
                    break;
                }
            }
        }
        return lines;
    }

    // ------------------------------------------------------------------
    // fetching

    private static byte[] obtain(Source source, Source.Download download, Path work,
                                 boolean offline) throws Exception {
        Path to = work.resolve(download.file());
        if (offline) {
            if (!Files.exists(to)) {
                throw new IOException("offline, and " + to + " is not there");
            }
            return Files.readAllBytes(to);
        }
        byte[] body = fetch(URI.create(download.url()), source.timeoutSeconds());
        Files.write(to, body);
        return body;
    }

    /**
     * The body of a 200, or an exception. Bounded twice: the request carries
     * its own timeout, and the future is given a little longer still, so that
     * a server which accepts a connection and then says nothing cannot hold
     * the run open.
     */
    private static byte[] fetch(URI uri, int seconds) throws Exception {
        Exception last = null;
        for (int attempt = 1; attempt <= 3; attempt++) {
            permits.acquire();
            try {
                HttpRequest request = HttpRequest.newBuilder(uri)
                        .timeout(Duration.ofSeconds(seconds))
                        .header("User-Agent", USER_AGENT)
                        .GET()
                        .build();
                CompletableFuture<HttpResponse<byte[]>> future =
                        http.sendAsync(request, HttpResponse.BodyHandlers.ofByteArray())
                                .orTimeout(seconds + 15L, TimeUnit.SECONDS);
                HttpResponse<byte[]> response;
                try {
                    response = future.get();
                } catch (Exception e) {
                    future.cancel(true);
                    throw e;
                }
                int code = response.statusCode();
                if (code == 200) {
                    return response.body();
                }
                // a 4xx will say the same thing next time; only 429 is worth waiting out
                if (code < 500 && code != 429) {
                    throw new IOException(uri.getHost() + " answered " + code);
                }
                last = new IOException(uri.getHost() + " answered " + code);
            } catch (Exception e) {
                last = e;
            } finally {
                permits.release();
            }
            if (attempt < 3) {
                Thread.sleep(attempt == 1 ? 2000 : 8000);
            }
        }
        throw last;
    }

    // ------------------------------------------------------------------
    // reporting

    private static void report(Path dir, List<Result> results) throws IOException {
        Files.createDirectories(dir);
        StringBuilder summary = new StringBuilder();
        for (Result r : results) {
            summary.append(r.source().id()).append('\t')
                    .append(r.status().name().toLowerCase(Locale.ROOT)).append('\t')
                    .append(r.source().output()).append('\t')
                    // never empty: a shell reading this with a tab IFS collapses
                    // a run of separators and every later field shifts left
                    .append(r.detail().isBlank() ? "-" : r.detail().replace('\t', ' '))
                    .append('\t')
                    .append(r.source().title().replace('\t', ' ')).append('\n');
            if (!r.warnings().isEmpty()) {
                Files.writeString(dir.resolve(r.source().id() + ".err"),
                        String.join("\n", r.warnings()) + "\n");
            }
            if (r.payload() != null) {
                Files.write(dir.resolve(r.source().id() + ".dat"), r.payload());
            }
            if (r.status() == Status.CHANGED && !r.source().redFlags().isEmpty()) {
                Files.writeString(dir.resolve(r.source().id() + ".flags"),
                        String.join("\n", r.source().redFlags()) + "\n");
            }
        }
        Files.writeString(dir.resolve("summary.tsv"), summary.toString());
    }

    private static void print(List<Result> results, boolean check) {
        PrintStream out = new PrintStream(System.out, true, StandardCharsets.UTF_8);
        for (Result r : results) {
            out.printf("%-9s %-16s %s%n", r.status().name().toLowerCase(Locale.ROOT),
                    r.source().id(), r.detail());
            r.warnings().forEach(w -> out.println("          | " + w));
        }
        long changed = results.stream().filter(r -> r.status() == Status.CHANGED).count();
        long failed = results.stream().filter(r -> r.status() == Status.FAILED).count();
        out.printf("%n%d source(s): %d unchanged, %d changed, %d failed%s%n",
                results.size(), results.size() - changed - failed, changed, failed,
                check ? " (checked, nothing written)" : "");
    }

    private static void deleteTree(Path root) throws IOException {
        if (!Files.exists(root)) {
            return;
        }
        try (var walk = Files.walk(root)) {
            walk.sorted(Comparator.reverseOrder()).forEach(p -> {
                try {
                    Files.deleteIfExists(p);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        }
    }
}
