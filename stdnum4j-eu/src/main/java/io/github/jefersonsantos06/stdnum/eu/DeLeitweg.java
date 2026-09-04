package io.github.jefersonsantos06.stdnum.eu;

import io.github.jefersonsantos06.stdnum.algo.Mod97;
import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidComponentException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.Message;
import io.github.jefersonsantos06.stdnum.spi.Reasons;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;

import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Leitweg-ID, the routing identifier a German public body publishes so that
 * electronic invoices reach it: a coarse address, an optional finer one and
 * two mod 97-10 check digits.
 *
 * <p>The dashes separate those three parts and belong to the identifier, so
 * unlike most numbers here it is not stripped of them.</p>
 */
public final class DeLeitweg implements StdNum {

    public static final DeLeitweg INSTANCE = new DeLeitweg();

    private static final Pattern PATTERN =
            Pattern.compile("[0-9]{2,12}(-[0-9A-Z]{0,30})?-[0-9]{2}");

    /** The sixteen Lander, plus 99 for the federation. */
    private static final Set<String> AUTHORITIES = Set.of(
            "01", "02", "03", "04", "05", "06", "07", "08",
            "09", "10", "11", "12", "13", "14", "15", "16", "99");

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("de.leitweg", "Leitweg-ID")
                    .country("DE")
                    .title("Deutsche Leitweg-ID")
                    .description("German e-invoicing routing identifier: an authority code, an"
                            + " address and two mod 97-10 check digits, separated by dashes.")
                    .tags(Tag.PAYMENT, Tag.OTHER)
                    .references("https://e-rechnung-bund.de/faq/")
                    .build();

    private DeLeitweg() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    /**
     * {@inheritDoc}
     *
     * <p>The dashes belong to the identifier rather than being separators, so
     * only surrounding whitespace is removed.</p>
     */
    @Override
    public String compact(String number) {
        if (number == null) {
            throw new InvalidFormatException(Reasons.nullNumber());
        }
        return number.strip().toUpperCase(Locale.ROOT);
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (n.length() < 5 || n.length() > 46) {
            throw new InvalidLengthException();
        }
        if (!PATTERN.matcher(n).matches()) {
            throw new InvalidFormatException();
        }
        if (!AUTHORITIES.contains(n.substring(0, 2))) {
            throw new InvalidComponentException(Message.of(DeLeitweg.class, "leitweg.authority",
                    "Not the code of a Land or of the federation."));
        }
        Mod97.validate(n.replace("-", ""));
        return n;
    }
}
