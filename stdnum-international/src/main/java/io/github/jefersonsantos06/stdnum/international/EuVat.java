package io.github.jefersonsantos06.stdnum.international;

import io.github.jefersonsantos06.stdnum.StdNums;
import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidComponentException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.Message;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.util.Locale;
import java.util.Set;

/**
 * The VAT number of a trader registered in the European Union.
 *
 * <p>Greece writes EL where its ISO code is GR, Northern Ireland kept XI when
 * the rest of the United Kingdom left, and a trader in one of the One Stop
 * Shop schemes carries an EU or IM number instead of a national one. A plain
 * GB number is no longer an EU VAT number and is refused here, though
 * {@link Vatin} still accepts it.</p>
 *
 * <p>The whole number, prefix included, is handed to the country's own type,
 * which strips the prefix it expects. This is stricter than {@link Vatin},
 * which also tries the number with the prefix cut off and so accepts a
 * doubled prefix such as {@code ATATU65033803}: here that is one prefix too
 * many, and the number is rejected.</p>
 */
public final class EuVat implements StdNum {

    public static final EuVat INSTANCE = new EuVat();

    /** The member states, as the prefixes their VAT and excise numbers open with. */
    public static final Set<String> MEMBER_STATES = Set.of(
            "AT", "BE", "BG", "CY", "CZ", "DE", "DK", "EE", "ES", "FI", "FR", "GR",
            "HR", "HU", "IE", "IT", "LT", "LU", "LV", "MT", "NL", "PL", "PT", "RO",
            "SE", "SI", "SK", "XI");

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("eu.vat", "EU VAT")
                    .title("European Union VAT number")
                    .description("VAT number of a trader registered in an EU member state, or"
                            + " in one of the One Stop Shop schemes.")
                    .tags(Tag.VAT, Tag.TAX)
                    .references("https://en.wikipedia.org/wiki/VAT_identification_number")
                    .build();

    private EuVat() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    /** The type that validates numbers under this prefix. */
    private static StdNum moduleFor(String countryCode) {
        if (countryCode.equals("EU") || countryCode.equals("IM")) {
            return StdNums.byId("eu.oss").orElseThrow(() -> new InvalidComponentException(Message.of(EuVat.class, "vat.oss-missing",
                    "The One Stop Shop numbers are not on the classpath.")));
        }
        // EL is Greece's VAT prefix, but membership is held under its ISO code
        String cc = countryCode.equals("EL") ? "GR" : countryCode;
        if (!MEMBER_STATES.contains(cc)) {
            throw new InvalidComponentException(Message.of(EuVat.class, "eu.member-state",
                    "{0} is not an EU member state.", countryCode));
        }
        // a Northern Irish number is a United Kingdom number
        return Vatin.vatModule(cc.equals("XI") ? "GB" : cc);
    }

    /** The two-letter prefix a number opens with. */
    private static String prefixOf(String n) {
        if (n.length() < 2) {
            throw new InvalidFormatException();
        }
        return n.substring(0, 2);
    }

    /** The prefix, restored if the country's type stripped it. */
    private static String prefixed(String countryCode, String national) {
        return national.startsWith(countryCode) ? national : countryCode + national;
    }

    @Override
    public String compact(String number) {
        String n = Strings.compact(number, "").toUpperCase(Locale.ROOT);
        String cc = prefixOf(n);
        return prefixed(cc, moduleFor(cc).compact(n));
    }

    @Override
    public String validate(String number) {
        String n = Strings.compact(number, "").toUpperCase(Locale.ROOT);
        String cc = prefixOf(n);
        return prefixed(cc, moduleFor(cc).validate(n));
    }
}
