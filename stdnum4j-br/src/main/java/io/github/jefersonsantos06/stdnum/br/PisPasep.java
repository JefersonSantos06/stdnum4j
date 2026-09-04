package io.github.jefersonsantos06.stdnum.br;

import io.github.jefersonsantos06.stdnum.algo.Weighted;
import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.Message;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Mask;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.util.List;

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
                    .references("https://pt.wikipedia.org/wiki/Programa_de_Integra%C3%A7%C3%A3o_Social")
                    .build();

    private static final Mask MASK = Mask.of("###.#####.##-#");

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
        if (Strings.allSame(n)) {
            throw new InvalidFormatException(Message.of(PisPasep.class, "pis-pasep.repeated",
                    "A PIS/PASEP consisting of a single repeated digit is not valid."));
        }
        if (n.charAt(10) - '0' != calcCheckDigit(n.substring(0, 10))) {
            throw new InvalidChecksumException();
        }
        return n;
    }

    @Override
    public String format(String number) {
        return MASK.fill(validate(number));
    }

    @Override
    public List<Mask> masks() {
        return List.of(MASK);
    }
}
