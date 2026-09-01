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
 * ALV nro (Arvonlisäveronumero), the Finnish VAT number: eight digits
 * weighted 7,9,10,5,8,4,2,1 modulo 11. It is the same number as the
 * business identifier ({@link FiYtunnus}), differently presented.
 */
public final class FiAlv implements StdNum {

    public static final FiAlv INSTANCE = new FiAlv();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("fi.alv", "ALV nro")
                    .country("FI")
                    .title("Arvonlisäveronumero")
                    .description("Finnish VAT number: 8 digits with a weighted mod 11 check.")
                    .tags(Tag.VAT)
                    .build();

    private static final int[] WEIGHTS = {7, 9, 10, 5, 8, 4, 2, 1};

    private FiAlv() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        String n = Strings.compact(number, " -").toUpperCase(Locale.ROOT);
        return n.startsWith("FI") ? n.substring(2) : n;
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (!Strings.isDigits(n)) {
            throw new InvalidFormatException();
        }
        if (n.length() != 8) {
            throw new InvalidLengthException();
        }
        int sum = 0;
        for (int i = 0; i < WEIGHTS.length; i++) {
            sum += WEIGHTS[i] * (n.charAt(i) - '0');
        }
        if (sum % 11 != 0) {
            throw new InvalidChecksumException();
        }
        return n;
    }
}
