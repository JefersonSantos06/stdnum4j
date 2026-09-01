package io.github.jefersonsantos06.stdnum.eu;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * NIPT / NUIS (Numri i Identifikimit për Personin e Tatueshëm), the
 * Albanian tax number: a letter from {@code A} to {@code M}, eight digits
 * and a trailing letter. The number carries no check digit.
 */
public final class AlNipt implements StdNum {

    public static final AlNipt INSTANCE = new AlNipt();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("al.nipt", "NIPT")
                    .country("AL")
                    .title("Numri i Identifikimit për Personin e Tatueshëm")
                    .description("Albanian tax number: a letter A-M, 8 digits and a letter,"
                            + " with no check digit.")
                    .tags(Tag.TAX, Tag.VAT)
                    .build();

    private static final Pattern STRUCTURE = Pattern.compile("[A-M][0-9]{8}[A-Z]");

    private AlNipt() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        String n = Strings.compact(number, " ").toUpperCase(Locale.ROOT);
        if (n.startsWith("(AL)")) {
            return n.substring(4);
        }
        return n.startsWith("AL") ? n.substring(2) : n;
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (n.length() != 10) {
            throw new InvalidLengthException();
        }
        if (!STRUCTURE.matcher(n).matches()) {
            throw new InvalidFormatException();
        }
        return n;
    }
}
