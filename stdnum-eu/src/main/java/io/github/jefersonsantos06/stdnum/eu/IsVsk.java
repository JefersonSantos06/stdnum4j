package io.github.jefersonsantos06.stdnum.eu;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.util.Locale;

/**
 * VSK, the Icelandic VAT number: five or six digits, with no check digit. It
 * is issued alongside the {@link IsKennitala} of the business.
 */
public final class IsVsk implements StdNum {

    public static final IsVsk INSTANCE = new IsVsk();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("is.vsk", "VSK")
                    .country("IS")
                    .title("Virdisaukaskattur")
                    .description("Icelandic VAT number: 5 or 6 digits.")
                    .tags(Tag.VAT, Tag.COMPANY)
                    .references("https://en.wikipedia.org/wiki/VAT_identification_number")
                    .build();

    private IsVsk() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        String n = Strings.compact(number, " ").toUpperCase(Locale.ROOT);
        return n.startsWith("IS") ? n.substring(2) : n;
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (!Strings.isDigits(n)) {
            throw new InvalidFormatException();
        }
        if (n.length() != 5 && n.length() != 6) {
            throw new InvalidLengthException();
        }
        return n;
    }
}
