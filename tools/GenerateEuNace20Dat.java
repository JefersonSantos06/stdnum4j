import java.io.PrintStream;
import java.util.List;
import java.util.Map;

/**
 * NACE Rev. 2: the sections, divisions, groups and classes of the European
 * statistical classification of economic activities.
 *
 * <p>Eurostat publishes the classification itself through its SDMX API, which
 * is a better source than a rendering of it. The two revisions are two
 * sources rather than one with an argument: which revision this is is what
 * this class <em>is</em>, not something told to it.</p>
 *
 * <p>{@link GenerateEuNaceDat} holds the reading, shared with the other
 * revision.</p>
 */
public final class GenerateEuNace20Dat implements Source {

    @Override
    public String id() {
        return "eu-nace20";
    }

    @Override
    public String title() {
        return "Regenerate eu-nace20.dat, NACE Rev. 2";
    }

    @Override
    public String output() {
        return "stdnum4j-eu/src/main/resources/io/github/jefersonsantos06/stdnum/eu/eu-nace20.dat";
    }

    @Override
    public List<Download> downloads(Map<String, String> seeds) {
        return List.of(new Download(
                "https://ec.europa.eu/eurostat/api/dissemination/sdmx/2.1/codelist/ESTAT/NACE_R2",
                "NACE_R2.xml"));
    }

    @Override
    public void generate(Run run, PrintStream out) throws Exception {
        GenerateEuNaceDat.emit(run.file("NACE_R2.xml"), "Rev. 2", out);
    }
}
