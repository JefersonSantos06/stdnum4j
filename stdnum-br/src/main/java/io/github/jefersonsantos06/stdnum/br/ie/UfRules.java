package io.github.jefersonsantos06.stdnum.br.ie;

import io.github.jefersonsantos06.stdnum.algo.Weighted;
import io.github.jefersonsantos06.stdnum.br.Uf;
import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidComponentException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.Message;
import io.github.jefersonsantos06.stdnum.text.Strings;

/**
 * The 27 state-specific validation routines, implemented from the official
 * "Roteiro de Crítica da Inscrição Estadual" published by SINTEGRA
 * (www.sintegra.gov.br/insc_est.html), state by state.
 *
 * <p>Two states have incomplete official pages and follow the rules that are
 * uniformly adopted by the market: the Federal District (whose SINTEGRA page
 * is empty; 13 digits starting with 07, two CNPJ-style check digits) and Rio
 * de Janeiro (whose page documents the remainder rule but not the weights;
 * 8 digits, weights 2,7,6,5,4,3,2).</p>
 */
final class UfRules {

    private UfRules() {
    }

    // shared weight tables
    private static final int[] W9_2 = Weighted.descending(9, 8);
    private static final int[] W10_2 = Weighted.descending(10, 9);
    private static final int[] W8_2 = Weighted.descending(8, 7);
    private static final int[] W7_2 = Weighted.descending(7, 6);
    private static final int[] W6_2 = Weighted.descending(6, 5);
    private static final int[] CYCLIC_11 = Weighted.cyclic(11, 2, 3, 4, 5, 6, 7, 8, 9);
    private static final int[] CYCLIC_12 = Weighted.cyclic(12, 2, 3, 4, 5, 6, 7, 8, 9);
    private static final int[] CYCLIC_13 = Weighted.cyclic(13, 2, 3, 4, 5, 6, 7, 8, 9);
    private static final int[] MT_10 = Weighted.cyclic(10, 2, 3, 4, 5, 6, 7, 8, 9);
    private static final int[] PR_8 = Weighted.cyclic(8, 2, 3, 4, 5, 6, 7);
    private static final int[] PR_9 = Weighted.cyclic(9, 2, 3, 4, 5, 6, 7);
    private static final int[] RJ_7 = Weighted.cyclic(7, 2, 3, 4, 5, 6, 7);
    private static final int[] MG_12 = {3, 2, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2};
    private static final int[] SP_8 = {1, 3, 4, 5, 6, 7, 8, 10};
    private static final int[] SP_11 = {3, 2, 10, 9, 8, 7, 6, 5, 4, 3, 2};
    private static final int[] PE_OLD_13 = {5, 4, 3, 2, 1, 9, 8, 7, 6, 5, 4, 3, 2};
    private static final int[] RS_10 = {2, 9, 8, 7, 6, 5, 4, 3, 2};

    static UfRule of(Uf uf) {
        return switch (uf) {
            case AC -> UfRules::ac;
            case AL -> UfRules::al;
            case AP -> UfRules::ap;
            case AM, SE, SC, PI, PB, ES, CE -> n -> standard(n, 9, W9_2);
            case BA -> UfRules::ba;
            case DF -> UfRules::df;
            case GO -> UfRules::go;
            case MA -> n -> standard(prefixed(n, "12"), 9, W9_2);
            case MT -> n -> standard(n, 11, MT_10);
            case MS -> n -> standard(prefixed(n, "28", "50"), 9, W9_2);
            case MG -> UfRules::mg;
            case PA -> n -> standard(prefixed(n, "15", "75", "76", "77", "78", "79"), 9, W9_2);
            case PR -> UfRules::pr;
            case PE -> UfRules::pe;
            case RJ -> n -> standard(n, 8, RJ_7);
            case RN -> UfRules::rn;
            case RS -> n -> standard(n, 10, RS_10);
            case RO -> UfRules::ro;
            case RR -> UfRules::rr;
            case SP -> UfRules::sp;
            case TO -> UfRules::to;
        };
    }

    // ------------------------------------------------------------------
    // helpers

    /** Requires an all-digit number with one of the given lengths. */
    private static String digits(String n, int... lengths) {
        if (!Strings.isDigits(n)) {
            throw new InvalidFormatException();
        }
        for (int length : lengths) {
            if (n.length() == length) {
                return n;
            }
        }
        throw new InvalidLengthException();
    }

    private static String prefixed(String n, String... prefixes) {
        for (String prefix : prefixes) {
            if (n.startsWith(prefix)) {
                return n;
            }
        }
        throw new InvalidComponentException(Message.of(InscricaoEstadual.class, "ie.prefix",
                "Unexpected state registration prefix."));
    }

    private static int digit(String n, int index) {
        return n.charAt(index) - '0';
    }

    private static void expect(String n, int index, int checkDigit) {
        if (digit(n, index) != checkDigit) {
            throw new InvalidChecksumException();
        }
    }

    /**
     * The most common shape: all digits, fixed length, one final check digit
     * over the preceding digits with the standard mod 11 rule (remainders
     * 0 and 1 give digit 0).
     */
    private static String standard(String n, int length, int[] weights) {
        digits(n, length);
        expect(n, length - 1, Weighted.mod11CheckDigit(n.substring(0, length - 1), weights));
        return n;
    }

    // ------------------------------------------------------------------
    // state-specific routines

    /** AC: 13 digits, prefix 01, two CNPJ-style check digits. */
    private static String ac(String n) {
        digits(n, 13);
        prefixed(n, "01");
        expect(n, 11, Weighted.mod11CheckDigit(n.substring(0, 11), CYCLIC_11));
        expect(n, 12, Weighted.mod11CheckDigit(n.substring(0, 12), CYCLIC_12));
        return n;
    }

    /** AL: 9 digits, prefix 24, company type in {0,3,5,7,8}, one check digit. */
    private static String al(String n) {
        digits(n, 9);
        prefixed(n, "24");
        int type = digit(n, 2);
        if (type != 0 && type != 3 && type != 5 && type != 7 && type != 8) {
            throw new InvalidComponentException(Message.of(InscricaoEstadual.class, "ie.al.company-type",
                    "Invalid company type digit for AL."));
        }
        expect(n, 8, Weighted.mod11CheckDigit(n.substring(0, 8), W9_2));
        return n;
    }

    /** AP: 9 digits, prefix 03, range-dependent offset and fallback digit. */
    private static String ap(String n) {
        digits(n, 9);
        prefixed(n, "03");
        long base = Long.parseLong(n.substring(0, 8));
        int p = 0;
        int d = 0;
        if (base >= 3_000_001L && base <= 3_017_000L) {
            p = 5;
            //noinspection DataFlowIssue
            d = 0;
        } else if (base >= 3_017_001L && base <= 3_019_022L) {
            p = 9;
            d = 1;
        }
        int sum = p + Weighted.weightedSum(n.substring(0, 8), W9_2);
        int difference = 11 - sum % 11;
        int dv = difference == 10 ? 0 : difference == 11 ? d : difference;
        expect(n, 8, dv);
        return n;
    }

    /**
     * BA: 8 or 9 digits with two check digits; modulo 10 or 11 chosen by the
     * first digit (8-digit numbers) or the second digit (9-digit numbers).
     * The last digit is computed first, over the base; the digit before it is
     * computed over the base plus the last digit.
     */
    private static String ba(String n) {
        digits(n, 8, 9);
        int baseLength = n.length() - 2;
        int key = n.length() == 8 ? digit(n, 0) : digit(n, 1);
        boolean mod11 = key == 6 || key == 7 || key == 9;
        String base = n.substring(0, baseLength);

        int[] weightsLast = Weighted.descending(baseLength + 1, baseLength);
        int[] weightsFirst = Weighted.descending(baseLength + 2, baseLength + 1);

        int dvLast = baDigit(Weighted.weightedSum(base, weightsLast), mod11);
        expect(n, baseLength + 1, dvLast);
        int dvFirst = baDigit(Weighted.weightedSum(base + dvLast, weightsFirst), mod11);
        expect(n, baseLength, dvFirst);
        return n;
    }

    private static int baDigit(int sum, boolean mod11) {
        int modulus = mod11 ? 11 : 10;
        int remainder = sum % modulus;
        if (remainder == 0 || (mod11 && remainder == 1)) {
            return 0;
        }
        return modulus - remainder;
    }

    /**
     * DF: 13 digits, prefix 07, two CNPJ-style check digits. The SINTEGRA
     * page for DF is empty; this is the rule uniformly adopted by the market.
     */
    private static String df(String n) {
        digits(n, 13);
        prefixed(n, "07");
        expect(n, 11, Weighted.mod11CheckDigit(n.substring(0, 11), CYCLIC_11));
        expect(n, 12, Weighted.mod11CheckDigit(n.substring(0, 12), CYCLIC_12));
        return n;
    }

    /** GO: 9 digits, prefix 10, 11 or 20-29, one standard check digit. */
    private static String go(String n) {
        digits(n, 9);
        int prefix = Integer.parseInt(n.substring(0, 2));
        if (prefix != 10 && prefix != 11 && (prefix < 20 || prefix > 29)) {
            throw new InvalidComponentException(Message.of(InscricaoEstadual.class, "ie.prefix",
                    "Unexpected state registration prefix."));
        }
        expect(n, 8, Weighted.mod11CheckDigit(n.substring(0, 8), W9_2));
        return n;
    }

    /**
     * MG: 13 digits. First check digit: insert a zero after the 3-digit
     * municipality code, multiply alternately by 1 and 2 and sum the digits
     * of the products; the digit completes the sum to the next multiple of
     * ten. Second check digit: weights 3,2,11,10,9,8,7,6,5,4,3,2 modulo 11.
     */
    private static String mg(String n) {
        digits(n, 13);
        String worked = n.substring(0, 3) + "0" + n.substring(3, 11);
        int sum = 0;
        for (int i = 0; i < worked.length(); i++) {
            int product = (worked.charAt(i) - '0') * (i % 2 == 0 ? 1 : 2);
            sum += product / 10 + product % 10;
        }
        expect(n, 11, (10 - sum % 10) % 10);
        expect(n, 12, Weighted.mod11CheckDigit(n.substring(0, 12), MG_12));
        return n;
    }

    /** PR: 10 digits, two check digits with cyclic weights 2..7. */
    private static String pr(String n) {
        digits(n, 10);
        expect(n, 8, Weighted.mod11CheckDigit(n.substring(0, 8), PR_8));
        expect(n, 9, Weighted.mod11CheckDigit(n.substring(0, 9), PR_9));
        return n;
    }

    /**
     * PE: 9 digits (current eFisco format, two standard check digits) or
     * 14 digits (legacy CACEPE format, one check digit with weights
     * 5,4,3,2,1,9,8,7,6,5,4,3,2 where results above 9 drop 10).
     */
    private static String pe(String n) {
        digits(n, 9, 14);
        if (n.length() == 9) {
            expect(n, 7, Weighted.mod11CheckDigit(n.substring(0, 7), W8_2));
            expect(n, 8, Weighted.mod11CheckDigit(n.substring(0, 8), W9_2));
        } else {
            int dv = 11 - Weighted.weightedSum(n.substring(0, 13), PE_OLD_13) % 11;
            if (dv > 9) {
                dv -= 10;
            }
            expect(n, 13, dv);
        }
        return n;
    }

    /** RN: prefix 20; 9 digits (weights 10..2 truncated to 9..2) or 10 digits (weights 10..2). */
    private static String rn(String n) {
        digits(n, 9, 10);
        prefixed(n, "20");
        if (n.length() == 9) {
            expect(n, 8, Weighted.mod11CheckDigit(n.substring(0, 8), W9_2));
        } else {
            expect(n, 9, Weighted.mod11CheckDigit(n.substring(0, 9), W10_2));
        }
        return n;
    }

    /**
     * RO: 9 digits (legacy: 3-digit municipality, 5-digit company, check
     * digit over the company part only) or 14 digits (current: 13-digit base
     * with cyclic weights 2..9). In both, results 10 and 11 drop 10 —
     * producing digits 0 and 1 — instead of collapsing to 0.
     */
    private static String ro(String n) {
        digits(n, 9, 14);
        if (n.length() == 9) {
            expect(n, 8, roDigit(Weighted.weightedSum(n.substring(3, 8), W6_2)));
        } else {
            expect(n, 13, roDigit(Weighted.weightedSum(n.substring(0, 13), CYCLIC_13)));
        }
        return n;
    }

    private static int roDigit(int sum) {
        int dv = 11 - sum % 11;
        return dv > 9 ? dv - 10 : dv;
    }

    /** RR: 9 digits, prefix 24, check digit is the position-weighted sum modulo 9. */
    private static String rr(String n) {
        digits(n, 9);
        prefixed(n, "24");
        int sum = 0;
        for (int i = 0; i < 8; i++) {
            sum += digit(n, i) * (i + 1);
        }
        expect(n, 8, sum % 9);
        return n;
    }

    /**
     * SP: 12 digits for industrial/commercial registrations (check digits at
     * positions 9 and 12, each the rightmost digit of the weighted sum
     * modulo 11) or 13 characters starting with {@code P} for rural
     * producers (one check digit at position 10).
     */
    private static String sp(String n) {
        if (n.startsWith("P")) {
            String rest = n.substring(1);
            digits(rest, 12);
            expect(rest, 8, Weighted.weightedSum(rest.substring(0, 8), SP_8) % 11 % 10);
            return n;
        }
        digits(n, 12);
        expect(n, 8, Weighted.weightedSum(n.substring(0, 8), SP_8) % 11 % 10);
        expect(n, 11, Weighted.weightedSum(n.substring(0, 11), SP_11) % 11 % 10);
        return n;
    }

    /**
     * TO: 11 digits, of which positions 3-4 carry the registration type (01,
     * 02, 03 or 99) and are skipped by the check digit, which weighs the
     * remaining digits with 9,8,7,6,5,4,3,2 — or 9 digits, which is the same
     * number with the type left out.
     *
     * <p>The SINTEGRA page documents only the 11-digit form, but Tocantins
     * stopped issuing the type digits and both are in circulation. Nothing
     * else changes: the eight digits the check digit runs over are the same
     * eight, and a 9-digit number carries no type to validate.</p>
     */
    private static String to(String n) {
        digits(n, 9, 11);
        String worked;
        if (n.length() == 11) {
            String type = n.substring(2, 4);
            if (!type.equals("01") && !type.equals("02")
                    && !type.equals("03") && !type.equals("99")) {
                throw new InvalidComponentException(Message.of(InscricaoEstadual.class,
                        "ie.to.registration-type", "Invalid registration type for TO."));
            }
            worked = n.substring(0, 2) + n.substring(4, 10);
        } else {
            worked = n.substring(0, 8);
        }
        expect(n, n.length() - 1, Weighted.mod11CheckDigit(worked, W9_2));
        return n;
    }
}
