package io.github.jefersonsantos06.stdnum.eu;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidComponentException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.Message;
import io.github.jefersonsantos06.stdnum.spi.Reasons;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.time.DateTimeException;
import java.time.LocalDate;

/**
 * RČ (Rodné číslo), the Czech birth number — identical to the Slovak one,
 * since both countries were one until 1993 (see {@link SkRc}).
 *
 * <p>Nine or ten digits: a birth date where females have 50 added to the
 * month (and 20 more when the daily serial overflows, since 2004), a
 * serial number, and — for ten-digit numbers issued from 1954 — a check
 * digit making the whole number a multiple of 11.</p>
 */
public final class CzRc implements StdNum {

    public static final CzRc INSTANCE = new CzRc();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("cz.rc", "RČ")
                    .country("CZ")
                    .title("Rodné číslo")
                    .description("Czech birth number: 9 or 10 digits encoding birth date and"
                            + " gender, with a mod 11 check digit since 1954.")
                    .tags(Tag.PERSON)
                    .build();

    private CzRc() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " /");
    }

    /** The birth date encoded in the number. */
    public static LocalDate getBirthDate(String number) {
        String n = INSTANCE.compact(number);
        if (!Strings.isDigits(n) || (n.length() != 9 && n.length() != 10)) {
            throw new InvalidFormatException();
        }
        int year = 1900 + Integer.parseInt(n.substring(0, 2));
        int month = Integer.parseInt(n.substring(2, 4)) % 50 % 20;
        int day = Integer.parseInt(n.substring(4, 6));
        if (n.length() == 9) {
            if (year >= 1980) {
                year -= 100;
            }
            if (year > 1953) {
                throw new InvalidLengthException(Message.of(CzRc.class, "rc.nine-digit-year",
                        "No 9 digit birth numbers after 1953."));
            }
        } else if (year < 1954) {
            year += 100;
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
        if (n.length() != 9 && n.length() != 10) {
            throw new InvalidLengthException();
        }
        getBirthDate(n);
        if (n.length() == 10) {
            int check = (int) (Long.parseLong(n.substring(0, 9)) % 11 % 10);
            if (n.charAt(9) - '0' != check) {
                throw new InvalidChecksumException();
            }
        }
        return n;
    }

    @Override
    public String format(String number) {
        String n = validate(number);
        return n.substring(0, 6) + "/" + n.substring(6);
    }
}
