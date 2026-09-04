package io.github.jefersonsantos06.stdnum.eu;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Mask;
import io.github.jefersonsantos06.stdnum.text.Strings;

/**
 * The NHS number, which identifies a patient to the health services of
 * England, Wales and the Isle of Man: ten digits weighted 10 down to 1, the
 * whole of which must be a multiple of 11.
 */
public final class GbNhs implements StdNum {

    public static final GbNhs INSTANCE = new GbNhs();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("gb.nhs", "NHS number")
                    .country("GB")
                    .title("United Kingdom National Health Service number")
                    .description("NHS patient number: 10 digits whose descending weighted sum"
                            + " is a multiple of 11.")
                    .tags(Tag.PERSON, Tag.HEALTH)
                    .references("https://en.wikipedia.org/wiki/NHS_number")
                    .build();

    private static final Mask MASK = Mask.of("### ### ####");

    private GbNhs() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " -");
    }

    /** The weighted sum modulo 11; a valid number yields 0. */
    public static int checksum(String number) {
        String n = INSTANCE.compact(number);
        int sum = 0;
        for (int i = 0; i < n.length(); i++) {
            sum += (i + 1) * (n.charAt(n.length() - 1 - i) - '0');
        }
        return sum % 11;
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
        if (checksum(n) != 0) {
            throw new InvalidChecksumException();
        }
        return n;
    }

    @Override
    public String format(String number) {
        return MASK.fill(validate(number));
    }
}
