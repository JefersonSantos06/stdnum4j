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
 * AMKA, the Greek social security number: the date of birth, a serial number
 * that gives the sex, and a Luhn check digit.
 *
 * <p>Only two digits of the year are stored, so a date that does not exist in
 * the twentieth century is read as the twenty-first — which is how 29 February
 * tells the two apart.</p>
 */
public final class GrAmka implements StdNum {

    public static final GrAmka INSTANCE = new GrAmka();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("gr.amka", "AMKA")
                    .country("GR")
                    .title("Arithmos Mitroou Koinonikis Asfalisis")
                    .description("Greek social security number: 11 digits giving the date of"
                            + " birth, the sex and a Luhn check digit.")
                    .tags(Tag.PERSON, Tag.HEALTH)
                    .references("https://www.amka.gr/")
                    .build();

    private GrAmka() {
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
        int day = Integer.parseInt(n.substring(0, 2));
        int month = Integer.parseInt(n.substring(2, 4));
        int year = Integer.parseInt(n.substring(4, 6)) + 1900;
        try {
            return LocalDate.of(year, month, day);
        } catch (DateTimeException e) {
            try {
                return LocalDate.of(year + 100, month, day);
            } catch (DateTimeException e2) {
                throw new InvalidComponentException(
                        "The number does not contain a valid birth date.");
            }
        }
    }

    /** The sex recorded in the number, {@code 'M'} or {@code 'F'}. */
    public static char getGender(String number) {
        String n = INSTANCE.compact(number);
        if (!Strings.isDigits(n) || n.length() != 11) {
            throw new InvalidFormatException();
        }
        return (n.charAt(9) - '0') % 2 == 1 ? 'M' : 'F';
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
        Luhn.validate(n);
        getBirthDate(n);
        return n;
    }
}
