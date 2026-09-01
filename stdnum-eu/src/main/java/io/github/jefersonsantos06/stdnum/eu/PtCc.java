package io.github.jefersonsantos06.stdnum.eu;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * The number of the Portuguese Cartao de Cidadao: the civil identification
 * number, two version characters and a check digit. The check digit is a
 * Luhn computed over base-36 values, so the letters take part in it.
 */
public final class PtCc implements StdNum {

    public static final PtCc INSTANCE = new PtCc();

    private static final String ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final Pattern PATTERN = Pattern.compile("[0-9]*[A-Z0-9]{2}[0-9]");

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("pt.cc", "CC")
                    .country("PT")
                    .title("Numero de Cartao de Cidadao")
                    .description("Portuguese citizen card number: the civil identification"
                            + " number, two version characters and a base-36 Luhn check digit.")
                    .tags(Tag.PERSON)
                    .references("https://www.autenticacao.gov.pt/cartao-de-cidadao")
                    .build();

    private PtCc() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " ").toUpperCase(Locale.ROOT);
    }

    /**
     * The check digit of a number, from everything before it. Values come
     * from the base-36 alphabet but the doubling and the modulus stay
     * decimal, as in an ordinary Luhn.
     */
    public static char calcCheckDigit(String base) {
        String n = INSTANCE.compact(base);
        int sum = 0;
        for (int i = 0; i < n.length(); i++) {
            int value = ALPHABET.indexOf(n.charAt(n.length() - 1 - i));
            if (value < 0) {
                throw new InvalidFormatException();
            }
            if (i % 2 == 0) {
                value *= 2;
                if (value > 9) {
                    value -= 9;
                }
            }
            sum += value;
        }
        return (char) ('0' + (10 - sum % 10) % 10);
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (!PATTERN.matcher(n).matches()) {
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
        if (n.length() < 5) {
            return n;
        }
        return n.substring(0, n.length() - 4) + ' ' + n.charAt(n.length() - 4)
                + ' ' + n.substring(n.length() - 3);
    }
}
