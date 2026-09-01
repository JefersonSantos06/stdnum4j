package io.github.jefersonsantos06.stdnum.eu;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

/**
 * ЛНЧ (Личен номер на чужденец), the Bulgarian personal number of a
 * foreigner: ten digits weighted 21,19,17,13,11,9,7,3,1 modulo 10.
 */
public final class BgPnf implements StdNum {

    public static final BgPnf INSTANCE = new BgPnf();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("bg.pnf", "ЛНЧ")
                    .country("BG")
                    .title("Личен номер на чужденец")
                    .description("Bulgarian personal number of a foreigner: 10 digits with a"
                            + " weighted mod 10 check digit.")
                    .tags(Tag.PERSON)
                    .build();

    private static final int[] WEIGHTS = {21, 19, 17, 13, 11, 9, 7, 3, 1};

    private BgPnf() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " -.");
    }

    /** The check digit for the nine-digit base. */
    public static char calcCheckDigit(String base) {
        int sum = 0;
        for (int i = 0; i < WEIGHTS.length && i < base.length(); i++) {
            sum += WEIGHTS[i] * (base.charAt(i) - '0');
        }
        return (char) ('0' + sum % 10);
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (!Strings.isDigits(n)) {
            throw new InvalidFormatException();
        }
        if (n.length() != 10) {
            throw new InvalidLengthException();
        }
        if (n.charAt(9) != calcCheckDigit(n.substring(0, 9))) {
            throw new InvalidChecksumException();
        }
        return n;
    }
}
