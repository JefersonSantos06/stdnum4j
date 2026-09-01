package io.github.jefersonsantos06.stdnum.international;

import io.github.jefersonsantos06.stdnum.StdNums;
import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidComponentException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.util.Locale;
import java.util.Map;

/**
 * VATIN (VAT identification number): an ISO country code followed by the
 * country-specific VAT number, dispatched through the registry to whatever
 * country modules are on the classpath.
 *
 * <p>This is the piece that replaces python-stdnum's dynamic imports: the
 * country prefix resolves to a registry id ({@code "de.vat"} by default,
 * with explicit aliases where the national VAT number has its own name,
 * such as {@code fr.tva} or the Brazilian CNPJ), and an absent module means
 * an unsupported country ({@code INVALID_COMPONENT}) rather than a
 * classpath error. Greece's {@code EL} prefix and Northern Ireland's
 * {@code XI} are mapped to GR and GB.</p>
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

    /**
     * Registry ids of national VAT numbers whose id is not {@code <cc>.vat}.
     */
    private static final Map<String, String> VAT_IDS = Map.of(
            "fr", "fr.tva",
            "es", "es.nif",
            "it", "it.iva",
            "pt", "pt.nif",
            "br", "br.cnpj",
            "ar", "ar.cuit",
            "cl", "cl.rut");

    private Vatin() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    private static StdNum moduleFor(String countryCode) {
        String cc = countryCode.toLowerCase(Locale.ROOT)
                .replace("el", "gr")   // Greece uses EL as its VAT prefix
                .replace("xi", "gb");  // Northern Ireland after Brexit
        return StdNums.byId(VAT_IDS.getOrDefault(cc, cc + ".vat"))
                .orElseThrow(() -> new InvalidComponentException(
                        "No VAT validator registered for country " + countryCode + "."));
    }

    private static String countryOf(String cleaned) {
        if (cleaned.length() < 2) {
            throw new InvalidFormatException();
        }
        String cc = cleaned.substring(0, 2).toUpperCase(Locale.ROOT);
        for (int i = 0; i < 2; i++) {
            char c = cc.charAt(i);
            if (c < 'A' || c > 'Z') {
                throw new InvalidFormatException(
                        "A VATIN must start with a two-letter country code.");
            }
        }
        return cc;
    }

    @Override
    public String compact(String number) {
        String n = Strings.compact(number, "");
        String cc = countryOf(n);
        return cc + moduleFor(cc).compact(n.substring(2));
    }

    @Override
    public String validate(String number) {
        String n = Strings.compact(number, "");
        String cc = countryOf(n);
        return cc + moduleFor(cc).validate(n.substring(2));
    }
}
