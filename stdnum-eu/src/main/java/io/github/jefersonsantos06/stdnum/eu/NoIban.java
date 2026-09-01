package io.github.jefersonsantos06.stdnum.eu;

import io.github.jefersonsantos06.stdnum.international.Iban;
import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidComponentException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;

/**
 * The Norwegian IBAN, whose account part is the {@link NoKontonr} with its
 * own weighted mod 11 check digit.
 */
public final class NoIban implements StdNum {

    public static final NoIban INSTANCE = new NoIban();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("no.iban", "IBAN")
                    .country("NO")
                    .title("Norsk internasjonalt kontonummer")
                    .description("Norwegian IBAN: the international number whose account part"
                            + " is a kontonummer, checked as one.")
                    .tags(Tag.BANK)
                    .references("https://en.wikipedia.org/wiki/International_Bank_Account_Number")
                    .build();

    private NoIban() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Iban.INSTANCE.compact(number);
    }

    /** The account number the IBAN was built around. */
    public static String toKontonr(String number) {
        String n = INSTANCE.compact(number);
        if (!n.startsWith("NO")) {
            throw new InvalidComponentException("Not a Norwegian IBAN.");
        }
        return n.substring(4);
    }

    @Override
    public String validate(String number) {
        String n = Iban.INSTANCE.validate(number, false);
        NoKontonr.INSTANCE.validate(toKontonr(n));
        return n;
    }

    @Override
    public String format(String number) {
        return Iban.INSTANCE.format(number);
    }
}
