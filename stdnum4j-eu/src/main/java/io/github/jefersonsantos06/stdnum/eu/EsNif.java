package io.github.jefersonsantos06.stdnum.eu;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;


/**
 * NIF (Número de Identificación Fiscal), the Spanish tax and VAT number.
 * The number is a DNI (residents), an NIE (foreigners), a special K/L/M
 * personal number, or a CIF (legal entities) — this validator dispatches on
 * the first character. An optional {@code ES} prefix is accepted.
 */
public final class EsNif implements StdNum {

    public static final EsNif INSTANCE = new EsNif();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("es.nif", "NIF")
                    .country("ES")
                    .title("Número de Identificación Fiscal")
                    .description("Spanish tax/VAT number: a DNI, NIE, K/L/M personal number"
                            + " or CIF, dispatched on the first character.")
                    .tags(Tag.TAX, Tag.VAT)
                    .build();

    private EsNif() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " -", "ES");
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (n.length() < 3 || !Strings.isDigits(n.substring(1, n.length() - 1))) {
            throw new InvalidFormatException();
        }
        if (n.length() != 9) {
            throw new InvalidLengthException();
        }
        char first = n.charAt(0);
        if (first == 'K' || first == 'L' || first == 'M') {
            // young residents, residents abroad and foreigners without NIE:
            // seven digits closed by the DNI check letter
            if (n.charAt(8) != EsDni.checkLetter(n.substring(1, 8))) {
                throw new InvalidChecksumException();
            }
        } else if (first >= '0' && first <= '9') {
            EsDni.INSTANCE.validate(n);
        } else if (first == 'X' || first == 'Y' || first == 'Z') {
            EsNie.INSTANCE.validate(n);
        } else {
            EsCif.INSTANCE.validate(n);
        }
        return n;
    }
}
