package io.github.jefersonsantos06.stdnum.eu;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.Reasons;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;


/**
 * ДДС номер, the Bulgarian VAT number: nine digits for legal entities, or
 * ten for physical persons, foreigners and others — in which case an
 * {@link BgEgn} or {@link BgPnf} number is also accepted.
 */
public final class BgVat implements StdNum {

    public static final BgVat INSTANCE = new BgVat();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("bg.vat", "ДДС")
                    .country("BG")
                    .title("Идентификационен номер по ДДС")
                    .description("Bulgarian VAT number: 9 digits for legal entities or 10 for"
                            + " persons, each with its own check digit rule.")
                    .tags(Tag.VAT)
                    .build();

    private static final int[] OTHER_WEIGHTS = {4, 3, 2, 7, 6, 5, 4, 3, 2};

    private BgVat() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " -.", "BG");
    }

    /** The check digit of a legal entity number, from its first eight digits. */
    public static char calcCheckDigitLegal(String base) {
        int check = 0;
        for (int i = 0; i < base.length(); i++) {
            check += (i + 1) * (base.charAt(i) - '0');
        }
        check %= 11;
        if (check == 10) {
            check = 0;
            for (int i = 0; i < base.length(); i++) {
                check += (i + 3) * (base.charAt(i) - '0');
            }
            check %= 11;
        }
        return (char) ('0' + check % 10);
    }

    /**
     * The check digit of an "other" ten-digit number, from its first nine
     * digits.
     *
     * <p>The rule is {@code (11 - sum) mod 11}, which can yield 10 — a value
     * with no single-digit representation. Such a base can never produce a
     * valid number, so this throws instead of folding 10 down to 0, which
     * would let numbers through that the rule rejects.</p>
     */
    public static char calcCheckDigitOther(String base) {
        int sum = 0;
        for (int i = 0; i < OTHER_WEIGHTS.length && i < base.length(); i++) {
            sum += OTHER_WEIGHTS[i] * (base.charAt(i) - '0');
        }
        int check = Math.floorMod(11 - sum, 11);
        if (check == 10) {
            throw new InvalidChecksumException(Reasons.noCheckDigit());
        }
        return (char) ('0' + check);
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (!Strings.isDigits(n)) {
            throw new InvalidFormatException();
        }
        if (n.length() == 9) {
            if (n.charAt(8) != calcCheckDigitLegal(n.substring(0, 8))) {
                throw new InvalidChecksumException();
            }
        } else if (n.length() == 10) {
            if (!BgEgn.INSTANCE.isValid(n) && !BgPnf.INSTANCE.isValid(n)
                    && !matchesOtherCheckDigit(n)) {
                throw new InvalidChecksumException();
            }
        } else {
            throw new InvalidLengthException();
        }
        return n;
    }

    /** Whether the "other" rule yields a check digit and it matches. */
    private static boolean matchesOtherCheckDigit(String n) {
        try {
            return n.charAt(9) == calcCheckDigitOther(n.substring(0, 9));
        } catch (InvalidChecksumException e) {
            // the base has no representable check digit, so it cannot match
            return false;
        }
    }
}
