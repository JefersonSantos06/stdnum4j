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
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Regenerates every {@code .dat} file in the repository, from its own source,
 * in one command.
 *
 * <pre>
 *   javac -d tools/classes tools/*.java
 *   java -cp tools/classes Regenerate            # all of them
 *   java -cp tools/classes Regenerate isbn oui   # just these
 *   java -cp tools/classes Regenerate --check    # reproduce and compare, write nothing
 * </pre>
 *
 * <p>{@link #TARGETS} is the only place a data file is configured: its
 * sources, the generator that reads them, and where the result goes. Adding a
 * {@code .dat} is adding a row. Everything else in this class is mechanism.</p>
 *
 * <p>Each target is independent. A source that will not answer, or a
 * generator that refuses because its source changed shape, is reported and
 * the rest carry on — the exit code stays 0 unless {@code --strict} is given,
 * because one dead registry must not stop the other fifteen. Every fetch is
 * bounded twice, by the request's own timeout and by an outer one, so nothing
 * can hang the run.</p>
 *
 * <p><b>The JDK matters.</b> {@code GeneratePostalCodesDat} takes its country
 * names from the JDK's CLDR copy, and those change between releases: 17 says
 * Turkey where 25 says Türkiye. Regenerate on the version the file was made
 * with, or a hundred and eighty names silently regress. {@code tools/README.md}
 * names it.</p>
 */
public final class Regenerate {

    private Regenerate() {
    }

    /**
     * Identifying the tool honestly is not politeness — Wikipedia answers 403
     * to the JDK's default user agent, which would take four targets down.
     */
    private static final String USER_AGENT =
            "stdnum-regenerate/1.0 (https://github.com/jefersonsantos06/java-stdnum)";

    private static final String EU = "stdnum-eu/src/main/resources/io/github/jefersonsantos06/stdnum/eu/";
    private static final String INT =
            "stdnum-international/src/main/resources/io/github/jefersonsantos06/stdnum/international/";
    private static final String APAC =
            "stdnum-apac/src/main/resources/io/github/jefersonsantos06/stdnum/apac/";
    private static final String POSTAL =
            "stdnum-postal/src/main/resources/io/github/jefersonsantos06/stdnum/postal/";

    private static final String WIKI = "https://en.wikipedia.org/w/index.php?title=";
    private static final String NACE =
            "https://ec.europa.eu/eurostat/api/dissemination/sdmx/2.1/codelist/ESTAT/";
    private static final String ADDRESS = "https://chromium-i18n.appspot.com/ssl-address/data";
    private static final String BPS = "https://sig.bps.go.id/rest-bridging/getwilayah";

    // ------------------------------------------------------------------
    // the table

    /**
     * One file to download. {@code pass} says whether its path is handed to
     * the generator; the CFI landing page is fetched only to find the
     * spreadsheet linked from it.
     */
    record Fetch(String url, String file, boolean pass) {
        Fetch(String url, String file) {
            this(url, file, true);
        }
    }

    /** Turns the text of the roots into the rest of the downloads. */
    interface Expander {
        List<Fetch> expand(Map<String, String> roots);
    }

    /**
     * One data file: where it comes from, who reads it, where it goes.
     *
     * @param id        the name on the command line, the branch and the work directory
     * @param title     the commit subject when this file moves
     * @param generator the class to run, by name — never a type reference, so
     *                  that this file still compiles on its own
     * @param out       the destination, relative to the repository root
     * @param roots     downloaded first; <b>this order is the argument order</b>
     * @param expand    null, or what the roots say still has to be downloaded
     * @param prefixArgs arguments before the file paths (the retrieval date)
     * @param suffixArgs arguments after them (the NACE revision label)
     * @param timeout   seconds for one request; oui.csv alone needs minutes
     * @param volatile_ header lines that differ on every download and mean nothing
     * @param redFlags  what a human must revisit when this file changes
     */
    record Target(String id, String title, String generator, String out,
                  List<Fetch> roots, Expander expand,
                  List<String> prefixArgs, List<String> suffixArgs,
                  int timeout, List<Pattern> volatile_, List<String> redFlags) {

        Target(String id, String title, String generator, String out, List<Fetch> roots) {
            this(id, title, generator, out, roots, null,
                    List.of(), List.of(), 60, List.of(), List.of());
        }

        Target with(Expander expand) {
            return new Target(id, title, generator, out, roots, expand,
                    prefixArgs, suffixArgs, timeout, volatile_, redFlags);
        }

        Target args(List<String> prefix, List<String> suffix) {
            return new Target(id, title, generator, out, roots, expand,
                    prefix, suffix, timeout, volatile_, redFlags);
        }

        Target timeout(int seconds) {
            return new Target(id, title, generator, out, roots, expand,
                    prefixArgs, suffixArgs, seconds, volatile_, redFlags);
        }

        Target volatileLines(String... regexes) {
            List<Pattern> compiled = Arrays.stream(regexes).map(Pattern::compile).toList();
            return new Target(id, title, generator, out, roots, expand,
                    prefixArgs, suffixArgs, timeout, compiled, redFlags);
        }

        Target redFlags(String... files) {
            return new Target(id, title, generator, out, roots, expand,
                    prefixArgs, suffixArgs, timeout, volatile_, List.of(files));
        }
    }

    private static final String TODAY = LocalDate.now(ZoneOffset.UTC).toString();

    static final List<Target> TARGETS = List.of(
            new Target("at-fa", "Regenerate at-fa.dat, the Austrian tax offices",
                    "GenerateAtFaDat", EU + "at-fa.dat",
                    List.of(new Fetch(deWiki("Abgabenkontonummer"), "abgabenkontonummer.wiki"))),

            new Target("at-postleitzahl", "Regenerate at-postleitzahl.dat, the Austrian postcodes",
                    "GenerateAtPostleitzahlDat", EU + "at-postleitzahl.dat",
                    List.of(new Fetch("https://data.rtr.at/api/v1/tables/plz.json", "plz.json"))),

            new Target("be-banks", "Regenerate be-banks.dat, the Belgian bank codes",
                    "GenerateBeBanksDat", EU + "be-banks.dat",
                    List.of(new Fetch("https://www.nbb.be/doc/be/be/protocol/grouped_list_current.xlsx",
                            "grouped_list_current.xlsx"))),

            new Target("cfi", "Regenerate cfi.dat, the ISO 10962 classification",
                    "GenerateCfiDat", INT + "cfi.dat",
                    List.of(new Fetch("https://www.six-group.com/en/products-services/"
                            + "financial-information/data-standards.html", "six.html", false)))
                    .with(Regenerate::cfiSpreadsheet),

            new Target("cn-loc", "Regenerate cn-loc.dat, the Chinese administrative divisions",
                    "GenerateCnLocDat", APAC + "cn-loc.dat", cnRegions()),

            new Target("cz-banks", "Regenerate cz-banks.dat, the Czech payment system codes",
                    "GenerateCzBanksDat", EU + "cz-banks.dat",
                    List.of(new Fetch("https://www.cnb.cz/cs/platebni-styk/.galleries/"
                            + "ucty_kody_bank/download/kody_bank_CR.csv", "kody_bank_CR.csv"))),

            new Target("eu-nace20", "Regenerate eu-nace20.dat, NACE Rev. 2",
                    "GenerateEuNaceDat", EU + "eu-nace20.dat",
                    List.of(new Fetch(NACE + "NACE_R2", "nace20.xml")))
                    .args(List.of(), List.of("Rev. 2")),

            new Target("eu-nace21", "Regenerate eu-nace21.dat, NACE Rev. 2.1",
                    "GenerateEuNaceDat", EU + "eu-nace21.dat",
                    List.of(new Fetch(NACE + "NACE_R2_1", "nace21.xml")))
                    .args(List.of(), List.of("Rev. 2.1")),

            new Target("gs1-ai", "Regenerate gs1-ai.dat, the GS1 application identifiers",
                    "GenerateGs1AiDat", INT + "gs1-ai.dat",
                    List.of(new Fetch("https://ref.gs1.org/ai/", "ai.html"))),

            new Target("iban", "Regenerate iban.dat, the IBAN country registry",
                    "GenerateIbanDat", INT + "iban.dat",
                    List.of(new Fetch("https://en.wikipedia.org/wiki/International_Bank_Account_Number",
                            "iban.html"))),

            new Target("id-loc", "Regenerate id-loc.dat, the Indonesian regions",
                    "GenerateIdLocDat", APAC + "id-loc.dat",
                    List.of(new Fetch(BPS + "?level=provinsi", "provinsi.json")))
                    .with(Regenerate::indonesianRegencies),

            new Target("imsi", "Regenerate imsi.dat, the mobile country and network codes",
                    "GenerateImsiDat", INT + "imsi.dat", imsiPages()),

            new Target("isbn", "Regenerate isbn.dat, the ISBN registration groups",
                    "GenerateIsbnDat", INT + "isbn.dat",
                    List.of(new Fetch("https://www.isbn-international.org/export_rangemessage.xml",
                            "RangeMessage.xml")))
                    // the export is built per request: these two differ every time
                    .volatileLines("^# serial ", "^# date "),

            new Target("nz-banks", "Regenerate nz-banks.dat, the New Zealand bank branches",
                    "GenerateNzBanksDat", APAC + "nz-banks.dat",
                    List.of(new Fetch("https://www.paymentsnz.co.nz/resources/industry-registers/"
                            + "bank-branch-register/download/xlsx/", "BankBranchRegister.xlsx"))),

            new Target("oui", "Regenerate oui.dat, the IEEE MAC address blocks",
                    "GenerateOuiDat", INT + "oui.dat",
                    List.of(new Fetch("https://standards-oui.ieee.org/oui/oui.csv", "oui.csv"),
                            new Fetch("https://standards-oui.ieee.org/oui28/mam.csv", "mam.csv"),
                            new Fetch("https://standards-oui.ieee.org/oui36/oui36.csv", "oui36.csv")))
                    // 3.8 MB from a slow server: measured at about fifty seconds
                    .timeout(240),

            new Target("postal-codes", "Regenerate postal-codes.dat, the postal code shapes",
                    "GeneratePostalCodesDat", POSTAL + "postal-codes.dat",
                    List.of(new Fetch(ADDRESS, "data.json")))
                    .with(Regenerate::addressRegions)
                    .args(List.of(TODAY), List.of())
                    .volatileLines("^# .*, retrieved [0-9]{4}-[0-9]{2}-[0-9]{2}\\.$")
                    .redFlags("stdnum-all/src/test/java/io/github/jefersonsantos06/stdnum/all/"
                                    + "AllRegisteredContractTest.java (the registered total)",
                            "stdnum-postal/src/test/java/io/github/jefersonsantos06/stdnum/postal/"
                                    + "PostalCodeExamplesTest.java (the region count)",
                            "README.md, docs/NUMBERS.md, docs/ARCHITECTURE.md, "
                                    + "docs/CONTRIBUTING.md, docs/TESTING.md (the counts in prose)"));

    private static String deWiki(String title) {
        return "https://de.wikipedia.org/w/index.php?title=" + title + "&action=raw";
    }

    private static List<Fetch> imsiPages() {
        List<String> titles = List.of("Mobile_country_code",
                "Mobile_network_codes_in_ITU_region_2xx_(Europe)",
                "Mobile_network_codes_in_ITU_region_3xx_(North_America)",
                "Mobile_network_codes_in_ITU_region_4xx_(Asia)",
                "Mobile_network_codes_in_ITU_region_5xx_(Oceania)",
                "Mobile_network_codes_in_ITU_region_6xx_(Africa)",
                "Mobile_network_codes_in_ITU_region_7xx_(South_America)");
        // the committed file was made with a shell glob, so alphabetical order;
        // GenerateImsiDat lets a later file win a repeated code, so this matters
        return titles.stream()
                .map(t -> new Fetch(WIKI + encode(t) + "&action=raw", t + ".wiki"))
                .sorted(Comparator.comparing(Fetch::file))
                .toList();
    }

    private static List<Fetch> cnRegions() {
        List<Fetch> pages = new ArrayList<>();
        for (int i = 1; i <= 8; i++) {
            String title = "中华人民共和国行政区划代码_(" + i + "区)";
            pages.add(new Fetch("https://zh.wikipedia.org/w/index.php?title=" + encode(title)
                    + "&action=raw", "region" + i + ".wiki"));
        }
        return pages;
    }

    /** The spreadsheet linked from the SIX data standards page, whose name is dated. */
    private static List<Fetch> cfiSpreadsheet(Map<String, String> roots) {
        Matcher m = Pattern.compile("href=\"([^\"]*/cfi/[^\"]*\\.xlsx)\"")
                .matcher(roots.get("six.html"));
        List<String> found = new ArrayList<>();
        while (m.find()) {
            if (!found.contains(m.group(1))) {
                found.add(m.group(1));
            }
        }
        if (found.size() != 1) {
            throw new IllegalStateException("the SIX page links " + found.size()
                    + " CFI spreadsheets, expected exactly one: " + found);
        }
        return List.of(new Fetch(found.get(0), "cfi.xlsx"));
    }

    /** One regency list per province, the province codes coming from the first. */
    private static List<Fetch> indonesianRegencies(Map<String, String> roots) {
        Matcher m = Pattern.compile("\"kode_bps\":\"([0-9]+)\"").matcher(roots.get("provinsi.json"));
        List<Fetch> more = new ArrayList<>();
        List<String> seen = new ArrayList<>();
        while (m.find()) {
            if (seen.add(m.group(1)) && !seen.subList(0, seen.size() - 1).contains(m.group(1))) {
                more.add(new Fetch(BPS + "?level=kabupaten&parent=" + m.group(1),
                        "kab-" + m.group(1) + ".json"));
            }
        }
        return more;
    }

    /** One file per region the address service lists. */
    private static List<Fetch> addressRegions(Map<String, String> roots) {
        Matcher m = Pattern.compile("\"countries\"\\s*:\\s*\"([^\"]*)\"")
                .matcher(roots.get("data.json"));
        if (!m.find()) {
            throw new IllegalStateException("no country list in the address service index");
        }
        return Arrays.stream(m.group(1).split("~"))
                .filter(c -> c.matches("[A-Z]{2}"))
                .map(c -> new Fetch(ADDRESS + "/" + c, c + ".json"))
                .toList();
    }

    // ------------------------------------------------------------------
    // running

    private enum Status { UNCHANGED, CHANGED, FAILED }

    private record Result(Target target, Status status, String detail, String stderr,
                          byte[] payload) {
    }

    private static HttpClient http;
    private static Semaphore permits;
    private static int requestTimeoutBonus = 15;

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

        for (int i = 0; i < args.length; i++) {
            String a = args[i];
            switch (a) {
                case "--list" -> {
                    TARGETS.forEach(t -> System.out.printf("%-16s %-26s %2d source(s)  %s%n",
                            t.id(), t.generator(), t.roots().size()
                                    + (t.expand() == null ? 0 : 1), t.out()));
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
                default -> {
                    if (a.startsWith("-")) {
                        System.err.println("unknown flag: " + a);
                        System.exit(2);
                    }
                    ids.add(a);
                }
            }
        }

        List<Target> chosen = TARGETS.stream()
                .filter(t -> ids.isEmpty() || ids.contains(t.id()))
                .toList();
        List<String> unknown = ids.stream()
                .filter(id -> TARGETS.stream().noneMatch(t -> t.id().equals(id)))
                .toList();
        if (!unknown.isEmpty()) {
            System.err.println("unknown target(s): " + unknown + "; known: "
                    + TARGETS.stream().map(Target::id).collect(Collectors.joining(" ")));
            System.exit(2);
        }

        permits = new Semaphore(jobs);
        http = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(20))
                // NORMAL, not ALWAYS: ALWAYS would follow https down to http
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();

        Path scratch = offline != null ? offline
                : Files.createTempDirectory("stdnum-regen-");
        Path classes = scratch.resolve("classes");
        Path work = scratch.resolve("work");
        Files.createDirectories(work);

        if (offline == null) {
            compileGenerators(classes);
        }

        long deadline = System.nanoTime() + Duration.ofMinutes(deadlineMinutes).toNanos();
        List<Result> results = new ArrayList<>();
        for (Target t : chosen) {
            if (System.nanoTime() > deadline) {
                results.add(new Result(t, Status.FAILED, "the run ran out of time", "", null));
                continue;
            }
            results.add(run(t, classes, work.resolve(t.id()), offline != null));
        }

        for (Result r : results) {
            if (r.status() == Status.CHANGED && !check) {
                Path to = outDir != null ? outDir.resolve(r.target().id() + ".dat")
                        : Path.of(r.target().out());
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

        boolean anyChanged = results.stream().anyMatch(r -> r.status() == Status.CHANGED);
        boolean anyFailed = results.stream().anyMatch(r -> r.status() == Status.FAILED);
        // an explicit exit: the HttpClient of 17 has no close() and its threads linger
        System.exit(check && (anyChanged || anyFailed) ? 1 : strict && anyFailed ? 1 : 0);
    }

    private static Result run(Target t, Path classes, Path work, boolean offline) {
        StringBuilder stderr = new StringBuilder();
        try {
            Files.createDirectories(work);
            Map<String, String> rootText = new LinkedHashMap<>();
            List<Fetch> all = new ArrayList<>(t.roots());
            for (Fetch f : t.roots()) {
                byte[] body = obtain(t, f, work, offline);
                rootText.put(f.file(), new String(body, StandardCharsets.UTF_8));
            }
            if (t.expand() != null) {
                List<Fetch> more = new ArrayList<>(t.expand().expand(rootText));
                more.sort(Comparator.comparing(Fetch::file));
                for (Fetch f : more) {
                    obtain(t, f, work, offline);
                }
                all.addAll(more);
            }

            List<String> argv = new ArrayList<>(t.prefixArgs());
            all.stream().filter(Fetch::pass).map(f -> work.resolve(f.file()).toString())
                    .forEach(argv::add);
            argv.addAll(t.suffixArgs());

            Path stdoutFile = work.resolve("__stdout");
            Path stderrFile = work.resolve("__stderr");
            List<String> command = new ArrayList<>(List.of(javaBinary(),
                    // the generators read the JDK's locale data; pin what we can
                    "-Duser.language=en", "-Duser.country=US", "-Dfile.encoding=UTF-8",
                    "-cp", classes.toString(), t.generator()));
            command.addAll(argv);
            // to files, never pipes: oui.dat is two megabytes and would fill the
            // operating system's pipe buffer long before the process exits
            Process p = new ProcessBuilder(command)
                    .directory(work.toFile())
                    .redirectOutput(stdoutFile.toFile())
                    .redirectError(stderrFile.toFile())
                    .start();
            if (!p.waitFor(5, TimeUnit.MINUTES)) {
                p.destroyForcibly();
                return new Result(t, Status.FAILED, t.generator() + " did not finish in 5 minutes",
                        "", null);
            }
            if (Files.exists(stderrFile)) {
                stderr.append(Files.readString(stderrFile, StandardCharsets.UTF_8));
            }
            byte[] payload = Files.exists(stdoutFile) ? Files.readAllBytes(stdoutFile) : new byte[0];
            if (p.exitValue() != 0) {
                return new Result(t, Status.FAILED,
                        t.generator() + " exited " + p.exitValue()
                                + (p.exitValue() == 2 ? " (the driver built bad arguments)"
                                        : " (the source changed shape)"),
                        stderr.toString(), null);
            }
            if (payload.length == 0) {
                return new Result(t, Status.FAILED, t.generator() + " produced nothing",
                        stderr.toString(), null);
            }
            return compare(t, normalise(payload), stderr.toString());
        } catch (Exception e) {
            Throwable cause = e instanceof ExecutionException && e.getCause() != null
                    ? e.getCause() : e;
            return new Result(t, Status.FAILED,
                    cause.getClass().getSimpleName() + ": " + cause.getMessage(),
                    stderr.toString(), null);
        }
    }

    /** LF everywhere, so a run on Windows and a run on Linux agree. */
    private static byte[] normalise(byte[] payload) {
        String text = new String(payload, StandardCharsets.UTF_8).replace("\r\n", "\n");
        return text.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Whether the file moved, ignoring the header lines that differ on every
     * download. A volatile line is masked <em>in place</em>, so that a source
     * which stops emitting one shifts the others and is reported as a change
     * rather than quietly masked away.
     */
    private static Result compare(Target t, byte[] fresh, String stderr) throws IOException {
        Path committed = Path.of(t.out());
        if (!Files.exists(committed)) {
            return new Result(t, Status.CHANGED, "new file", stderr, fresh);
        }
        List<String> was = mask(t, new String(normalise(Files.readAllBytes(committed)),
                StandardCharsets.UTF_8));
        List<String> now = mask(t, new String(fresh, StandardCharsets.UTF_8));
        if (was.equals(now)) {
            return new Result(t, Status.UNCHANGED, "", stderr, null);
        }
        long added = now.stream().filter(l -> !was.contains(l)).count();
        long removed = was.stream().filter(l -> !now.contains(l)).count();
        return new Result(t, Status.CHANGED,
                was.size() + " -> " + now.size() + " lines (+" + added + " -" + removed + ")",
                stderr, fresh);
    }

    private static List<String> mask(Target t, String text) {
        List<String> lines = new ArrayList<>(List.of(text.split("\n", -1)));
        for (int i = 0; i < lines.size(); i++) {
            for (Pattern p : t.volatile_()) {
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

    private static byte[] obtain(Target t, Fetch f, Path work, boolean offline) throws Exception {
        Path to = work.resolve(f.file());
        if (offline) {
            if (!Files.exists(to)) {
                throw new IOException("offline, and " + to + " is not there");
            }
            return Files.readAllBytes(to);
        }
        byte[] body = fetch(URI.create(f.url()), t.timeout());
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
                                .orTimeout(seconds + requestTimeoutBonus, TimeUnit.SECONDS);
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
    // odds and ends

    private static void compileGenerators(Path classes) throws Exception {
        Files.createDirectories(classes);
        List<String> command = new ArrayList<>(List.of(
                Path.of(System.getProperty("java.home"), "bin", tool("javac")).toString(),
                "-encoding", "UTF-8", "-nowarn", "-d", classes.toString()));
        try (var sources = Files.list(Path.of("tools"))) {
            sources.filter(p -> p.toString().endsWith(".java"))
                    .sorted()
                    .forEach(p -> command.add(p.toString()));
        }
        Process p = new ProcessBuilder(command).inheritIO().start();
        if (!p.waitFor(5, TimeUnit.MINUTES) || p.exitValue() != 0) {
            p.destroyForcibly();
            throw new IOException("the generators do not compile");
        }
    }

    private static String javaBinary() {
        return Path.of(System.getProperty("java.home"), "bin", tool("java")).toString();
    }

    private static String tool(String name) {
        return System.getProperty("os.name").toLowerCase(Locale.ROOT).startsWith("windows")
                ? name + ".exe" : name;
    }

    private static String encode(String title) {
        return java.net.URLEncoder.encode(title, StandardCharsets.UTF_8).replace("+", "_");
    }

    private static void report(Path dir, List<Result> results) throws IOException {
        Files.createDirectories(dir);
        StringBuilder summary = new StringBuilder();
        for (Result r : results) {
            summary.append(r.target().id()).append('\t')
                    .append(r.status().name().toLowerCase(Locale.ROOT)).append('\t')
                    .append(r.target().out()).append('\t')
                    .append(r.detail().replace('\t', ' ')).append('\t')
                    .append(r.target().title()).append('\n');
            if (!r.stderr().isBlank()) {
                Files.writeString(dir.resolve(r.target().id() + ".err"), r.stderr());
            }
            if (r.payload() != null) {
                Files.write(dir.resolve(r.target().id() + ".dat"), r.payload());
            }
            if (r.status() == Status.CHANGED && !r.target().redFlags().isEmpty()) {
                Files.writeString(dir.resolve(r.target().id() + ".flags"),
                        String.join("\n", r.target().redFlags()) + "\n");
            }
        }
        Files.writeString(dir.resolve("summary.tsv"), summary.toString());
    }

    private static void print(List<Result> results, boolean check) {
        PrintStream out = new PrintStream(System.out, true, StandardCharsets.UTF_8);
        for (Result r : results) {
            out.printf("%-9s %-16s %s%n", r.status().name().toLowerCase(Locale.ROOT),
                    r.target().id(), r.detail());
            if (!r.stderr().isBlank()) {
                r.stderr().lines().forEach(l -> out.println("          | " + l));
            }
        }
        long changed = results.stream().filter(r -> r.status() == Status.CHANGED).count();
        long failed = results.stream().filter(r -> r.status() == Status.FAILED).count();
        out.printf("%n%d target(s): %d unchanged, %d changed, %d failed%s%n",
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
