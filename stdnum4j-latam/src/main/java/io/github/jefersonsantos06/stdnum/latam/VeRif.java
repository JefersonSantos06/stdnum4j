package io.github.jefersonsantos06.stdnum.latam;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidComponentException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.Message;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.util.Locale;

/**
 * RIF (Registro de Identificación Fiscal), the Venezuelan fiscal number: a
 * type letter, eight digits and a check digit. The type letter contributes
 * its own value to the checksum.
 */
public final class VeRif implements StdNum {

    public static final VeRif INSTANCE = new VeRif();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("ve.rif", "RIF")
                    .country("VE")
                    .title("Registro de Identificación Fiscal")
                    .description("Venezuelan fiscal number: a type letter, 8 digits and a"
                            + " weighted mod 11 check digit.")
                    .tags(Tag.TAX, Tag.VAT)
                    .build();

    /** Type letters and the value each contributes to the checksum. */
    private static final String TYPES = "VEJPG";
    private static final int[] TYPE_VALUES = {4, 8, 12, 16, 20};
    private static final int[] WEIGHTS = {3, 2, 7, 6, 5, 4, 3, 2};

    private VeRif() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " -").toUpperCase(Locale.ROOT);
    }

    /** The check digit for a number whose first nine characters are known. */
    public static char calcCheckDigit(String base) {
        String b = INSTANCE.compact(base);
        int type = TYPES.indexOf(b.charAt(0));
        if (type < 0) {
            throw new InvalidComponentException(Message.of(VeRif.class, "rif.type",
                    "Unknown RIF type letter."));
        }
        int sum = TYPE_VALUES[type];
        for (int i = 0; i < WEIGHTS.length; i++) {
            sum += WEIGHTS[i] * (b.charAt(i + 1) - '0');
        }
        return "00987654321".charAt(sum % 11);
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (n.length() != 10) {
            throw new InvalidLengthException();
        }
        if (TYPES.indexOf(n.charAt(0)) < 0) {
            throw new InvalidComponentException(Message.of(VeRif.class, "rif.type",
                    "Unknown RIF type letter."));
        }
        if (!Strings.isDigits(n.substring(1))) {
            throw new InvalidFormatException();
        }
        if (n.charAt(9) != calcCheckDigit(n)) {
            throw new InvalidChecksumException();
        }
        return n;
    }
}
