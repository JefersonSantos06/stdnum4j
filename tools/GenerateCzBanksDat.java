import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Generates the cz-banks.dat registry consumed by the CzBankaccount class.
 *
 * <p>Source: the payment system code list the Czech National Bank publishes
 * as a semicolon-separated file. Its columns are the four-digit code, the
 * institution, its BIC and whether it settles through CERTIS.</p>
 *
 */
public final class GenerateCzBanksDat implements Source {

    @Override
    public String id() {
        return "cz-banks";
    }

    @Override
    public String title() {
        return "Regenerate cz-banks.dat, the Czech payment system codes";
    }

    @Override
    public String output() {
        return "stdnum-eu/src/main/resources/io/github/jefersonsantos06/stdnum/eu/cz-banks.dat";
    }

    @Override
    public List<Download> downloads(Map<String, String> seeds) {
        return List.of(new Download(
                "https://www.cnb.cz/cs/platebni-styk/.galleries/ucty_kody_bank/download/kody_bank_CR.csv",
                "kody_bank_CR.csv"));
    }

    @Override
    public void generate(Run run, PrintStream out) throws Exception {
        List<String> lines = Files.readAllLines(run.file("kody_bank_CR.csv"), StandardCharsets.UTF_8);
        List<String> entries = new ArrayList<>();
        for (String line : lines) {
            String entry = parseRow(line);
            if (entry != null) {
                entries.add(entry);
            }
        }
        if (entries.isEmpty()) {
            throw new IllegalStateException("no bank rows found: the file layout has changed");
        }
        entries.sort(String::compareTo);

        // the institution names carry Czech diacritics, so the output must be
        // UTF-8 whatever the console the run is redirected from happens to use
        out.println("# Czech payment system codes: the four-digit code of each institution,");
        out.println("# its name, its BIC and whether it settles through CERTIS.");
        out.println("# Generated from the code list the Czech National Bank publishes at");
        out.println("# https://www.cnb.cz/cs/platebni-styk/ucty-kody-bank/");
        entries.forEach(out::println);
    }

    /** One bank record, or null when the row is not one. */
    private static String parseRow(String row) {
        String[] cells = row.split(";", -1);
        if (cells.length < 3) {
            return null;
        }
        String code = cells[0].strip();
        if (code.length() != 4 || !code.chars().allMatch(Character::isDigit)) {
            return null;  // the header row, and any trailing notes
        }
        StringBuilder sb = new StringBuilder(code);
        sb.append(" bank=\"").append(escape(cells[1])).append('"');
        String bic = cells[2].strip();
        if (!bic.isEmpty()) {
            sb.append(" bic=\"").append(escape(bic)).append('"');
        }
        if (cells.length > 3 && cells[3].strip().equalsIgnoreCase("A")) {
            sb.append(" certis=\"True\"");
        }
        return sb.toString();
    }

    /** A cell as a property value: collapsed whitespace, no quotes of its own. */
    private static String escape(String cell) {
        return cell.strip().replaceAll("\\s+", " ").replace("\"", "");
    }
}
