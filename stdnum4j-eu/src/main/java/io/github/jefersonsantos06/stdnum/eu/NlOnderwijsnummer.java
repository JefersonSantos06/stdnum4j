package io.github.jefersonsantos06.stdnum.eu;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

/**
 * The Dutch onderwijsnummer, issued to a pupil who has no {@link NlBsn}. It
 * is built like one and shares its elfproef, but always opens with 10 and
 * leaves a remainder of 5 rather than 0, so the two can never collide.
 */
public final class NlOnderwijsnummer implements StdNum {

    public static final NlOnderwijsnummer INSTANCE = new NlOnderwijsnummer();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("nl.onderwijsnummer", "Onderwijsnummer")
                    .country("NL")
                    .title("Nederlands onderwijsnummer")
                    .description("Dutch pupil number for those without a citizen number:"
                            + " 9 digits starting with 10, on a shifted elfproef.")
                    .tags(Tag.PERSON, Tag.EDUCATION)
                    .references("https://nl.wikipedia.org/wiki/Onderwijsnummer")
                    .build();

    private NlOnderwijsnummer() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return NlBsn.INSTANCE.compact(number);
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (!Strings.isDigits(n) || n.chars().allMatch(c -> c == '0') || !n.startsWith("10")) {
            throw new InvalidFormatException();
        }
        if (n.length() != 9) {
            throw new InvalidLengthException();
        }
        if (NlBsn.checksum(n) != 5) {
            throw new InvalidChecksumException();
        }
        return n;
    }
}
