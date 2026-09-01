package io.github.jefersonsantos06.stdnum.eu;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.util.Locale;

/**
 * NIE (Número de Identificación de Extranjero), the Spanish foreigner
 * number: X, Y or Z followed by seven digits and the DNI check letter
 * (computed with the leading letter replaced by 0, 1 or 2).
 */
public final class EsNie implements StdNum {

    public static final EsNie INSTANCE = new EsNie();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("es.nie", "NIE")
                    .country("ES")
                    .title("Número de Identificación de Extranjero")
                    .description("Spanish foreigner identity number: X/Y/Z, 7 digits and"
                            + " the DNI mod 23 check letter.")
                    .tags(Tag.PERSON)
                    .build();

    private EsNie() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " -").toUpperCase(Locale.ROOT);
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (n.length() < 3 || "XYZ".indexOf(n.charAt(0)) < 0
                || !Strings.isDigits(n.substring(1, n.length() - 1))) {
            throw new InvalidFormatException();
        }
        if (n.length() != 9) {
            throw new InvalidLengthException();
        }
        String digits = "XYZ".indexOf(n.charAt(0)) + n.substring(1, 8);
        if (n.charAt(8) != EsDni.checkLetter(digits)) {
            throw new InvalidChecksumException();
        }
        return n;
    }
}
