package io.github.jefersonsantos06.stdnum.eu;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.util.Locale;

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
        String n = Strings.compact(number, " -.").toUpperCase(Locale.ROOT);
        return n.startsWith("BG") ? n.substring(2) : n;
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

    /** The check digit of an "other" ten-digit number, from its first nine digits. */
    public static char calcCheckDigitOther(String base) {
        int sum = 0;
        for (int i = 0; i < OTHER_WEIGHTS.length && i < base.length(); i++) {
            sum += OTHER_WEIGHTS[i] * (base.charAt(i) - '0');
        }
        return (char) ('0' + Math.floorMod(11 - sum, 11) % 10);
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
            boolean valid = BgEgn.INSTANCE.isValid(n)
                    || BgPnf.INSTANCE.isValid(n)
                    || n.charAt(9) == calcCheckDigitOther(n.substring(0, 9));
            if (!valid) {
                throw new InvalidChecksumException();
            }
        } else {
            throw new InvalidLengthException();
        }
        return n;
    }
}
