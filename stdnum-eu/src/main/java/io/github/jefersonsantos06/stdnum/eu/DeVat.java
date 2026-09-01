package io.github.jefersonsantos06.stdnum.eu;

import io.github.jefersonsantos06.stdnum.algo.Iso7064;
import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.util.Locale;

/**
 * USt-IdNr. (Umsatzsteuer-Identifikationsnummer), the German VAT number:
 * nine digits not starting with zero, closed by an ISO 7064 MOD 11,10
 * check digit. An optional {@code DE} prefix is accepted and stripped.
 */
public final class DeVat implements StdNum {

    public static final DeVat INSTANCE = new DeVat();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("de.vat", "USt-IdNr")
                    .country("DE")
                    .title("Umsatzsteuer-Identifikationsnummer")
                    .description("German VAT number: 9 digits with an ISO 7064 MOD 11,10"
                            + " check digit.")
                    .tags(Tag.VAT)
                    .build();

    private DeVat() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        String n = Strings.compact(number, " -./,").toUpperCase(Locale.ROOT);
        return n.startsWith("DE") ? n.substring(2) : n;
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (!Strings.isDigits(n) || n.charAt(0) == '0') {
            throw new InvalidFormatException();
        }
        if (n.length() != 9) {
            throw new InvalidLengthException();
        }
        Iso7064.MOD_11_10.validate(n);
        return n;
    }
}
