package io.github.jefersonsantos06.stdnum.eu;

import io.github.jefersonsantos06.stdnum.spi.Dates;
import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.time.LocalDate;

/**
 * PVN (Pievienotās vērtības nodoklis), the Latvian VAT number: eleven
 * digits that are either a legal entity reference (first digit above 3) or
 * a personal code — starting {@code 32} for codes issued from July 2017,
 * otherwise carrying a birth date in the first six digits.
 */
public final class LvPvn implements StdNum {

    public static final LvPvn INSTANCE = new LvPvn();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("lv.pvn", "PVN")
                    .country("LV")
                    .title("Pievienotās vērtības nodokļa numurs")
                    .description("Latvian VAT number: 11 digits, either a legal entity"
                            + " reference or a personal code.")
                    .tags(Tag.VAT)
                    .build();

    private static final int[] LEGAL_WEIGHTS = {9, 1, 4, 8, 3, 10, 2, 5, 7, 6, 1};
    private static final int[] PERSONAL_WEIGHTS = {10, 5, 8, 4, 2, 1, 6, 3, 7, 9};

    private LvPvn() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " -", "LV");
    }

    /** The birth date encoded in an old-style personal code. */
    public static LocalDate getBirthDate(String number) {
        String n = Strings.requireDigits(INSTANCE.compact(number), 11);
        int day = Integer.parseInt(n.substring(0, 2));
        int month = Integer.parseInt(n.substring(2, 4));
        int year = Integer.parseInt(n.substring(4, 6)) + 1800 + (n.charAt(6) - '0') * 100;
        return Dates.birthDate(year, month, day);
    }

    /** The check digit of a personal code, from its first ten digits. */
    public static char calcPersonalCheckDigit(String base) {
        int check = 1;
        for (int i = 0; i < PERSONAL_WEIGHTS.length && i < base.length(); i++) {
            check += PERSONAL_WEIGHTS[i] * (base.charAt(i) - '0');
        }
        return (char) ('0' + check % 11 % 10);
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
        if (n.charAt(0) > '3') {
            int sum = 0;
            for (int i = 0; i < LEGAL_WEIGHTS.length; i++) {
                sum += LEGAL_WEIGHTS[i] * (n.charAt(i) - '0');
            }
            if (sum % 11 != 3) {
                throw new InvalidChecksumException();
            }
        } else {
            if (!n.startsWith("32")) {
                // old-style personal code: the first six digits are a birth date
                getBirthDate(n);
            }
            if (n.charAt(10) != calcPersonalCheckDigit(n.substring(0, 10))) {
                throw new InvalidChecksumException();
            }
        }
        return n;
    }
}
