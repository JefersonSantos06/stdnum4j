package io.github.jefersonsantos06.stdnum.latam;

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
 * NI (Número de identidad), the Cuban identity card number: eleven digits
 * whose first six are the birth date and whose seventh digit selects the
 * century ({@code 9} for the 1800s, {@code 0-5} for the 1900s, otherwise
 * the 2000s). The gender is encoded in the tenth digit.
 */
public final class CuNi implements StdNum {

    public static final CuNi INSTANCE = new CuNi();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("cu.ni", "NI")
                    .country("CU")
                    .title("Número de identidad")
                    .description("Cuban identity card number: 11 digits encoding birth date"
                            + " and gender.")
                    .tags(Tag.PERSON)
                    .build();

    private CuNi() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " ");
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
        char century = n.charAt(6);
        if (century == '9') {
            year += 1800;
        } else if (century >= '0' && century <= '5') {
            year += 1900;
        } else {
            year += 2000;
        }
        try {
            return LocalDate.of(year, month, day);
        } catch (DateTimeException e) {
            throw new InvalidComponentException(
                    "The number does not contain a valid birth date.");
        }
    }

    /** The gender encoded in the number: {@code 'M'} or {@code 'F'}. */
    public static char getGender(String number) {
        String n = INSTANCE.validate(number);
        return (n.charAt(9) - '0') % 2 == 0 ? 'M' : 'F';
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
        getBirthDate(n);
        return n;
    }
}
