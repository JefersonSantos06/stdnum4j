package io.github.jefersonsantos06.stdnum.apac;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

/**
 * ИНН (Идентификационный номер налогоплательщика), the Russian taxpayer
 * number: ten digits for organisations (one check digit) or twelve for
 * persons (two check digits), each with its own weight set.
 */
public final class RuInn implements StdNum {

    public static final RuInn INSTANCE = new RuInn();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("ru.inn", "ИНН")
                    .country("RU")
                    .title("Идентификационный номер налогоплательщика")
                    .description("Russian taxpayer number: 10 digits for organisations or 12"
                            + " for persons, with weighted mod 11 check digits.")
                    .tags(Tag.TAX, Tag.VAT)
                    .build();

    private static final int[] COMPANY = {2, 4, 10, 3, 5, 9, 4, 6, 8};
    private static final int[] PERSONAL_1 = {7, 2, 4, 10, 3, 5, 9, 4, 6, 8};
    private static final int[] PERSONAL_2 = {3, 7, 2, 4, 10, 3, 5, 9, 4, 6, 8};

    private RuInn() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " ");
    }

    private static char weightedDigit(String number, int[] weights) {
        int sum = 0;
        for (int i = 0; i < weights.length && i < number.length(); i++) {
            sum += weights[i] * (number.charAt(i) - '0');
        }
        return (char) ('0' + sum % 11 % 10);
    }

    /** The check digit of a ten-digit organisation number. */
    public static char calcCompanyCheckDigit(String base) {
        return weightedDigit(base, COMPANY);
    }

    /** The two check digits of a twelve-digit personal number. */
    public static String calcPersonalCheckDigits(String base) {
        char d1 = weightedDigit(base, PERSONAL_1);
        char d2 = weightedDigit(base.substring(0, 10) + d1, PERSONAL_2);
        return "" + d1 + d2;
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (!Strings.isDigits(n)) {
            throw new InvalidFormatException();
        }
        if (n.length() == 10) {
            if (n.charAt(9) != calcCompanyCheckDigit(n)) {
                throw new InvalidChecksumException();
            }
        } else if (n.length() == 12) {
            if (!n.endsWith(calcPersonalCheckDigits(n))) {
                throw new InvalidChecksumException();
            }
        } else {
            throw new InvalidLengthException();
        }
        return n;
    }
}
