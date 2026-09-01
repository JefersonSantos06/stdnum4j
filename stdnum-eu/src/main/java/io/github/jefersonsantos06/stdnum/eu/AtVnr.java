package io.github.jefersonsantos06.stdnum.eu;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

/**
 * Versicherungsnummer, the Austrian social insurance number: ten digits
 * whose last six encode the date of birth. The check digit sits in fourth
 * position, which is why its own weight in the sum is zero.
 */
public final class AtVnr implements StdNum {

    public static final AtVnr INSTANCE = new AtVnr();

    private static final int[] WEIGHTS = {3, 7, 9, 0, 5, 8, 4, 2, 1, 6};

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("at.vnr", "VNR")
                    .country("AT")
                    .title("Osterreichische Sozialversicherungsnummer")
                    .description("Austrian social insurance number: 10 digits with a weighted"
                            + " mod 11 check digit in fourth position.")
                    .tags(Tag.PERSON, Tag.HEALTH)
                    .references("https://en.wikipedia.org/wiki/National_identification_number#Austria")
                    .build();

    private AtVnr() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.clean(number, " ");
    }

    /**
     * The check digit belonging in fourth position.
     *
     * @throws InvalidChecksumException when the weighted sum leaves a
     *                                  remainder of 10, for which no check
     *                                  digit exists
     */
    public static char calcCheckDigit(String number) {
        String n = INSTANCE.compact(number);
        int sum = 0;
        for (int i = 0; i < WEIGHTS.length && i < n.length(); i++) {
            sum += WEIGHTS[i] * (n.charAt(i) - '0');
        }
        int check = sum % 11;
        if (check == 10) {
            throw new InvalidChecksumException("No valid check digit exists for this number.");
        }
        return (char) ('0' + check);
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (!Strings.isDigits(n) || n.charAt(0) == '0') {
            throw new InvalidFormatException();
        }
        if (n.length() != 10) {
            throw new InvalidLengthException();
        }
        if (n.charAt(3) != calcCheckDigit(n)) {
            throw new InvalidChecksumException();
        }
        return n;
    }
}
