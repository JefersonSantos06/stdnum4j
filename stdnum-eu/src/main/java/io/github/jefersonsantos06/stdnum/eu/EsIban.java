package io.github.jefersonsantos06.stdnum.eu;

import io.github.jefersonsantos06.stdnum.international.Iban;
import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidComponentException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;

/**
 * The Spanish IBAN, whose account part is the {@link EsCcc} it replaced, so
 * the account carries both the IBAN check digits and its own two.
 */
public final class EsIban implements StdNum {

    public static final EsIban INSTANCE = new EsIban();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("es.iban", "IBAN")
                    .country("ES")
                    .title("Numero de cuenta bancaria internacional espanol")
                    .description("Spanish IBAN: the international number whose account part is"
                            + " a CCC, checked as one.")
                    .tags(Tag.BANK)
                    .references("https://en.wikipedia.org/wiki/International_Bank_Account_Number")
                    .build();

    private EsIban() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Iban.INSTANCE.compact(number);
    }

    /** The CCC the IBAN was built around. */
    public static String toCcc(String number) {
        String n = INSTANCE.compact(number);
        if (!n.startsWith("ES")) {
            throw new InvalidComponentException("Not a Spanish IBAN.");
        }
        return n.substring(4);
    }

    @Override
    public String validate(String number) {
        String n = Iban.INSTANCE.validate(number, false);
        EsCcc.INSTANCE.validate(toCcc(n));
        return n;
    }

    @Override
    public String format(String number) {
        // the generic grouping, but of a number this type accepts
        return Iban.INSTANCE.format(validate(number));
    }
}
