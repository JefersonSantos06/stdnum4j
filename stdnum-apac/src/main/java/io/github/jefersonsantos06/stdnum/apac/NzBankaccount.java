package io.github.jefersonsantos06.stdnum.apac;

import io.github.jefersonsantos06.stdnum.numdb.NumDb;
import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidComponentException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The New Zealand bank account number: two digits of bank, four of branch,
 * seven of account and three of suffix, usually written in those four
 * groups.
 *
 * <p>There is no single check digit rule. Each bank was assigned one of
 * seven algorithms, differing in their weights and in the two moduli they
 * fold by; one of them, X, checks nothing at all. A bank on algorithm A
 * switches to B for the account numbers from 990000 up.</p>
 */
public final class NzBankaccount implements StdNum {

    public static final NzBankaccount INSTANCE = new NzBankaccount();

    /** The algorithm each bank was assigned; anything else checks nothing. */
    private static final Map<String, Character> ALGORITHMS = Map.ofEntries(
            Map.entry("01", 'A'), Map.entry("02", 'A'), Map.entry("03", 'A'),
            Map.entry("04", 'A'), Map.entry("06", 'A'), Map.entry("08", 'D'),
            Map.entry("09", 'E'), Map.entry("10", 'A'), Map.entry("11", 'A'),
            Map.entry("12", 'A'), Map.entry("13", 'A'), Map.entry("14", 'A'),
            Map.entry("15", 'A'), Map.entry("16", 'A'), Map.entry("17", 'A'),
            Map.entry("18", 'A'), Map.entry("19", 'A'), Map.entry("20", 'A'),
            Map.entry("21", 'A'), Map.entry("22", 'A'), Map.entry("23", 'A'),
            Map.entry("24", 'A'), Map.entry("25", 'F'), Map.entry("26", 'G'),
            Map.entry("27", 'A'), Map.entry("28", 'G'), Map.entry("29", 'G'),
            Map.entry("30", 'A'), Map.entry("31", 'X'), Map.entry("33", 'F'),
            Map.entry("35", 'A'), Map.entry("38", 'A'));

    private static final Map<Character, int[]> WEIGHTS = Map.of(
            'A', new int[] {0, 0, 6, 3, 7, 9, 0, 10, 5, 8, 4, 2, 1, 0, 0, 0},
            'B', new int[] {0, 0, 0, 0, 0, 0, 0, 10, 5, 8, 4, 2, 1, 0, 0, 0},
            'D', new int[] {0, 0, 0, 0, 0, 0, 7, 6, 5, 4, 3, 2, 1, 0, 0, 0},
            'E', new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 5, 4, 3, 2, 0, 0, 1},
            'F', new int[] {0, 0, 0, 0, 0, 0, 1, 7, 3, 1, 7, 3, 1, 0, 0, 0},
            'G', new int[] {0, 0, 0, 0, 0, 0, 1, 3, 7, 1, 3, 7, 1, 3, 7, 1},
            'X', new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0});

    /** The modulus each product is folded by, and the modulus of the sum. */
    private static final Map<Character, int[]> MODULI = Map.of(
            'A', new int[] {11, 11},
            'B', new int[] {11, 11},
            'D', new int[] {11, 11},
            'E', new int[] {9, 11},
            'F', new int[] {10, 10},
            'G', new int[] {9, 10},
            'X', new int[] {1, 1});

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("nz.bankaccount", "Bank account number")
                    .country("NZ")
                    .title("New Zealand bank account number")
                    .description("New Zealand bank account number: 16 digits as bank, branch,"
                            + " account and suffix, checked by the bank's own algorithm.")
                    .tags(Tag.BANK)
                    .references("https://www.paymentsnz.co.nz/resources/industry-registers/"
                            + "bank-branch-register/")
                    .build();

    private NzBankaccount() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    /** The banks, with their branches nested under them. */
    private static NumDb banks() {
        return NumDb.load(NzBankaccount.class, "nz-banks.dat");
    }

    @Override
    public String compact(String number) {
        String[] parts = Strings.compact(number, "").replace(' ', '-').split("-", -1);
        if (parts.length == 4) {
            int[] lengths = {2, 4, 7, 3};
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 4; i++) {
                sb.append(pad(parts[i], lengths[i]));
            }
            return sb.toString();
        }
        String joined = String.join("", parts);
        return joined.length() <= 13
                ? joined + "000"
                : joined.substring(0, 13) + pad(joined.substring(13), 3);
    }

    private static String pad(String part, int width) {
        return "0".repeat(Math.max(0, width - part.length())) + part;
    }

    /** The weighted sum under the bank's own algorithm; a valid account yields 0. */
    public static int checksum(String number) {
        String n = INSTANCE.compact(number);
        char algorithm = ALGORITHMS.getOrDefault(n.substring(0, Math.min(2, n.length())), 'X');
        // a bank on A switches to B for the account numbers from 990000 up
        if (algorithm == 'A' && n.length() >= 13 && n.substring(6, 13).compareTo("0990000") >= 0) {
            algorithm = 'B';
        }
        int[] weights = WEIGHTS.get(algorithm);
        int first = MODULI.get(algorithm)[0];
        int second = MODULI.get(algorithm)[1];
        int sum = 0;
        for (int i = 0; i < weights.length && i < n.length(); i++) {
            int product = weights[i] * (n.charAt(i) - '0');
            sum += product > first ? product % first : product;
        }
        return sum % second;
    }

    /** What is known about the bank and the branch the account is held at. */
    public static Map<String, String> info(String number) {
        Map<String, String> info = new LinkedHashMap<>();
        for (NumDb.Entry entry : banks().info(INSTANCE.compact(number))) {
            info.putAll(entry.properties());
        }
        return info;
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (!Strings.isDigits(n)) {
            throw new InvalidFormatException();
        }
        if (n.length() != 16) {
            throw new InvalidLengthException();
        }
        if (checksum(n) != 0) {
            throw new InvalidChecksumException();
        }
        Map<String, String> info = info(n);
        if (!info.containsKey("bank") || !info.containsKey("branch")) {
            throw new InvalidComponentException("Not the number of a bank and branch.");
        }
        return n;
    }

    @Override
    public String format(String number) {
        String n = validate(number);
        return n.substring(0, 2) + '-' + n.substring(2, 6) + '-'
                + n.substring(6, 13) + '-' + n.substring(13);
    }
}
