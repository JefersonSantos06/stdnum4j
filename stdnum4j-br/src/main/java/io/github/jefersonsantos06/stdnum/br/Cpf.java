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
 * CPF (Cadastro de Pessoas Físicas), the Brazilian personal tax identifier.
 *
 * <p>Eleven digits, the last two being check digits computed with weighted
 * sums modulo 11. Unlike the pure check digit algorithm, numbers made of a
 * single repeated digit ({@code 111.111.111-11}) are rejected, matching the
 * behaviour of the Receita Federal.</p>
 */
public final class Cpf implements StdNum {

    public static final Cpf INSTANCE = new Cpf();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("br.cpf", "CPF")
                    .country("BR")
                    .title("Cadastro de Pessoas Físicas")
                    .description("Brazilian personal tax identifier: 11 digits with two"
                            + " weighted mod 11 check digits. Numbers consisting of a single"
                            + " repeated digit are rejected.")
                    .tags(Tag.TAX, Tag.PERSON)
                    .references("https://www.gov.br/receitafederal/pt-br/assuntos/meu-cpf",
                            "https://en.wikipedia.org/wiki/CPF_number")
                    .build();

    private static final Mask MASK = Mask.of("###.###.###-##");

    private static final int[] WEIGHTS_1 = Weighted.descending(10, 9);
    private static final int[] WEIGHTS_2 = Weighted.descending(11, 10);

    private Cpf() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " -.");
    }

    /**
     * Calculates the two check digits for the given 9-digit base.
     *
     * @throws InvalidLengthException if the base does not have 9 digits
     * @throws InvalidFormatException on non-digit input
     */
    public static String calcCheckDigits(String base) {
        String b = INSTANCE.compact(base);
        if (!Strings.isDigits(b)) {
            throw new InvalidFormatException();
        }
        if (b.length() != 9) {
            throw new InvalidLengthException();
        }
        int d1 = Weighted.mod11CheckDigit(b, WEIGHTS_1);
        int d2 = Weighted.mod11CheckDigit(b + d1, WEIGHTS_2);
        return "" + d1 + d2;
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
            throw new InvalidFormatException(Message.of(Cpf.class, "cpf.repeated",
                    "A CPF consisting of a single repeated digit is not valid."));
        }
        if (!n.endsWith(calcCheckDigits(n.substring(0, 9)))) {
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
