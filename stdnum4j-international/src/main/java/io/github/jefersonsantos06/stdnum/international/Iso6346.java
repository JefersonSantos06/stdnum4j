package io.github.jefersonsantos06.stdnum.international;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Mask;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * ISO 6346 container code, the identifier marked on intermodal shipping
 * containers: a three-letter owner code, an equipment category
 * ({@code U}, {@code J}, {@code Z} or {@code R}), a six-digit serial and a
 * check digit weighted by powers of two.
 */
public final class Iso6346 implements StdNum {

    public static final Iso6346 INSTANCE = new Iso6346();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("iso6346", "Container code")
                    .title("ISO 6346 container identification")
                    .description("Shipping container code: owner, category, serial and a"
                            + " power-of-two weighted check digit.")
                    .tags(Tag.VEHICLE)
                    .references("https://en.wikipedia.org/wiki/ISO_6346")
                    .build();

    private static final Mask MASK = Mask.of("#### ###### #");

    private static final Pattern STRUCTURE = Pattern.compile("[A-Z]{3}[UJZR][0-9]{7}");

    /** Letters skip the multiples of eleven, which are never used. */
    private static final String ALPHABET = "0123456789A BCDEFGHIJK LMNOPQRSTU VWXYZ";

    private Iso6346() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " ").toUpperCase(Locale.ROOT);
    }

    /** The check digit for the ten-character owner code and serial. */
    public static char calcCheckDigit(String base) {
        String b = INSTANCE.compact(base);
        int sum = 0;
        for (int i = 0; i < b.length(); i++) {
            int value = ALPHABET.indexOf(b.charAt(i));
            if (value < 0) {
                throw new InvalidFormatException();
            }
            sum += value * (1 << i);
        }
        return (char) ('0' + sum % 11 % 10);
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (n.length() != 11) {
            throw new InvalidLengthException();
        }
        if (!STRUCTURE.matcher(n).matches()) {
            throw new InvalidFormatException();
        }
        if (n.charAt(10) != calcCheckDigit(n.substring(0, 10))) {
            throw new InvalidChecksumException();
        }
        return n;
    }

    @Override
    public String format(String number) {
        return MASK.fill(validate(number));
    }

    @Override
    public List<Mask> masks() {
        return List.of(MASK);
    }
}
