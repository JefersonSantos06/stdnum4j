package io.github.jefersonsantos06.stdnum.eu;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidComponentException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.Message;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

/**
 * Numero fiscal de reference, the French personal tax number: thirteen digits
 * opening with 0, 1, 2 or 3, the last three being the first ten taken modulo
 * 511. It is also known as the numero SPI.
 */
public final class FrNif implements StdNum {

    public static final FrNif INSTANCE = new FrNif();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("fr.nif", "NIF")
                    .country("FR")
                    .title("Numero fiscal de reference")
                    .description("French personal tax number: 13 digits whose last three are"
                            + " the first ten modulo 511.")
                    .tags(Tag.PERSON, Tag.TAX)
                    .references("https://www.oecd.org/content/dam/oecd/en/topics/policy-issue-focus/aeoi/"
                            + "france-tin.pdf")
                    .build();

    private FrNif() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " ");
    }

    /** The three check digits of a number, from its first ten digits. */
    public static String calcCheckDigits(String number) {
        String n = INSTANCE.compact(number);
        long head = Long.parseLong(n.substring(0, 10));
        return String.format("%03d", head % 511);
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (!Strings.isDigits(n)) {
            throw new InvalidFormatException();
        }
        if ("0123".indexOf(n.charAt(0)) < 0) {
            throw new InvalidComponentException(Message.of(FrNif.class, "nif.prefix",
                    "French tax numbers start with 0, 1, 2 or 3."));
        }
        if (n.length() != 13) {
            throw new InvalidLengthException();
        }
        if (!calcCheckDigits(n).equals(n.substring(10))) {
            throw new InvalidChecksumException();
        }
        return n;
    }

    @Override
    public String format(String number) {
        String n = validate(number);
        return n.substring(0, 2) + ' ' + n.substring(2, 4) + ' ' + n.substring(4, 7)
                + ' ' + n.substring(7, 10) + ' ' + n.substring(10);
    }
}
