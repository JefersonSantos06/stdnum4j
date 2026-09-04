package io.github.jefersonsantos06.stdnum.international;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidComponentException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.Message;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Mask;
import io.github.jefersonsantos06.stdnum.text.Resources;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * ISRC, the International Standard Recording Code: a two-letter prefix, a
 * three-character registrant, the last two digits of the year of reference
 * and a five-digit designation.
 *
 * <p>There is no check digit. The prefix is the one part that can be
 * checked: it is normally an ISO 3166-1 country code, but the agency has
 * also allocated codes outside that list as countries ran out of numbers.</p>
 */
public final class Isrc implements StdNum {

    public static final Isrc INSTANCE = new Isrc();

    private static final Pattern PATTERN =
            Pattern.compile("[A-Z]{2}[A-Z0-9]{3}[0-9]{2}[0-9]{5}");

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("isrc", "ISRC")
                    .title("International Standard Recording Code")
                    .description("Identifier of a sound or music video recording: a prefix, a"
                            + " registrant, a year and a designation.")
                    .tags(Tag.MEDIA, Tag.PRODUCT)
                    .references("https://isrc.ifpi.org/")
                    .build();

    private static final Mask MASK = Mask.of("##-###-##-#####");

    private Isrc() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    /** The prefixes a code may open with; loaded on first use. */
    private static Set<String> prefixes() {
        return Resources.lines(Isrc.class, "isrc-prefixes.txt");
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " -").toUpperCase(Locale.ROOT);
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (n.length() != 12) {
            throw new InvalidLengthException();
        }
        if (!PATTERN.matcher(n).matches()) {
            throw new InvalidFormatException();
        }
        if (!prefixes().contains(n.substring(0, 2))) {
            throw new InvalidComponentException(Message.of(Isrc.class, "isrc.prefix",
                    "Not a prefix the ISRC agency allocates."));
        }
        return n;
    }

    @Override
    public String format(String number) {
        return MASK.fill(validate(number));
    }

    @Override
    public List<Mask> masks() {
        return List.of(MASK);
    }
}
