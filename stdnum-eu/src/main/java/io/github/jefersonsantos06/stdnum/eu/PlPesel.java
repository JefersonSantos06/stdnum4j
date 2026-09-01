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
 * PESEL, the Polish national identification number: eleven digits carrying
 * the birth date (with the century encoded as an offset on the month), a
 * serial number whose second-to-last digit gives the gender, and a check
 * digit.
 */
public final class PlPesel implements StdNum {

    public static final PlPesel INSTANCE = new PlPesel();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("pl.pesel", "PESEL")
                    .country("PL")
                    .title("Powszechny Elektroniczny System Ewidencji Ludności")
                    .description("Polish national identification number: 11 digits encoding"
                            + " birth date, gender and a check digit.")
                    .tags(Tag.PERSON)
                    .build();

    private static final int[] WEIGHTS = {1, 3, 7, 9, 1, 3, 7, 9, 1, 3};

    private PlPesel() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " -");
    }

    /** The birth date encoded in the number. */
    public static LocalDate getBirthDate(String number) {
        String n = INSTANCE.compact(number);
        if (!Strings.isDigits(n) || n.length() != 11) {
            throw new InvalidFormatException();
        }
        int year = Integer.parseInt(n.substring(0, 2));
        int month = Integer.parseInt(n.substring(2, 4));
        int day = Integer.parseInt(n.substring(4, 6));
        year += switch (month / 20) {
            case 0 -> 1900;
            case 1 -> 2000;
            case 2 -> 2100;
            case 3 -> 2200;
            default -> 1800;
        };
        try {
            return LocalDate.of(year, month % 20, day);
        } catch (DateTimeException e) {
            throw new InvalidComponentException(
                    "The number does not contain a valid birth date.");
        }
    }

    /** The gender encoded in the number: {@code 'M'} or {@code 'F'}. */
    public static char getGender(String number) {
        String n = INSTANCE.validate(number);
        return (n.charAt(9) - '0') % 2 == 0 ? 'F' : 'M';
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
        getBirthDate(n);
        int sum = 0;
        for (int i = 0; i < WEIGHTS.length; i++) {
            sum += WEIGHTS[i] * (n.charAt(i) - '0');
        }
        if ((10 - sum % 10) % 10 != n.charAt(10) - '0') {
            throw new InvalidChecksumException();
        }
        return n;
    }

    /** The check digit for the ten-digit base. */
    public static char calcCheckDigit(String base) {
        String b = INSTANCE.compact(base);
        if (!Strings.isDigits(b)) {
            throw new InvalidFormatException();
        }
        if (b.length() != 10) {
            throw new InvalidLengthException();
        }
        int sum = 0;
        for (int i = 0; i < WEIGHTS.length; i++) {
            sum += WEIGHTS[i] * (b.charAt(i) - '0');
        }
        return (char) ('0' + (10 - sum % 10) % 10);
    }

}
