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
 * EMSO, the Slovenian personal identification number: the date of birth with
 * a three-digit year, a region of birth, a serial number that gives the sex,
 * and a check digit. It is the Slovenian form of the number the whole of the
 * former Yugoslavia used.
 */
public final class SiEmso implements StdNum {

    public static final SiEmso INSTANCE = new SiEmso();

    private static final int[] WEIGHTS = {7, 6, 5, 4, 3, 2, 7, 6, 5, 4, 3, 2};

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("si.emso", "EMSO")
                    .country("SI")
                    .title("Enotna maticna stevilka obcana")
                    .description("Slovenian personal identification number: 13 digits giving"
                            + " the date and region of birth, the sex and a check digit.")
                    .tags(Tag.PERSON)
                    .references("https://en.wikipedia.org/wiki/Unique_Master_Citizen_Number")
                    .build();

    private SiEmso() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " ");
    }

    /** The check digit of a number, from its first twelve digits. */
    public static char calcCheckDigit(String number) {
        String n = INSTANCE.compact(number);
        int total = 0;
        for (int i = 0; i < WEIGHTS.length && i < n.length(); i++) {
            total += WEIGHTS[i] * (n.charAt(i) - '0');
        }
        return (char) ('0' + Math.floorMod(-total, 11) % 10);
    }

    /** The birth date encoded in the number, whose year runs on three digits. */
    public static LocalDate getBirthDate(String number) {
        String n = INSTANCE.compact(number);
        if (!Strings.isDigits(n) || n.length() != 13) {
            throw new InvalidFormatException();
        }
        int year = Integer.parseInt(n.substring(4, 7));
        year += year < 800 ? 2000 : 1000;
        try {
            return LocalDate.of(year, Integer.parseInt(n.substring(2, 4)),
                    Integer.parseInt(n.substring(0, 2)));
        } catch (DateTimeException e) {
            throw new InvalidComponentException(
                    "The number does not contain a valid birth date.");
        }
    }

    /** The sex recorded in the number, {@code 'M'} or {@code 'F'}. */
    public static char getGender(String number) {
        String n = INSTANCE.compact(number);
        if (!Strings.isDigits(n) || n.length() != 13) {
            throw new InvalidFormatException();
        }
        return Integer.parseInt(n.substring(9, 12)) < 500 ? 'M' : 'F';
    }

    /** The two digits naming the region of birth. */
    public static String getRegion(String number) {
        String n = INSTANCE.compact(number);
        if (n.length() != 13) {
            throw new InvalidLengthException();
        }
        return n.substring(7, 9);
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (n.length() != 13) {
            throw new InvalidLengthException();
        }
        if (!Strings.isDigits(n)) {
            throw new InvalidFormatException();
        }
        getBirthDate(n);
        if (n.charAt(12) != calcCheckDigit(n)) {
            throw new InvalidChecksumException();
        }
        return n;
    }
}
