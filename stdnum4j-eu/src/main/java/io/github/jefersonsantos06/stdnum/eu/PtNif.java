package io.github.jefersonsantos06.stdnum.eu;

import io.github.jefersonsantos06.stdnum.algo.Weighted;
import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;


/**
 * NIF (Número de Identificação Fiscal), the Portuguese tax and VAT number:
 * nine digits not starting with zero, closed by a weighted mod 11 check
 * digit. An optional {@code PT} prefix is accepted.
 */
public final class PtNif implements StdNum {

    public static final PtNif INSTANCE = new PtNif();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("pt.nif", "NIF")
                    .country("PT")
                    .title("Número de Identificação Fiscal")
                    .description("Portuguese tax/VAT number: 9 digits with a weighted"
                            + " mod 11 check digit.")
                    .tags(Tag.TAX, Tag.VAT)
                    .build();

    private static final int[] WEIGHTS = Weighted.descending(9, 8);

    private PtNif() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " -.", "PT");
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
        if (n.charAt(8) - '0' != Weighted.mod11CheckDigit(n.substring(0, 8), WEIGHTS)) {
            throw new InvalidChecksumException();
        }
        return n;
    }

    /** The check digit for the eight-digit base. */
    public static char calcCheckDigit(String base) {
        String b = INSTANCE.compact(base);
        if (!Strings.isDigits(b)) {
            throw new InvalidFormatException();
        }
        if (b.length() != 8) {
            throw new InvalidLengthException();
        }
        return (char) ('0' + Weighted.mod11CheckDigit(b, WEIGHTS));
    }

}
