import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Generates the nz-banks.dat registry consumed by the NzBankaccount class.
 *
 * <p>Source: the bank branch register Payments NZ publishes as a
 * spreadsheet. Each row is one branch, giving its bank number, its branch
 * number and the names of both.</p>
 *
 * <p>The output nests the branches under their bank, one level of
 * indentation deep, which is how the account number is read: two digits of
 * bank, then four of branch.</p>
 *
 * <p>Usage:</p>
 * <pre>
 *   curl -L -o BankBranchRegister.xlsx \
 *       https://www.paymentsnz.co.nz/resources/industry-registers/bank-branch-register/download/xlsx/
 *   java -cp tools tools/GenerateNzBanksDat.java BankBranchRegister.xlsx \
 *       &gt; stdnum-apac/src/main/resources/io/github/jefersonsantos06/stdnum/apac/nz-banks.dat
 * </pre>
 */
public final class GenerateNzBanksDat {

    private GenerateNzBanksDat() {
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("usage: java GenerateNzBanksDat.java <BankBranchRegister.xlsx>");
            System.exit(2);
        }
        List<List<String>> rows = Xlsx.rows(Path.of(args[0]));
        if (rows.size() < 2) {
            System.err.println("no branch rows found: the spreadsheet layout has changed");
            System.exit(1);
        }
        List<String> headings = rows.get(0);
        int bankNumber = columnOf(headings, "Bank_Number");
        int branchNumber = columnOf(headings, "Branch_Number");
        int bankName = columnOf(headings, "Bank_Name");
        int branchName = columnOf(headings, "Branch_Information");
        if (bankNumber < 0 || branchNumber < 0 || bankName < 0 || branchName < 0) {
            System.err.println("missing a column: the spreadsheet headings have changed");
            System.exit(1);
        }

        // bank -> its name and its branches, both kept in numeric order
        Map<String, String> bankNames = new TreeMap<>();
        Map<String, Map<String, String>> branches = new LinkedHashMap<>();
        for (List<String> row : rows.subList(1, rows.size())) {
            String bank = cell(row, bankNumber);
            String branch = cell(row, branchNumber);
            if (bank.isEmpty() || branch.isEmpty()) {
                continue;
            }
            bankNames.putIfAbsent(bank, cell(row, bankName));
            branches.computeIfAbsent(bank, k -> new TreeMap<>())
                    .putIfAbsent(branch, cell(row, branchName));
        }
        if (bankNames.isEmpty()) {
            System.err.println("no branch rows found: the spreadsheet layout has changed");
            System.exit(1);
        }

        PrintStream out = new PrintStream(System.out, true, StandardCharsets.UTF_8);
        out.println("# New Zealand bank and branch numbers: the two-digit bank, with its");
        out.println("# branches nested under it.");
        out.println("# Generated from the bank branch register Payments NZ publishes at");
        out.println("# https://www.paymentsnz.co.nz/resources/industry-registers/bank-branch-register/");
        for (Map.Entry<String, String> bank : bankNames.entrySet()) {
            out.println(bank.getKey() + " bank=\"" + escape(bank.getValue()) + "\"");
            for (Map.Entry<String, String> branch : branches.get(bank.getKey()).entrySet()) {
                out.println(" " + branch.getKey()
                        + " branch=\"" + escape(branch.getValue()) + "\"");
            }
        }
    }

    private static int columnOf(List<String> headings, String name) {
        for (int i = 0; i < headings.size(); i++) {
            if (headings.get(i).strip().equalsIgnoreCase(name)) {
                return i;
            }
        }
        return -1;
    }

    private static String cell(List<String> row, int column) {
        return column < row.size() ? row.get(column).strip() : "";
    }

    private static String escape(String value) {
        return value.replaceAll("\\s+", " ").replace("\"", "").strip();
    }
}
