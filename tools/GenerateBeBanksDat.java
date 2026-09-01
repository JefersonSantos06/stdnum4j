import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Generates the be-banks.dat registry consumed by the BeIban class.
 *
 * <p>Source: the grouped list of bank identification codes the National Bank
 * of Belgium publishes as a spreadsheet. Each row gives a range of the
 * three-digit bank code, the BIC, and the institution's name in up to four
 * languages.</p>
 *
 * <p>Usage:</p>
 * <pre>
 *   curl -L -o grouped_list_current.xlsx \
 *       https://www.nbb.be/doc/be/be/protocol/grouped_list_current.xlsx
 *   java -cp tools tools/GenerateBeBanksDat.java grouped_list_current.xlsx \
 *       &gt; stdnum-eu/src/main/resources/io/github/jefersonsantos06/stdnum/eu/be-banks.dat
 * </pre>
 */
public final class GenerateBeBanksDat {

    private GenerateBeBanksDat() {
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("usage: java GenerateBeBanksDat.java <grouped_list_current.xlsx>");
            System.exit(2);
        }
        List<List<String>> rows = Xlsx.rows(Path.of(args[0]));
        if (rows.size() < 3) {
            System.err.println("no bank rows found: the spreadsheet layout has changed");
            System.exit(1);
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
            System.err.println("no bank rows found: the spreadsheet layout has changed");
            System.exit(1);
        }

        PrintStream out = new PrintStream(System.out, true, StandardCharsets.UTF_8);
        out.println("# Belgian bank identification codes: the range of the three-digit code");
        out.println("# each institution holds, with its BIC and its name.");
        out.println("# Generated from grouped_list_current.xlsx downloaded from");
        out.println("# https://www.nbb.be/doc/be/be/protocol/grouped_list_current.xlsx");
        out.println("# " + version);
        entries.forEach(out::println);
    }

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
