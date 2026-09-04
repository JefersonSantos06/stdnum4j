import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Generates the be-banks.dat registry consumed by the BeIban class.
 *
 * <p>Source: the grouped list of bank identification codes the National Bank
 * of Belgium publishes as a spreadsheet. Each row gives a range of the
 * three-digit bank code, the BIC, and the institution's name in up to four
 * languages.</p>
 *
 * <p>The list also records the ranges nobody holds, as VRIJ/LIBRE (free) or
 * Onbeschikbaar/Indisponible (unavailable). Those name no institution, so an
 * account number in one of them belongs to nobody; they are left out.</p>
 *
 */
public final class GenerateBeBanksDat implements Source {

    @Override
    public String id() {
        return "be-banks";
    }

    @Override
    public String title() {
        return "Regenerate be-banks.dat, the Belgian bank codes";
    }

    @Override
    public String output() {
        return "stdnum4j-eu/src/main/resources/io/github/jefersonsantos06/stdnum/eu/be-banks.dat";
    }

    @Override
    public List<Download> downloads(Map<String, String> seeds) {
        return List.of(new Download(
                "https://www.nbb.be/doc/be/be/protocol/grouped_list_current.xlsx",
                "grouped_list_current.xlsx"));
    }

    @Override
    public void generate(Run run, PrintStream out) throws Exception {
        List<List<String>> rows = Xlsx.rows(run.file("grouped_list_current.xlsx"));
        if (rows.size() < 3) {
            throw new IllegalStateException("no bank rows found: the spreadsheet layout has changed");
        }
        String version = cell(rows.get(0), 0);

        List<String> entries = new ArrayList<>();
        // the first row is the version stamp and the second the column headings
        for (List<String> row : rows.subList(2, rows.size())) {
            String entry = parseRow(row);
            if (entry != null) {
                entries.add(entry);
            }
        }
        if (entries.isEmpty()) {
            throw new IllegalStateException("no bank rows found: the spreadsheet layout has changed");
        }

        out.println("# Belgian bank identification codes: the range of the three-digit code");
        out.println("# each institution holds, with its BIC and its name.");
        out.println("# Generated from grouped_list_current.xlsx downloaded from");
        out.println("# https://www.nbb.be/doc/be/be/protocol/grouped_list_current.xlsx");
        out.println("# " + version);
        entries.forEach(out::println);
    }

    /** The names the list gives a range that no institution holds. */
    private static final Set<String> UNHELD =
            Set.of("VRIJ", "LIBRE", "FREI", "FREE",
                    "ONBESCHIKBAAR", "INDISPONIBLE", "NICHT VERFUGBAR", "NOT AVAILABLE");

    /** One range record, or null when the row names no institution. */
    private static String parseRow(List<String> row) {
        String low = cell(row, 0);
        String high = cell(row, 1);
        String bic = cell(row, 2).replace(" ", "");
        // the name is given in up to four languages; take the last one filled in
        String bank = "";
        for (int i = 2; i < row.size(); i++) {
            if (!cell(row, i).isEmpty()) {
                bank = cell(row, i);
            }
        }
        if (low.isEmpty() || high.isEmpty() || (bic.isEmpty() && bank.isEmpty())) {
            return null;
        }
        if (bic.equals("VRIJ") || UNHELD.contains(bank.toUpperCase(Locale.ROOT))) {
            return null;
        }
        StringBuilder sb = new StringBuilder(low).append('-').append(high);
        if (!bic.isEmpty()) {
            sb.append(" bic=\"").append(bic.replace("\"", "")).append('"');
        }
        if (!bank.isEmpty()) {
            sb.append(" bank=\"").append(bank.replace("\"", "")).append('"');
        }
        return sb.toString();
    }

    private static String cell(List<String> row, int column) {
        return column < row.size() ? row.get(column).strip() : "";
    }
}
