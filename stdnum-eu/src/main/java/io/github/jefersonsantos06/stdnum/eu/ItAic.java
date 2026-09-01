package io.github.jefersonsantos06.stdnum.eu;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidComponentException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.util.Locale;

/**
 * AIC, the code Italy gives a medicinal product: nine digits opening with a
 * zero and closing with a check digit.
 *
 * <p>The package also carries the same number in a six-character base-32
 * form, which is what the barcode holds; both are accepted.</p>
 */
public final class ItAic implements StdNum {

    public static final ItAic INSTANCE = new ItAic();

    /** The base-32 alphabet: the digits and the consonants bar A, E, I and O. */
    private static final String BASE32 = "0123456789BCDFGHJKLMNPQRSTUVWXYZ";
    private static final int[] WEIGHTS = {1, 2, 1, 2, 1, 2, 1, 2};

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("it.aic", "AIC")
                    .country("IT")
                    .title("Autorizzazione allImmissione in Commercio")
                    .description("Italian medicinal product code: 9 digits with a check digit,"
                            + " or the same number in a 6-character base-32 form.")
                    .tags(Tag.PRODUCT, Tag.HEALTH)
                    .references("https://www.aifa.gov.it/")
                    .build();

    private ItAic() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " ").toUpperCase(Locale.ROOT);
    }

    /** The base-10 form of a base-32 code. */
    public static String toBase10(String number) {
        String n = INSTANCE.compact(number);
        long value = 0;
        for (int i = 0; i < n.length(); i++) {
            int digit = BASE32.indexOf(n.charAt(i));
            if (digit < 0) {
                throw new InvalidFormatException();
            }
            value = value * 32 + digit;
        }
        String s = Long.toString(value);
        return "0".repeat(Math.max(0, 9 - s.length())) + s;
    }

    /** The base-32 form of a base-10 code. */
    public static String toBase32(String number) {
        String n = INSTANCE.compact(number);
        if (!Strings.isDigits(n)) {
            throw new InvalidFormatException();
        }
        long remainder = Long.parseLong(n);
        StringBuilder sb = new StringBuilder();
        while (remainder > 31) {
            sb.insert(0, BASE32.charAt((int) (remainder % 32)));
            remainder /= 32;
        }
        sb.insert(0, BASE32.charAt((int) remainder));
        while (sb.length() < 6) {
            sb.insert(0, '0');
        }
        return sb.toString();
    }

    /** The check digit of a base-10 code, from its first eight digits. */
    public static char calcCheckDigit(String number) {
        String n = INSTANCE.compact(number);
        int sum = 0;
        for (int i = 0; i < WEIGHTS.length && i < n.length(); i++) {
            int product = WEIGHTS[i] * (n.charAt(i) - '0');
            sum += product / 10 + product % 10;
        }
        return (char) ('0' + sum % 10);
    }

    /** Validates the nine-digit base-10 form. */
    public static String validateBase10(String number) {
        String n = INSTANCE.compact(number);
        if (n.length() != 9) {
            throw new InvalidLengthException();
        }
        if (!Strings.isDigits(n)) {
            throw new InvalidFormatException();
        }
        if (n.charAt(0) != '0') {
            throw new InvalidComponentException("A product code starts with 0.");
        }
        if (n.charAt(8) != calcCheckDigit(n)) {
            throw new InvalidChecksumException();
        }
        return n;
    }

    /**
     * {@inheritDoc}
     *
     * <p>A base-32 code is checked by converting it, but is returned as it
     * was given: this returns the compact form, as every type here does.
     * Use {@link #toBase10(String)} for the converted one.</p>
     */
    @Override
    public String validate(String number) {
        String n = compact(number);
        if (n.length() == 6) {
            validateBase10(toBase10(n));
            return n;
        }
        return validateBase10(n);
    }
}
