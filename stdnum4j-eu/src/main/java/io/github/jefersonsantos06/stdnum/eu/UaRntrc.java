package io.github.jefersonsantos06.stdnum.eu;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

/**
 * RNTRC, the Ukrainian individual taxpayer number: ten digits whose first
 * five count the days since the turn of 1900 and whose last is a weighted
 * mod 11 check digit. It is the personal counterpart of the company
 * {@link UaEdrpou}.
 */
public final class UaRntrc implements StdNum {

    public static final UaRntrc INSTANCE = new UaRntrc();

    private static final int[] WEIGHTS = {-1, 5, 7, 9, 4, 6, 10, 5, 7};

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("ua.rntrc", "RNTRC")
                    .country("UA")
                    .title("Reyestratsiynyi nomer oblikovoyi kartky platnyka podatkiv")
                    .description("Ukrainian individual taxpayer number: 10 digits with a"
                            + " weighted mod 11 check digit.")
                    .tags(Tag.PERSON, Tag.TAX)
                    .references("https://en.wikipedia.org/wiki/National_identification_number#Ukraine")
                    .build();

    private UaRntrc() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " ");
    }

    /** The check digit of a number, from its first nine digits. */
    public static char calcCheckDigit(String number) {
        String n = INSTANCE.compact(number);
        int total = 0;
        for (int i = 0; i < WEIGHTS.length && i < n.length(); i++) {
            total += WEIGHTS[i] * (n.charAt(i) - '0');
        }
        return (char) ('0' + Math.floorMod(total, 11) % 10);
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (n.length() != 10) {
            throw new InvalidLengthException();
        }
        if (!Strings.isDigits(n)) {
            throw new InvalidFormatException();
        }
        if (n.charAt(9) != calcCheckDigit(n)) {
            throw new InvalidChecksumException();
        }
        return n;
    }
}
