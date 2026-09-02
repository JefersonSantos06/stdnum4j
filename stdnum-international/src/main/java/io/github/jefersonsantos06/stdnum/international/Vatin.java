package io.github.jefersonsantos06.stdnum.international;

import io.github.jefersonsantos06.stdnum.StdNums;
import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidComponentException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.Message;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.spi.ValidationException;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * VATIN (VAT identification number): an ISO country code followed by the
 * country-specific VAT number, dispatched through the registry to whatever
 * country modules are on the classpath.
 *
 * <p>This is the piece that replaces python-stdnum's dynamic imports. The
 * country prefix resolves to a registry entry in two steps: the id
 * {@code <cc>.vat} if it exists, otherwise the country's unique number
 * tagged {@link Tag#VAT} (which is how countries whose VAT number has a
 * local name — {@code fr.tva}, {@code it.iva}, {@code pt.nif} — are found
 * without a hard-coded table). An absent or ambiguous module means an
 * unsupported country ({@code INVALID_COMPONENT}) rather than a classpath
 * error. Greece's {@code EL} prefix and Northern Ireland's {@code XI} are
 * mapped to GR and GB.</p>
 */
public final class Vatin implements StdNum {

    public static final Vatin INSTANCE = new Vatin();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("vatin", "VATIN")
                    .title("VAT identification number")
                    .description("International VAT number: country prefix plus the national"
                            + " VAT number, delegated to the country module on the classpath.")
                    .tags(Tag.VAT)
                    .references("https://en.wikipedia.org/wiki/VAT_identification_number")
                    .build();

    private Vatin() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    /**
     * The type that validates a country's VAT number: the id {@code <cc>.vat}
     * if there is one, otherwise the country's single type tagged
     * {@link Tag#VAT}. Shared with {@link EuVat}, which resolves the country
     * differently but looks the module up the same way.
     *
     * @throws InvalidComponentException if the country has no such type, or
     *                                   has more than one
     */
    static StdNum vatModule(String countryCode) {
        String cc = countryCode.toLowerCase(Locale.ROOT);
        Optional<StdNum> byId = StdNums.byId(cc + ".vat");
        if (byId.isPresent()) {
            return byId.get();
        }
        List<StdNum> tagged = StdNums.byCountry(cc).stream()
                .filter(n -> n.descriptor().tags().contains(Tag.VAT))
                .toList();
        if (tagged.size() != 1) {
            throw new InvalidComponentException(Message.of(Vatin.class, "vatin.module",
                    "No VAT validator registered for country {0}.", countryCode));
        }
        return tagged.get(0);
    }

    private static StdNum moduleFor(String countryCode) {
        return vatModule(countryCode.toLowerCase(Locale.ROOT)
                .replace("el", "gr")   // Greece uses EL as its VAT prefix
                .replace("xi", "gb")); // Northern Ireland after Brexit
    }

    private static String countryOf(String cleaned) {
        if (cleaned.length() < 2) {
            throw new InvalidFormatException();
        }
        String cc = cleaned.substring(0, 2).toUpperCase(Locale.ROOT);
        for (int i = 0; i < 2; i++) {
            char c = cc.charAt(i);
            if (c < 'A' || c > 'Z') {
                throw new InvalidFormatException(Message.of(Vatin.class, "vatin.country-prefix",
                        "A VATIN must start with a two-letter country code."));
            }
        }
        return cc;
    }

    @Override
    public String compact(String number) {
        String n = Strings.compact(number, "");
        String cc = countryOf(n);
        StdNum module = moduleFor(cc);
        try {
            return cc + module.compact(n.substring(2));
        } catch (ValidationException e) {
            return module.compact(n);
        }
    }

    @Override
    public String validate(String number) {
        String n = Strings.compact(number, "");
        String cc = countryOf(n);
        StdNum module = moduleFor(cc);
        try {
            return cc + module.validate(n.substring(2));
        } catch (ValidationException e) {
            // Some national numbers carry the country code inside the number
            // itself (the Swiss CHE prefix, for one), so the prefix must not
            // be stripped: hand the whole string to the country module.
            return module.validate(n);
        }
    }
}
