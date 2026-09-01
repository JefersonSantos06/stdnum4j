package io.github.jefersonsantos06.stdnum.apac;

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
 * RRN (주민등록번호), the South Korean resident registration number:
 * thirteen digits — a six-digit birth date, one digit encoding century and
 * gender, four for the place of birth, two for the community centre, one
 * serial and a weighted check digit. Foreign residents receive an alien
 * registration number with the same encoding.
 */
public final class KrRrn implements StdNum {

    public static final KrRrn INSTANCE = new KrRrn();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("kr.rrn", "RRN")
                    .country("KR")
                    .title("Resident registration number (주민등록번호)")
                    .description("South Korean resident registration number: 13 digits"
                            + " encoding birth date, gender and place of birth.")
                    .tags(Tag.PERSON)
                    .build();

    private static final int[] WEIGHTS = {2, 3, 4, 5, 6, 7, 8, 9, 2, 3, 4, 5};

    private KrRrn() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, "-");
    }

    /** The check digit for the twelve-digit base. */
    public static char calcCheckDigit(String base) {
        int sum = 0;
        for (int i = 0; i < WEIGHTS.length && i < base.length(); i++) {
            sum += WEIGHTS[i] * (base.charAt(i) - '0');
        }
        return (char) ('0' + (11 - sum % 11) % 10);
    }

    /** The birth date encoded in the number. */
    public static LocalDate getBirthDate(String number) {
        String n = INSTANCE.compact(number);
        if (!Strings.isDigits(n) || n.length() != 13) {
            throw new InvalidFormatException();
        }
        int year = Integer.parseInt(n.substring(0, 2));
        int month = Integer.parseInt(n.substring(2, 4));
        int day = Integer.parseInt(n.substring(4, 6));
        char marker = n.charAt(6);
        if ("1256".indexOf(marker) >= 0) {
            year += 1900;
        } else if ("3478".indexOf(marker) >= 0) {
            year += 2000;
        } else {
            year += 1800;
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
        if (!Strings.isDigits(n)) {
            throw new InvalidFormatException();
        }
        if (n.length() != 13) {
            throw new InvalidLengthException();
        }
        getBirthDate(n);
        if (Integer.parseInt(n.substring(7, 9)) > 96) {
            throw new InvalidComponentException("Unknown place of birth code.");
        }
        if (n.charAt(12) != calcCheckDigit(n.substring(0, 12))) {
            throw new InvalidChecksumException();
        }
        return n;
    }

    @Override
    public String format(String number) {
        String n = validate(number);
        return n.substring(0, 6) + "-" + n.substring(6);
    }

    /**
     * Validates the number, optionally rejecting birth dates in the future.
     * The plain {@link #validate(String)} allows them, since the number is
     * issued at birth and registries do carry forward-dated records.
     */
    public static String validate(String number, boolean allowFuture) {
        String n = INSTANCE.validate(number);
        if (!allowFuture && getBirthDate(n).isAfter(java.time.LocalDate.now())) {
            throw new InvalidComponentException("The birth date is in the future.");
        }
        return n;
    }

}
