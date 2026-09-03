package io.github.jefersonsantos06.stdnum.eu;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;


/**
 * KMKR (Käibemaksukohuslase number), the Estonian VAT number: nine digits
 * weighted 3,7,1 repeating, modulo 10.
 */
public final class EeKmkr implements StdNum {

    public static final EeKmkr INSTANCE = new EeKmkr();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("ee.kmkr", "KMKR")
                    .country("EE")
                    .title("Käibemaksukohuslase number")
                    .description("Estonian VAT number: 9 digits with a weighted mod 10 check.")
                    .tags(Tag.VAT)
                    .build();

    private static final int[] WEIGHTS = {3, 7, 1, 3, 7, 1, 3, 7, 1};

    private EeKmkr() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " ", "EE");
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (!Strings.isDigits(n)) {
            throw new InvalidFormatException();
        }
        if (n.length() != 9) {
            throw new InvalidLengthException();
        }
        int sum = 0;
        for (int i = 0; i < WEIGHTS.length; i++) {
            sum += WEIGHTS[i] * (n.charAt(i) - '0');
        }
        if (sum % 10 != 0) {
            throw new InvalidChecksumException();
        }
        return n;
    }
}
