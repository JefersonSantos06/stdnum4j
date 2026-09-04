package io.github.jefersonsantos06.stdnum.eu;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.Message;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Mask;
import io.github.jefersonsantos06.stdnum.text.Strings;

/**
 * BSN (Burgerservicenummer), the Dutch citizen identification number: nine
 * digits (leading zeros are commonly omitted and restored here) closed by
 * the "elfproef" checksum, where the last digit is subtracted rather than
 * added.
 */
public final class NlBsn implements StdNum {

    public static final NlBsn INSTANCE = new NlBsn();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("nl.bsn", "BSN")
                    .country("NL")
                    .title("Burgerservicenummer")
                    .description("Dutch citizen identification number: 9 digits with the"
                            + " elfproef checksum.")
                    .tags(Tag.PERSON)
                    .build();

    private static final Mask MASK = Mask.of("####.##.###");

    private NlBsn() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        String n = Strings.compact(number, " -.");
        return n.length() < 9 && Strings.isDigits(n) ? "0".repeat(9 - n.length()) + n : n;
    }

    /** The elfproef checksum; valid numbers yield 0. */
    static int checksum(String number) {
        int sum = 0;
        for (int i = 0; i < 8; i++) {
            sum += (9 - i) * (number.charAt(i) - '0');
        }
        return Math.floorMod(sum - (number.charAt(8) - '0'), 11);
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
        if (Long.parseLong(n) <= 0) {
            throw new InvalidFormatException(Message.of(NlBsn.class, "bsn.positive",
                    "A BSN must be greater than zero."));
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
