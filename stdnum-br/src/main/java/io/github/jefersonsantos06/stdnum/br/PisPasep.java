package io.github.jefersonsantos06.stdnum.br;

import io.github.jefersonsantos06.stdnum.algo.Weighted;
import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

/**
 * PIS/PASEP/NIT (Número de Inscrição do Trabalhador), the Brazilian worker
 * registration number.
 *
 * <p>Eleven digits, the last one a check digit computed with weights
 * 3,2,9,8,7,6,5,4,3,2 modulo 11 (remainders 10 and 11 map to 0). Numbers made
 * of a single repeated digit are rejected.</p>
 */
public final class PisPasep implements StdNum {

    public static final PisPasep INSTANCE = new PisPasep();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("br.pis", "PIS/PASEP")
                    .country("BR")
                    .title("PIS/PASEP - Número de Inscrição do Trabalhador")
                    .description("Brazilian worker registration number: 11 digits with one"
                            + " weighted mod 11 check digit.")
                    .tags(Tag.PERSON)
                    .references("https://www.gov.br/pt-br/servicos/consultar-inscricao-no-pis-pasep")
                    .build();

    private static final int[] WEIGHTS = Weighted.cyclic(10, 2, 3, 4, 5, 6, 7, 8, 9);

    private PisPasep() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " -.");
    }

    /** Calculates the check digit for the given 10-digit base. */
    public static int calcCheckDigit(String base) {
        String b = INSTANCE.compact(base);
        if (!Strings.isDigits(b)) {
            throw new InvalidFormatException();
        }
        if (b.length() != 10) {
            throw new InvalidLengthException();
        }
        return Weighted.mod11CheckDigit(b, WEIGHTS);
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (!Strings.isDigits(n)) {
            throw new InvalidFormatException();
        }
        if (n.length() != 11) {
            throw new InvalidLengthException();
        }
        if (Repeats.allSame(n)) {
            throw new InvalidFormatException(
                    "A PIS/PASEP consisting of a single repeated digit is not valid.");
        }
        if (n.charAt(10) - '0' != calcCheckDigit(n.substring(0, 10))) {
            throw new InvalidChecksumException();
        }
        return n;
    }

    @Override
    public String format(String number) {
        String n = validate(number);
        return n.substring(0, 3) + "." + n.substring(3, 8) + "."
                + n.substring(8, 10) + "-" + n.substring(10);
    }
}
