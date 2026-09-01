package io.github.jefersonsantos06.stdnum.eu;

import io.github.jefersonsantos06.stdnum.numdb.NumDb;
import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidComponentException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.util.Locale;
import java.util.Map;

/**
 * Abgabenkontonummer, the Austrian tax number: two digits naming the tax
 * office and seven more, the last a check digit.
 *
 * <p>The office is what makes the first two digits meaningful, so a number
 * whose office was never issued is refused however well its check digit
 * works out.</p>
 */
public final class AtTin implements StdNum {

    public static final AtTin INSTANCE = new AtTin();

    /** The doubled value of each digit, as the checksum counts it. */
    private static final String DOUBLED = "0246813579";

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("at.tin", "Abgabenkontonummer")
                    .country("AT")
                    .title("Osterreichische Abgabenkontonummer")
                    .description("Austrian tax number: a two-digit tax office and seven more"
                            + " digits, the last a mod 10 check digit.")
                    .tags(Tag.PERSON, Tag.COMPANY, Tag.TAX)
                    .references("https://de.wikipedia.org/wiki/Abgabenkontonummer")
                    .build();

    private AtTin() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    /** The tax offices, keyed by the two digits a number opens with. */
    private static NumDb offices() {
        return NumDb.load(AtTin.class, "at-fa.dat");
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " -./,");
    }

    /** The check digit of a number, from its first eight digits. */
    public static char calcCheckDigit(String number) {
        String n = INSTANCE.compact(number);
        int sum = 0;
        for (int i = 0; i < 8 && i < n.length(); i++) {
            int digit = n.charAt(i) - '0';
            if (digit < 0 || digit > 9) {
                throw new InvalidFormatException();
            }
            sum += i % 2 == 1 ? DOUBLED.charAt(digit) - '0' : digit;
        }
        return (char) ('0' + (10 - sum % 10) % 10);
    }

    /** The tax office the number belongs to, or an empty map if it names none. */
    public static Map<String, String> info(String number) {
        String n = INSTANCE.compact(number);
        if (n.length() < 2) {
            throw new InvalidLengthException();
        }
        return offices().info(n.substring(0, 2)).get(0).properties();
    }

    /**
     * An office name reduced to the letters that identify it, so that
     * spelling and punctuation stop mattering.
     */
    private static String key(String office) {
        StringBuilder sb = new StringBuilder();
        for (char c : office.toLowerCase(Locale.ROOT).toCharArray()) {
            // a and u are left out, the offices being named inconsistently
            // around umlauts and the word fur
            if (c >= 'b' && c <= 'z' && c != 'u') {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    @Override
    public String validate(String number) {
        return validate(number, null);
    }

    /**
     * Validates the number, optionally checking that it belongs to the named
     * tax office.
     */
    public String validate(String number, String office) {
        String n = compact(number);
        if (n.length() != 9) {
            throw new InvalidLengthException();
        }
        if (!Strings.isDigits(n)) {
            throw new InvalidFormatException();
        }
        if (n.charAt(8) != calcCheckDigit(n)) {
            throw new InvalidChecksumException();
        }
        Map<String, String> info = info(n);
        if (info.isEmpty()) {
            throw new InvalidComponentException("Not the number of a tax office.");
        }
        if (office != null && !key(info.getOrDefault("office", "")).equals(key(office))) {
            throw new InvalidComponentException("Not a number of the " + office + " office.");
        }
        return n;
    }
}
