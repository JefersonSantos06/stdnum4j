package io.github.jefersonsantos06.stdnum.eu;

import io.github.jefersonsantos06.stdnum.algo.Luhn;
import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidComponentException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.time.DateTimeException;
import java.time.LocalDate;

/**
 * Personnummer, the Swedish personal identity number: the date of birth, a
 * separator, a three-digit serial that gives the sex, and a Luhn check digit.
 *
 * <p>The separator is part of the number rather than decoration: it turns
 * from a hyphen into a plus on the New Year's Eve of the year the holder
 * turns 100, which is how a two-digit year stays unambiguous. The compact
 * form therefore keeps it.</p>
 */
public final class SePersonnummer implements StdNum {

    public static final SePersonnummer INSTANCE = new SePersonnummer();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("se.personnummer", "Personnummer")
                    .country("SE")
                    .title("Svenskt personnummer")
                    .description("Swedish personal identity number: the date of birth, an age"
                            + " separator, a serial number and a Luhn check digit.")
                    .tags(Tag.PERSON)
                    .references("https://sv.wikipedia.org/wiki/Personnummer_i_Sverige")
                    .build();

    private SePersonnummer() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        String n = Strings.clean(number, " :");
        if ((n.length() == 10 || n.length() == 12) && "-+".indexOf(n.charAt(n.length() - 5)) < 0) {
            n = n.substring(0, n.length() - 4) + '-' + n.substring(n.length() - 4);
        }
        if (n.length() < 5) {
            return n;
        }
        // the separator is only meaningful in its own position
        return n.substring(0, n.length() - 5).replace("-", "").replace("+", "")
                + n.substring(n.length() - 5);
    }

    /**
     * The birth date encoded in the number. A ten-digit number gives only two
     * digits of the year, so the century is worked out from today's date and
     * from whether the separator says the holder has turned 100.
     */
    public static LocalDate getBirthDate(String number) {
        String n = INSTANCE.compact(number);
        if (n.length() != 11 && n.length() != 13) {
            throw new InvalidLengthException();
        }
        int year;
        int month;
        int day;
        if (n.length() == 13) {
            year = Integer.parseInt(n.substring(0, 4));
            month = Integer.parseInt(n.substring(4, 6));
            day = Integer.parseInt(n.substring(6, 8));
        } else {
            int thisYear = LocalDate.now().getYear();
            int century = thisYear / 100;
            if (Integer.parseInt(n.substring(0, 2)) > thisYear % 100) {
                century--;
            }
            if (n.charAt(n.length() - 5) == '+') {
                century--;
            }
            year = century * 100 + Integer.parseInt(n.substring(0, 2));
            month = Integer.parseInt(n.substring(2, 4));
            day = Integer.parseInt(n.substring(4, 6));
        }
        try {
            return LocalDate.of(year, month, day);
        } catch (DateTimeException e) {
            throw new InvalidComponentException(
                    "The number does not contain a valid birth date.");
        }
    }

    /** The sex recorded in the number, {@code 'M'} or {@code 'F'}. */
    public static char getGender(String number) {
        String n = INSTANCE.compact(number);
        if (n.length() < 2) {
            throw new InvalidFormatException();
        }
        return (n.charAt(n.length() - 2) - '0') % 2 == 1 ? 'M' : 'F';
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (n.length() != 11 && n.length() != 13) {
            throw new InvalidLengthException();
        }
        String digits = n.substring(0, n.length() - 5) + n.substring(n.length() - 4);
        if ("-+".indexOf(n.charAt(n.length() - 5)) < 0 || !Strings.isDigits(digits)) {
            throw new InvalidFormatException();
        }
        getBirthDate(n);
        Luhn.validate(digits.substring(digits.length() - 10));
        return n;
    }
}
