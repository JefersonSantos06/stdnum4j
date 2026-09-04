package io.github.jefersonsantos06.stdnum.apac;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Mask;
import io.github.jefersonsantos06.stdnum.text.Strings;

/**
 * The Japanese Individual Number, known as My Number: twelve digits with a
 * weighted mod 11 check digit at the end. It is the personal counterpart of
 * the corporate {@link JpCn}.
 */
public final class JpIn implements StdNum {

    public static final JpIn INSTANCE = new JpIn();

    private static final int[] WEIGHTS = {6, 5, 4, 3, 2, 7, 6, 5, 4, 3, 2};

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("jp.in", "My Number")
                    .country("JP")
                    .title("Japanese Individual Number")
                    .description("Japanese personal identification number: 12 digits with a"
                            + " weighted mod 11 check digit.")
                    .tags(Tag.PERSON, Tag.TAX)
                    .references("https://en.wikipedia.org/wiki/Individual_Number")
                    .build();

    private static final Mask MASK = Mask.of("#### #### ####");

    private JpIn() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, "- ");
    }

    /** The check digit of a number, from its first eleven digits. */
    public static char calcCheckDigit(String base) {
        String n = INSTANCE.compact(base);
        int sum = 0;
        for (int i = 0; i < WEIGHTS.length && i < n.length(); i++) {
            sum += WEIGHTS[i] * (n.charAt(i) - '0');
        }
        return (char) ('0' + Math.floorMod(-sum, 11) % 10);
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (n.length() != 12) {
            throw new InvalidLengthException();
        }
        if (!Strings.isDigits(n)) {
            throw new InvalidFormatException();
        }
        if (n.charAt(11) != calcCheckDigit(n.substring(0, 11))) {
            throw new InvalidChecksumException();
        }
        return n;
    }

    @Override
    public String format(String number) {
        return MASK.fill(validate(number));
    }
}
