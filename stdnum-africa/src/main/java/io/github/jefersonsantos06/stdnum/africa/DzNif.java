package io.github.jefersonsantos06.stdnum.africa;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

/**
 * NIF (Numéro d'Identification Fiscale), the Algerian tax number in use
 * since 2006: fifteen digits, or twenty when it identifies a branch or a
 * secondary establishment. The number carries no check digit.
 */
public final class DzNif implements StdNum {

    public static final DzNif INSTANCE = new DzNif();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("dz.nif", "NIF")
                    .country("DZ")
                    .title("Numéro d'Identification Fiscale")
                    .description("Algerian tax number: 15 digits, or 20 for a branch, with no"
                            + " check digit.")
                    .tags(Tag.TAX, Tag.VAT)
                    .build();

    private DzNif() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " ");
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (n.length() != 15 && n.length() != 20) {
            throw new InvalidLengthException();
        }
        if (!Strings.isDigits(n)) {
            throw new InvalidFormatException();
        }
        return n;
    }
}
