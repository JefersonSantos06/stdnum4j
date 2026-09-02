package io.github.jefersonsantos06.stdnum.eu;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidComponentException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.Reasons;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.time.DateTimeException;
import java.time.LocalDate;

/**
 * ЕГН (Единен граждански номер), the Bulgarian personal identity code: ten
 * digits where the first six are a birth date (the month carries the
 * century: +40 for the 2000s, +20 for the 1800s), the next three give a
 * birth order and gender, and the last is a weighted check digit.
 */
public final class BgEgn implements StdNum {

    public static final BgEgn INSTANCE = new BgEgn();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("bg.egn", "ЕГН")
                    .country("BG")
                    .title("Единен граждански номер")
                    .description("Bulgarian personal identity code: 10 digits with an"
                            + " embedded birth date and a weighted mod 11 check digit.")
                    .tags(Tag.PERSON)
                    .build();

    private static final int[] WEIGHTS = {2, 4, 8, 5, 10, 9, 7, 3, 6};

    private BgEgn() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " -.");
    }

    /** The check digit for the nine-digit base. */
    public static char calcCheckDigit(String base) {
        int sum = 0;
        for (int i = 0; i < WEIGHTS.length && i < base.length(); i++) {
            sum += WEIGHTS[i] * (base.charAt(i) - '0');
        }
        return (char) ('0' + sum % 11 % 10);
    }

    /** The birth date encoded in the number. */
    public static LocalDate getBirthDate(String number) {
        String n = INSTANCE.compact(number);
        if (!Strings.isDigits(n) || n.length() != 10) {
            throw new InvalidFormatException();
        }
        int year = Integer.parseInt(n.substring(0, 2)) + 1900;
        int month = Integer.parseInt(n.substring(2, 4));
        int day = Integer.parseInt(n.substring(4, 6));
        if (month > 40) {
            year += 100;
            month -= 40;
        } else if (month > 20) {
            year -= 100;
            month -= 20;
        }
        try {
            return LocalDate.of(year, month, day);
        } catch (DateTimeException e) {
            throw new InvalidComponentException(Reasons.birthDate());
        }
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (!Strings.isDigits(n)) {
            throw new InvalidFormatException();
        }
        if (n.length() != 10) {
            throw new InvalidLengthException();
        }
        getBirthDate(n);
        if (n.charAt(9) != calcCheckDigit(n.substring(0, 9))) {
            throw new InvalidChecksumException();
        }
        return n;
    }
}
