package io.github.jefersonsantos06.stdnum.latam;

import io.github.jefersonsantos06.stdnum.spi.*;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.util.Locale;

/**
 * NIT (Número De Identificación Tributaria), also called RUT, the Colombian
 * business tax number: 8 to 16 digits closed by a check digit weighted with
 * ascending primes from the right.
 */
public final class CoNit implements StdNum {

    public static final CoNit INSTANCE = new CoNit();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("co.nit", "NIT")
                    .country("CO")
                    .title("Número De Identificación Tributaria")
                    .description("Colombian business tax number: 8 to 16 digits with a"
                            + " prime-weighted mod 11 check digit.")
                    .tags(Tag.TAX, Tag.VAT, Tag.COMPANY)
                    .build();

    private static final int[] WEIGHTS =
            {3, 7, 13, 17, 19, 23, 29, 37, 41, 43, 47, 53, 59, 67, 71};

    private CoNit() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, ".,- ").toUpperCase(Locale.ROOT);
    }

    /** The check digit for the base without its final digit. */
    public static char calcCheckDigit(String base) {
        int sum = 0;
        for (int i = 0; i < WEIGHTS.length && i < base.length(); i++) {
            sum += WEIGHTS[i] * (base.charAt(base.length() - 1 - i) - '0');
        }
        return "01987654321".charAt(sum % 11);
    }

    /**
     * Validates a NIT written without its check digit, the way the DIAN's own
     * forms and electronic invoice write the DV apart from the number, and
     * returns its compact form. With no check digit there is nothing to verify
     * beyond the digits and their count: the 7 to 15 that come before the DV.
     *
     * @throws ValidationException if the base is not 7 to 15 digits
     */
    public static String validateBase(String base) {
        String n = INSTANCE.compact(base);
        if (n.length() < 7 || n.length() > 15) {
            throw new InvalidLengthException();
        }
        if (!Strings.isDigits(n)) {
            throw new InvalidFormatException();
        }
        return n;
    }

    /** Whether the number is a valid NIT written without its check digit. Never throws. */
    public static boolean isValidBase(String base) {
        try {
            validateBase(base);
            return true;
        } catch (ValidationException e) {
            return false;
        }
    }

    /**
     * The presentation of a NIT written without its check digit: the base
     * grouped in thousands, as {@link #format} writes it before the DV
     * ({@code 900.373.115}).
     *
     * @throws ValidationException if the base is not valid
     */
    public static String formatBase(String base) {
        return group(validateBase(base));
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (n.length() < 8 || n.length() > 16) {
            throw new InvalidLengthException();
        }
        if (!Strings.isDigits(n)) {
            throw new InvalidFormatException();
        }
        if (n.charAt(n.length() - 1) != calcCheckDigit(n.substring(0, n.length() - 1))) {
            throw new InvalidChecksumException();
        }
        return n;
    }

    @Override
    public String format(String number) {
        String n = validate(number);
        return group(n.substring(0, n.length() - 1)) + '-' + n.charAt(n.length() - 1);
    }

    /** The digits grouped in thousands with dots, from the right. */
    private static String group(String digits) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < digits.length(); i++) {
            if (i > 0 && (digits.length() - i) % 3 == 0) {
                sb.append('.');
            }
            sb.append(digits.charAt(i));
        }
        return sb.toString();
    }
}
