package io.github.jefersonsantos06.stdnum.latam;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.util.Locale;

/**
 * CUI, the number on the Peruvian national identity card: the eight digits of
 * the DNI and a check character.
 *
 * <p>The card carries the check character as a digit on one side and as a
 * letter on the other, and either is accepted. The digit alone, without any
 * check character, is the DNI as it is usually quoted.</p>
 */
public final class PeCui implements StdNum {

    public static final PeCui INSTANCE = new PeCui();

    private static final int[] WEIGHTS = {3, 2, 7, 6, 5, 4, 3, 2};
    private static final String CHECK_DIGITS = "65432110987";
    private static final String CHECK_LETTERS = "KJIHGFEDCBA";

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("pe.cui", "CUI")
                    .country("PE")
                    .title("Codigo Unico de Identificacion")
                    .description("Peruvian national identity card number: the 8 digits of the"
                            + " DNI and an optional check digit or check letter.")
                    .tags(Tag.PERSON)
                    .references("https://www.gob.pe/235-documento-nacional-de-identidad-dni")
                    .build();

    private PeCui() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " -").toUpperCase(Locale.ROOT);
    }

    /**
     * The two check characters a number may carry, the digit first and the
     * letter second; either one makes the number valid.
     */
    public static String calcCheckDigits(String number) {
        String n = INSTANCE.compact(number);
        int sum = 0;
        for (int i = 0; i < WEIGHTS.length && i < n.length(); i++) {
            sum += WEIGHTS[i] * (n.charAt(i) - '0');
        }
        int c = sum % 11;
        return "" + CHECK_DIGITS.charAt(c) + CHECK_LETTERS.charAt(c);
    }

    /** The RUC the tax office derives from this number. */
    public static String toRuc(String number) {
        String n = "10" + INSTANCE.compact(number).substring(0, 8);
        return n + PeRuc.calcCheckDigit(n);
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (n.length() != 8 && n.length() != 9) {
            throw new InvalidLengthException();
        }
        if (!Strings.isDigits(n.substring(0, 8))) {
            throw new InvalidFormatException();
        }
        if (n.length() > 8 && calcCheckDigits(n).indexOf(n.charAt(8)) < 0) {
            throw new InvalidChecksumException();
        }
        return n;
    }
}
