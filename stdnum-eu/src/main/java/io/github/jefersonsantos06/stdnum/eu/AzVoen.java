package io.github.jefersonsantos06.stdnum.eu;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidComponentException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

/**
 * VÖEN (Vergi ödəyicisinin eyniləşdirmə nömrəsi), the Azerbaijani tax
 * number: ten digits, the last of which is 1 for legal entities or 2 for
 * individuals, preceded by a weighted mod 11 check digit.
 */
public final class AzVoen implements StdNum {

    public static final AzVoen INSTANCE = new AzVoen();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("az.voen", "VÖEN")
                    .country("AZ")
                    .title("Vergi ödəyicisinin eyniləşdirmə nömrəsi")
                    .description("Azerbaijani tax number: 10 digits ending in 1 or 2, with a"
                            + " weighted mod 11 check digit before it.")
                    .tags(Tag.TAX, Tag.VAT)
                    .build();

    private static final int[] WEIGHTS = {4, 1, 8, 6, 2, 7, 5, 3};

    private AzVoen() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        String n = Strings.compact(number, " ");
        return n.length() == 9 && Strings.isDigits(n) ? "0" + n : n;
    }

    /** The check digit, which sits in the ninth position. */
    public static char calcCheckDigit(String number) {
        String n = INSTANCE.compact(number);
        int sum = 0;
        for (int i = 0; i < WEIGHTS.length && i < n.length(); i++) {
            sum += WEIGHTS[i] * (n.charAt(i) - '0');
        }
        return (char) ('0' + sum % 11);
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (n.length() != 10) {
            throw new InvalidLengthException();
        }
        if (!Strings.isDigits(n)) {
            throw new InvalidFormatException();
        }
        if (n.charAt(9) != '1' && n.charAt(9) != '2') {
            throw new InvalidComponentException("A VÖEN ends with 1 or 2.");
        }
        if (n.charAt(8) != calcCheckDigit(n)) {
            throw new InvalidChecksumException();
        }
        return n;
    }
}
