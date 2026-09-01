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
 * ANUM (Közösségi adószám), the Hungarian VAT number: eight digits weighted
 * 9,7,3,1,9,7,3,1 modulo 10.
 */
public final class HuAnum implements StdNum {

    public static final HuAnum INSTANCE = new HuAnum();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("hu.anum", "ANUM")
                    .country("HU")
                    .title("Közösségi adószám")
                    .description("Hungarian VAT number: 8 digits with a weighted mod 10 check.")
                    .tags(Tag.VAT)
                    .build();

    private static final int[] WEIGHTS = {9, 7, 3, 1, 9, 7, 3, 1};

    private HuAnum() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        String n = Strings.compact(number, " -").toUpperCase(Locale.ROOT);
        return n.startsWith("HU") ? n.substring(2) : n;
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
        if (sum % 10 != 0) {
            throw new InvalidChecksumException();
        }
        return n;
    }
}
