import java.io.PrintStream;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * One data file: where it comes from, and how its source becomes a
 * {@code .dat}. Every generator in {@code tools/} is one of these.
 *
 * <p>A source is not a program. It declares what it needs and writes what it
 * makes; the fetching, the character set, the line endings, the buffering and
 * the comparison belong to {@link Regenerate}, once, for all of them. That is
 * the point: there is one way to produce a data file, so producing one by
 * hand and producing all sixteen cannot drift apart.</p>
 *
 * <p>It also means a source takes no arguments. The retrieval date a header
 * needs comes from {@link Run#retrievedOn()}; a revision label that used to be
 * an argument is now simply what that source is. Nothing is passed in from a
 * command line, because there is no command line to pass it from.</p>
 *
 * <p>Refusing is a normal outcome. When the upstream layout has moved, throw
 * with a sentence saying so — {@code Regenerate} reports the target as failed,
 * writes nothing, and carries on with the others.</p>
 */
public interface Source {

    /** The name on the command line, on the branch and in the report. */
    String id();

    /** The commit subject when this file moves. */
    String title();

    /** Where the result goes, relative to the repository root. */
    String output();

    /**
     * Files fetched before {@link #downloads}, whose text decides what else
     * is needed. Almost always empty: only three sources have to look at one
     * answer to know the next question.
     */
    default List<Download> seeds() {
        return List.of();
    }

    /**
     * Everything {@link #generate} reads, in the order it reads them, given
     * the text of the seeds. A seed that is itself an input appears here too;
     * it is not fetched twice.
     */
    List<Download> downloads(Map<String, String> seeds);

    /**
     * Turns the downloads into the data file.
     *
     * @throws Exception when the source no longer has the shape this reads —
     *                   the message is what the report shows
     */
    void generate(Run run, PrintStream out) throws Exception;

    /**
     * Header lines that differ on every download and mean nothing — the ISBN
     * export's serial, a retrieval date. A file whose only difference is one
     * of these has not changed, and must not open a pull request.
     */
    default List<String> volatileLines() {
        return List.of();
    }

    /** What a person has to revisit when this file moves. Usually nothing. */
    default List<String> redFlags() {
        return List.of();
    }

    /** Seconds for one request. The IEEE registry alone needs minutes. */
    default int timeoutSeconds() {
        return 60;
    }

    /** One file to fetch, and the name it is saved under. */
    record Download(String url, String file) {
    }

    /** What a source is given while it runs. */
    interface Run {

        /** A downloaded file, by the name it was saved under. */
        Path file(String name);

        /** Every download, in the order {@link #downloads} listed them. */
        List<Path> files();

        /** The day the sources were fetched, for a header that records it. */
        LocalDate retrievedOn();

        /**
         * Something worth reading afterwards that is not a failure — a row
         * skipped, a count worth checking. It reaches the report and the
         * pull request.
         */
        void warn(String message);
    }
}
