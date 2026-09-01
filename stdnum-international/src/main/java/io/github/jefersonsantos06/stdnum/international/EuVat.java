package io.github.jefersonsantos06.stdnum.international;

import io.github.jefersonsantos06.stdnum.StdNums;
import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidComponentException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.util.Locale;
import java.util.Set;

/**
 * The VAT number of a trader registered in the European Union: the same
 * dispatch as {@link Vatin}, but confined to the member states.
 *
 * <p>Greece writes EL where its ISO code is GR, Northern Ireland kept XI when
 * the rest of the United Kingdom left, and a trader in one of the One Stop
 * Shop schemes carries an EU or IM number instead of a national one. A plain
 * GB number is no longer an EU VAT number and is refused here, though
 * {@link Vatin} still accepts it.</p>
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
                    .references("https://ec.europa.eu/taxation_customs/vies/")
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
            return StdNums.byId("eu.oss").orElseThrow(() -> new InvalidComponentException(
                    "The One Stop Shop numbers are not on the classpath."));
        }
        // EL is Greece's VAT prefix; Vatin maps it, but membership is checked first
        String cc = countryCode.equals("EL") ? "GR" : countryCode;
        if (!MEMBER_STATES.contains(cc)) {
            throw new InvalidComponentException(countryCode + " is not an EU member state.");
        }
        return Vatin.INSTANCE;
    }

    /** The two-letter prefix a number opens with. */
    private static String prefixOf(String n) {
        if (n.length() < 2) {
            throw new InvalidFormatException();
        }
        return n.substring(0, 2);
    }

    @Override
    public String compact(String number) {
        String n = Strings.compact(number, "").toUpperCase(Locale.ROOT);
        return moduleFor(prefixOf(n)).compact(n);
    }

    @Override
    public String validate(String number) {
        String n = Strings.compact(number, "").toUpperCase(Locale.ROOT);
        return moduleFor(prefixOf(n)).validate(n);
    }
}
