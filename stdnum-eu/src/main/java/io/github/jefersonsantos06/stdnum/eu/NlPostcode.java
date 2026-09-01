package io.github.jefersonsantos06.stdnum.eu;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidComponentException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * The Dutch postcode: four digits and two letters.
 *
 * <p>Three letter pairs are never issued — SA, SD and SS, for what they
 * stood for under the occupation.</p>
 */
public final class NlPostcode implements StdNum {

    public static final NlPostcode INSTANCE = new NlPostcode();

    private static final Pattern PATTERN = Pattern.compile("[1-9][0-9]{3}[A-Z]{2}");
    private static final Set<String> NEVER_ISSUED = Set.of("SA", "SD", "SS");

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("nl.postcode", "Postcode")
                    .country("NL")
                    .title("Nederlandse postcode")
                    .description("Dutch postcode: 4 digits and 2 letters.")
                    .tags(Tag.POSTAL)
                    .references("https://nl.wikipedia.org/wiki/Postcodes_in_Nederland")
                    .build();

    private NlPostcode() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        String n = Strings.compact(number, " -").toUpperCase(Locale.ROOT);
        return n.startsWith("NL") ? n.substring(2) : n;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Returns the compact form, as every type here does; the space the
     * postcode is written with belongs to {@link #format(String)}.</p>
     */
    @Override
    public String validate(String number) {
        String n = compact(number);
        if (!PATTERN.matcher(n).matches()) {
            throw new InvalidFormatException();
        }
        if (NEVER_ISSUED.contains(n.substring(4))) {
            throw new InvalidComponentException("This letter pair is never issued.");
        }
        return n;
    }

    @Override
    public String format(String number) {
        String n = validate(number);
        return n.substring(0, 4) + ' ' + n.substring(4);
    }
}
