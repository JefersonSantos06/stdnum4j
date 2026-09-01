package io.github.jefersonsantos06.stdnum.eu;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidComponentException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.time.DateTimeException;
import java.time.LocalDate;

/**
 * Fodselsnummer, the Norwegian national identity number: the date of birth,
 * a three-digit individual number giving the sex and the century, and two
 * check digits.
 *
 * <p>Norway offsets the day by 40 for a D-number, issued to non-residents,
 * and the month by 40 for an H-number; a day of 80 or more marks an
 * FH-number, which carries no birth date at all.</p>
 */
public final class NoFodselsnummer implements StdNum {

    public static final NoFodselsnummer INSTANCE = new NoFodselsnummer();

    private static final int[] WEIGHTS_1 = {3, 7, 6, 1, 8, 9, 4, 5, 2};
    private static final int[] WEIGHTS_2 = {5, 4, 3, 2, 7, 6, 5, 4, 3, 2};

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("no.fodselsnummer", "Fodselsnummer")
                    .country("NO")
                    .title("Norsk fodselsnummer")
                    .description("Norwegian national identity number: 11 digits giving the date"
                            + " of birth, the sex and two weighted mod 11 check digits.")
                    .tags(Tag.PERSON)
                    .references("https://no.wikipedia.org/wiki/F%C3%B8dselsnummer")
                    .build();

    private NoFodselsnummer() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " -:");
    }

    /** The first check digit, in tenth position. */
    public static char calcCheckDigit1(String number) {
        return checkDigit(INSTANCE.compact(number), WEIGHTS_1);
    }

    /** The second check digit, in eleventh position. */
    public static char calcCheckDigit2(String number) {
        return checkDigit(INSTANCE.compact(number), WEIGHTS_2);
    }

    /**
     * A weighted mod 11 check digit, or {@code '\0'} when the remainder is 10
     * and no digit can stand for it.
     */
    private static char checkDigit(String n, int[] weights) {
        int sum = 0;
        for (int i = 0; i < weights.length && i < n.length(); i++) {
            sum += weights[i] * (n.charAt(i) - '0');
        }
        int check = Math.floorMod(11 - sum, 11);
        return check == 10 ? '\0' : (char) ('0' + check);
    }

    /** The sex recorded in the number, {@code 'M'} or {@code 'F'}. */
    public static char getGender(String number) {
        String n = INSTANCE.compact(number);
        if (n.length() < 9) {
            throw new InvalidFormatException();
        }
        return (n.charAt(8) - '0') % 2 == 1 ? 'M' : 'F';
    }

    /**
     * The birth date encoded in the number.
     *
     * @throws InvalidComponentException for an FH-number, which by design
     *                                   carries no birth date
     */
    public static LocalDate getBirthDate(String number) {
        String n = INSTANCE.compact(number);
        if (!Strings.isDigits(n) || n.length() != 11) {
            throw new InvalidFormatException();
        }
        int day = Integer.parseInt(n.substring(0, 2));
        int month = Integer.parseInt(n.substring(2, 4));
        int year = Integer.parseInt(n.substring(4, 6));
        int individual = Integer.parseInt(n.substring(6, 9));
        if (day >= 80) {
            throw new InvalidComponentException(
                    "This is an FH-number and carries no birth date by design.");
        }
        if (day > 40) {
            day -= 40;
        }
        if (month > 40) {
            month -= 40;
        }
        if (individual < 500) {
            year += 1900;
        } else if (individual < 750 && year >= 54) {
            year += 1800;
        } else if (individual < 1000 && year < 40) {
            year += 2000;
        } else if (individual >= 900) {
            year += 1900;
        } else {
            throw new InvalidComponentException("The century of birth cannot be determined.");
        }
        try {
            return LocalDate.of(year, month, day);
        } catch (DateTimeException e) {
            throw new InvalidComponentException(
                    "The number does not contain a valid birth date.");
        }
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (n.length() != 11) {
            throw new InvalidLengthException();
        }
        if (!Strings.isDigits(n)) {
            throw new InvalidFormatException();
        }
        if (n.charAt(9) != calcCheckDigit1(n) || n.charAt(10) != calcCheckDigit2(n)) {
            throw new InvalidChecksumException();
        }
        if (getBirthDate(n).isAfter(LocalDate.now())) {
            throw new InvalidComponentException(
                    "The birth date is valid but has not happened yet.");
        }
        return n;
    }

    @Override
    public String format(String number) {
        String n = validate(number);
        return n.substring(0, 6) + ' ' + n.substring(6);
    }
}
