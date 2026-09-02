package io.github.jefersonsantos06.stdnum.eu;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidComponentException;
import io.github.jefersonsantos06.stdnum.spi.Message;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;

/**
 * The Monegasque VAT number. Monaco is inside the French VAT territory, so
 * its numbers are French ones ({@link FrTva}) whose SIREN begins with 000,
 * and they are issued and quoted with the FR prefix.
 *
 * <p>For that reason this type is deliberately not tagged {@link Tag#VAT}:
 * there is no MC VAT prefix for the {@code vatin} dispatcher to resolve.</p>
 */
public final class McTva implements StdNum {

    public static final McTva INSTANCE = new McTva();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("mc.tva", "TVA")
                    .country("MC")
                    .title("Numero d'identification a la taxe sur la valeur ajoutee")
                    .description("Monegasque VAT number: a French VAT number whose SIREN"
                            + " starts with 000, carried under the FR prefix.")
                    .tags(Tag.COMPANY, Tag.TAX)
                    .references("https://en.wikipedia.org/wiki/VAT_identification_number")
                    .build();

    private McTva() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return "FR" + FrTva.INSTANCE.compact(number);
    }

    @Override
    public String validate(String number) {
        String n = FrTva.INSTANCE.validate(number);
        if (!n.startsWith("000", 2)) {
            throw new InvalidComponentException(Message.of(McTva.class, "tva.mc.siren-prefix",
                    "Monegasque VAT numbers carry a 000 SIREN prefix."));
        }
        return "FR" + n;
    }
}
