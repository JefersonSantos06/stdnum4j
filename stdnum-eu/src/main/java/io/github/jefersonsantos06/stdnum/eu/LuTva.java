package io.github.jefersonsantos06.stdnum.eu;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;


/**
 * TVA (taxe sur la valeur ajoutée), the Luxembourgian VAT number: eight
 * digits whose last two are the first six taken modulo 89.
 */
public final class LuTva implements StdNum {

    public static final LuTva INSTANCE = new LuTva();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("lu.tva", "TVA")
                    .country("LU")
                    .title("Numéro d'identification à la taxe sur la valeur ajoutée")
                    .description("Luxembourgian VAT number: 8 digits with a mod 89 check.")
                    .tags(Tag.VAT)
                    .build();

    private LuTva() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " :.-", "LU");
    }

    /** The two check digits for the six-digit base. */
    public static String calcCheckDigits(String base) {
        return String.format("%02d", Integer.parseInt(base) % 89);
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (!Strings.isDigits(n)) {
            throw new InvalidFormatException();
        }
        if (n.length() != 8) {
            throw new InvalidLengthException();
        }
        if (!n.substring(6).equals(calcCheckDigits(n.substring(0, 6)))) {
            throw new InvalidChecksumException();
        }
        return n;
    }
}
