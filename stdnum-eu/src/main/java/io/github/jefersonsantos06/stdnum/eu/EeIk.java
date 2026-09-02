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
 * Isikukood, the Estonian personal identification code: eleven digits whose
 * first gives both the sex and the century of birth, followed by the date of
 * birth, a serial number and a check digit.
 */
public final class EeIk implements StdNum {

    public static final EeIk INSTANCE = new EeIk();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("ee.ik", "Isikukood")
                    .country("EE")
                    .title("Eesti isikukood")
                    .description("Estonian personal identification code: 11 digits giving the"
                            + " sex, the date of birth and a weighted mod 11 check digit.")
                    .tags(Tag.PERSON)
                    .references("https://et.wikipedia.org/wiki/Isikukood")
                    .build();

    private EeIk() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " ");
    }

    /**
     * The check digit of a number, from every digit but its last. The weights
     * run 1..9 cyclically; when that leaves a remainder of 10 the sum is taken
     * again with the weights shifted along by two.
     *
     * <p>This is the routine Estonia also applies to the company
     * {@link EeRegistrikood} and Lithuania to its {@link LtAsmens}.</p>
     */
    public static char calcCheckDigit(String number) {
        String n = INSTANCE.compact(number);
        int check = weightedSum(n, 1) % 11;
        if (check == 10) {
            check = weightedSum(n, 3) % 11;
        }
        return (char) ('0' + check % 10);
    }

    /** The digits of {@code n} bar the last, weighted from {@code first} on, cycling 1..9. */
    private static int weightedSum(String n, int first) {
        int sum = 0;
        for (int i = 0; i < n.length() - 1; i++) {
            sum += ((i + first - 1) % 9 + 1) * (n.charAt(i) - '0');
        }
        return sum;
    }

    /** The birth date encoded in the number. */
    public static LocalDate getBirthDate(String number) {
        String n = INSTANCE.compact(number);
        if (!Strings.isDigits(n) || n.length() < 7) {
            throw new InvalidFormatException();
        }
        int century = switch (n.charAt(0)) {
            case '1', '2' -> 1800;
            case '3', '4' -> 1900;
            case '5', '6' -> 2000;
            case '7', '8' -> 2100;
            default -> throw new InvalidComponentException(Message.of(EeIk.class, "ik.century",
                    "The first digit does not name a century."));
        };
        try {
            return LocalDate.of(century + Integer.parseInt(n.substring(1, 3)),
                    Integer.parseInt(n.substring(3, 5)), Integer.parseInt(n.substring(5, 7)));
        } catch (DateTimeException e) {
            throw new InvalidComponentException(Reasons.birthDate());
        }
    }

    /** The sex recorded in the number, {@code 'M'} or {@code 'F'}. */
    public static char getGender(String number) {
        String n = INSTANCE.compact(number);
        if (n.isEmpty() || "12345678".indexOf(n.charAt(0)) < 0) {
            throw new InvalidComponentException(Message.of(EeIk.class, "ik.sex",
                    "The first digit does not name a sex."));
        }
        return n.charAt(0) % 2 == 1 ? 'M' : 'F';
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
        if (n.charAt(10) != calcCheckDigit(n)) {
            throw new InvalidChecksumException();
        }
        return n;
    }
}
