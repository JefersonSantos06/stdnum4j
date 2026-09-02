package io.github.jefersonsantos06.stdnum.br;

import io.github.jefersonsantos06.stdnum.algo.Weighted;
import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidComponentException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.Message;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

/**
 * CNS (Cartão Nacional de Saúde), the Brazilian national health card number.
 *
 * <p>Fifteen digits, validated with weights 15 down to 1 modulo 11 following
 * the DATASUS rules:</p>
 *
 * <ul>
 *   <li>numbers starting with 1 or 2 (definitive) are derived from an
 *       11-digit base: the full number is the base, a {@code 000} or
 *       {@code 001} filler and one check digit, recomputed here and compared;</li>
 *   <li>numbers starting with 7, 8 or 9 (provisional) must have a weighted
 *       sum that is a multiple of 11.</li>
 * </ul>
 */
public final class Cns implements StdNum {

    public static final Cns INSTANCE = new Cns();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("br.cns", "CNS")
                    .country("BR")
                    .title("Cartão Nacional de Saúde")
                    .description("Brazilian national health card number: 15 digits validated"
                            + " with weights 15..1 modulo 11 (DATASUS rules for definitive"
                            + " and provisional numbers).")
                    .tags(Tag.HEALTH, Tag.PERSON)
                    .references("https://rni-docs.anvisa.gov.br/docs/regras_gerais/validacoes/validacaoCNS/")
                    .build();

    private static final int[] WEIGHTS_BASE = Weighted.descending(15, 11);
    private static final int[] WEIGHTS_FULL = Weighted.descending(15, 15);

    private Cns() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " .-");
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (!Strings.isDigits(n)) {
            throw new InvalidFormatException();
        }
        if (n.length() != 15) {
            throw new InvalidLengthException();
        }
        char first = n.charAt(0);
        if (first == '1' || first == '2') {
            if (!n.equals(n.substring(0, 11) + definitiveSuffix(n.substring(0, 11)))) {
                throw new InvalidChecksumException();
            }
        } else if (first == '7' || first == '8' || first == '9') {
            if (Weighted.weightedSum(n, WEIGHTS_FULL) % 11 != 0) {
                throw new InvalidChecksumException();
            }
        } else {
            throw new InvalidComponentException(Message.of(Cns.class, "cns.prefix",
                    "A CNS must start with 1, 2 (definitive) or 7, 8, 9 (provisional)."));
        }
        return n;
    }

    @Override
    public String format(String number) {
        String n = validate(number);
        return n.substring(0, 3) + " " + n.substring(3, 7) + " "
                + n.substring(7, 11) + " " + n.substring(11);
    }

    /**
     * The 4-character suffix (filler plus check digit) that completes an
     * 11-digit definitive base, per the DATASUS derivation.
     */
    static String definitiveSuffix(String base) {
        int sum = Weighted.weightedSum(base, WEIGHTS_BASE);
        int digit = 11 - sum % 11;
        if (digit == 11) {
            digit = 0;
        }
        if (digit == 10) {
            sum += 2;
            digit = 11 - sum % 11;
            if (digit == 11) {
                digit = 0;
            }
            return "001" + digit;
        }
        return "000" + digit;
    }
}
