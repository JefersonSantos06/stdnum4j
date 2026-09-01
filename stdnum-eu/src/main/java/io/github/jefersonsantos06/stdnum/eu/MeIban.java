package io.github.jefersonsantos06.stdnum.eu;

import io.github.jefersonsantos06.stdnum.international.Iban;
import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidComponentException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;

/**
 * The Montenegrin IBAN. Its account part carries a second mod 97 check of
 * its own, over the eighteen digits after the country and the IBAN check
 * digits.
 */
public final class MeIban implements StdNum {

    public static final MeIban INSTANCE = new MeIban();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("me.iban", "IBAN")
                    .country("ME")
                    .title("Crnogorski medunarodni broj racuna")
                    .description("Montenegrin IBAN: the international number whose account"
                            + " part carries a mod 97 check of its own.")
                    .tags(Tag.BANK)
                    .references("https://en.wikipedia.org/wiki/International_Bank_Account_Number")
                    .build();

    private MeIban() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Iban.INSTANCE.compact(number);
    }

    @Override
    public String validate(String number) {
        String n = Iban.INSTANCE.validate(number, false);
        if (!n.startsWith("ME")) {
            throw new InvalidComponentException("Not a Montenegrin IBAN.");
        }
        if (Long.parseLong(n.substring(4)) % 97 != 1) {
            throw new InvalidChecksumException();
        }
        return n;
    }

    @Override
    public String format(String number) {
        // the generic grouping, but of a number this type accepts
        return Iban.INSTANCE.format(validate(number));
    }
}
