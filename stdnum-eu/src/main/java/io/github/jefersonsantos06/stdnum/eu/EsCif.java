package io.github.jefersonsantos06.stdnum.eu;

import io.github.jefersonsantos06.stdnum.algo.Luhn;
import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.util.Locale;

/**
 * CIF (Código de Identificación Fiscal), the Spanish tax number for legal
 * entities: an organisation-type letter, two province digits, a five-digit
 * sequence and a check digit that may be numeric or alphabetic. Sources
 * conflict on which organisation types take which check form, so both are
 * accepted.
 */
public final class EsCif implements StdNum {

    public static final EsCif INSTANCE = new EsCif();

    private static final String ORGANISATION_TYPES = "ABCDEFGHJNPQRSUVW";
    private static final String CHECK_LETTERS = "JABCDEFGHI";

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("es.cif", "CIF")
                    .country("ES")
                    .title("Código de Identificación Fiscal")
                    .description("Spanish legal entity tax number: type letter, 7 digits"
                            + " and a numeric or alphabetic check digit.")
                    .tags(Tag.TAX, Tag.COMPANY)
                    .build();

    private EsCif() {
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
        if (n.length() < 2 || !Strings.isDigits(n.substring(1, n.length() - 1))) {
            throw new InvalidFormatException();
        }
        if (n.length() != 9) {
            throw new InvalidLengthException();
        }
        if (ORGANISATION_TYPES.indexOf(n.charAt(0)) < 0) {
            throw new InvalidFormatException();
        }
        char digitCheck = Luhn.calcCheckDigit(n.substring(1, 8));
        char letterCheck = CHECK_LETTERS.charAt(digitCheck - '0');
        if (n.charAt(8) != digitCheck && n.charAt(8) != letterCheck) {
            throw new InvalidChecksumException();
        }
        return n;
    }

    /** The four components of a CIF: type letter, province, sequence and check. */
    public record Parts(String organisationType, String province,
                        String sequence, String checkDigit) {
    }

    /** Splits a valid CIF into its four components. */
    public static Parts split(String number) {
        String n = INSTANCE.validate(number);
        return new Parts(n.substring(0, 1), n.substring(1, 3),
                n.substring(3, 8), n.substring(8));
    }

}
