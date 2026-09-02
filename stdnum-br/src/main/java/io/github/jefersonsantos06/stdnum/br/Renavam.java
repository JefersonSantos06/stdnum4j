package io.github.jefersonsantos06.stdnum.br;

import io.github.jefersonsantos06.stdnum.algo.Weighted;
import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.Message;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

/**
 * RENAVAM (Registro Nacional de Veículos Automotores), the Brazilian vehicle
 * registration number.
 *
 * <p>Eleven digits since 2013 (older numbers had nine and are accepted by
 * left-padding with zeros, which does not affect the check digit). The last
 * digit is a check digit computed with weights 3,2,9,8,7,6,5,4,3,2 modulo 11
 * (remainders 10 and 11 map to 0).</p>
 */
public final class Renavam implements StdNum {

    public static final Renavam INSTANCE = new Renavam();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("br.renavam", "RENAVAM")
                    .country("BR")
                    .title("Registro Nacional de Veículos Automotores")
                    .description("Brazilian vehicle registration number: 11 digits with one"
                            + " weighted mod 11 check digit; legacy 9-digit numbers are"
                            + " accepted by zero-padding.")
                    .tags(Tag.VEHICLE)
                    .references("https://www.gov.br/transportes/pt-br")
                    .build();

    private static final int[] WEIGHTS = Weighted.cyclic(10, 2, 3, 4, 5, 6, 7, 8, 9);

    private Renavam() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        String n = Strings.compact(number, " -.");
        // legacy 9-digit numbers are equivalent to the zero-padded 11-digit form
        if (n.length() == 9 && Strings.isDigits(n)) {
            return "00" + n;
        }
        return n;
    }

    /** Calculates the check digit for the given 10-digit base. */
    public static int calcCheckDigit(String base) {
        String b = Strings.compact(base, " -.");
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
            throw new InvalidFormatException(Message.of(Renavam.class, "renavam.repeated",
                    "A RENAVAM consisting of a single repeated digit is not valid."));
        }
        if (n.charAt(10) - '0' != calcCheckDigit(n.substring(0, 10))) {
            throw new InvalidChecksumException();
        }
        return n;
    }
}
