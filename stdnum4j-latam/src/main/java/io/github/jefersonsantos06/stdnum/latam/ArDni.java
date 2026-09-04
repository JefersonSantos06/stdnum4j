package io.github.jefersonsantos06.stdnum.latam;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

/**
 * DNI (Documento Nacional de Identidad), the Argentinian national identity
 * number: seven or eight digits. The number carries no check digit, so
 * validation is structural only.
 */
public final class ArDni implements StdNum {

    public static final ArDni INSTANCE = new ArDni();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("ar.dni", "DNI")
                    .country("AR")
                    .title("Documento Nacional de Identidad")
                    .description("Argentinian identity number: 7 or 8 digits, with no check"
                            + " digit.")
                    .tags(Tag.PERSON)
                    .build();

    private ArDni() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " .");
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (!Strings.isDigits(n)) {
            throw new InvalidFormatException();
        }
        if (n.length() != 7 && n.length() != 8) {
            throw new InvalidLengthException();
        }
        return n;
    }

    @Override
    public String format(String number) {
        String n = validate(number);
        return n.length() == 8
                ? n.substring(0, 2) + "." + n.substring(2, 5) + "." + n.substring(5)
                : n.charAt(0) + "." + n.substring(1, 4) + "." + n.substring(4);
    }
}
