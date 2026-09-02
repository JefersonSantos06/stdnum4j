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
 * Código de barras de cobrança (FEBRABAN), the 44-digit barcode of a
 * Brazilian bank payment slip: a three-digit bank code, a currency code,
 * the general check digit, a four-digit due-date factor, a ten-digit
 * amount and twenty-five digits of free field.
 *
 * <p>The check digit sits at position 5 and is computed over the other 43
 * digits with cyclic weights 2..9 modulo 11, where remainders 0, 1 and 10
 * give the digit 1 — a FEBRABAN rule that differs from the plain mod 11
 * used elsewhere.</p>
 */
public final class CodigoBarras implements StdNum {

    public static final CodigoBarras INSTANCE = new CodigoBarras();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("br.boleto-barras", "Código de barras")
                    .country("BR")
                    .title("Código de barras de cobrança (FEBRABAN)")
                    .description("Barcode of a Brazilian payment slip: 44 digits with the"
                            + " general check digit in position 5.")
                    .tags(Tag.BANK, Tag.PAYMENT)
                    .references("https://portal.febraban.org.br/")
                    .build();

    private static final int[] WEIGHTS = Weighted.cyclic(43, 2, 3, 4, 5, 6, 7, 8, 9);

    private CodigoBarras() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " .-");
    }

    /**
     * The general check digit for the 43 digits that remain once position 5
     * is removed. Remainders 0, 1 and 10 all give 1.
     */
    public static int calcCheckDigit(String base) {
        int digit = 11 - Weighted.weightedSum(base, WEIGHTS) % 11;
        return digit == 0 || digit == 10 || digit == 11 ? 1 : digit;
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (!Strings.isDigits(n)) {
            throw new InvalidFormatException();
        }
        if (n.length() != 44) {
            throw new InvalidLengthException();
        }
        if (n.charAt(3) != '9') {
            throw new InvalidComponentException(Message.of(CodigoBarras.class, "codigo-barras.currency",
                    "The currency code of a payment slip barcode is 9 (real)."));
        }
        String base = n.substring(0, 4) + n.substring(5);
        if (n.charAt(4) - '0' != calcCheckDigit(base)) {
            throw new InvalidChecksumException();
        }
        return n;
    }

    /** The three-digit code of the issuing bank. */
    public static String banco(String number) {
        return INSTANCE.validate(number).substring(0, 3);
    }

    /** The amount in cents, taken from the ten-digit value field. */
    public static long valorEmCentavos(String number) {
        return Long.parseLong(INSTANCE.validate(number).substring(9, 19));
    }
}
