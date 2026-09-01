package io.github.jefersonsantos06.stdnum.eu;

import io.github.jefersonsantos06.stdnum.algo.Luhn;
import io.github.jefersonsantos06.stdnum.algo.Mod97;
import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

/**
 * Kontonummer, the Norwegian bank account number: eleven digits closing with
 * a weighted mod 11 check digit. A postgiro account is seven digits with a
 * Luhn check digit, and is also written with the bank code 0000 in front,
 * which is stripped.
 */
public final class NoKontonr implements StdNum {

    public static final NoKontonr INSTANCE = new NoKontonr();

    private static final int[] WEIGHTS = {6, 7, 8, 9, 4, 5, 6, 7, 8, 9};

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("no.kontonr", "Kontonummer")
                    .country("NO")
                    .title("Norsk kontonummer")
                    .description("Norwegian bank account number: 11 digits with a weighted"
                            + " mod 11 check digit, or a 7-digit postgiro account.")
                    .tags(Tag.BANK)
                    .references("https://no.wikipedia.org/wiki/Kontonummer")
                    .build();

    private NoKontonr() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        String n = Strings.compact(number, " .-");
        // 0000 is the postgiro bank code rather than part of the account
        return n.startsWith("0000") ? n.substring(4) : n;
    }

    /**
     * The check digit of an eleven-digit account, from its first ten digits.
     *
     * @throws InvalidChecksumException when the weighted sum leaves a
     *                                  remainder of 10, for which no check
     *                                  digit exists
     */
    public static char calcCheckDigit(String base) {
        String n = INSTANCE.compact(base);
        int sum = 0;
        for (int i = 0; i < WEIGHTS.length && i < n.length(); i++) {
            sum += WEIGHTS[i] * (n.charAt(i) - '0');
        }
        int check = sum % 11;
        if (check == 10) {
            throw new InvalidChecksumException("No valid check digit exists for this number.");
        }
        return (char) ('0' + check);
    }

    /** The IBAN this account is held under, in its compact form. */
    public static String toIban(String number) {
        String n = INSTANCE.validate(number);
        return "NO" + Mod97.calcCheckDigits(n + "NO") + n;
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (!Strings.isDigits(n)) {
            throw new InvalidFormatException();
        }
        if (n.length() == 7) {
            Luhn.validate(n);
        } else if (n.length() == 11) {
            if (n.charAt(10) != calcCheckDigit(n)) {
                throw new InvalidChecksumException();
            }
        } else {
            throw new InvalidLengthException();
        }
        return n;
    }

    @Override
    public String format(String number) {
        String n = validate(number);
        String padded = "0".repeat(11 - n.length()) + n;
        return padded.substring(0, 4) + '.' + padded.substring(4, 6) + '.' + padded.substring(6);
    }
}
