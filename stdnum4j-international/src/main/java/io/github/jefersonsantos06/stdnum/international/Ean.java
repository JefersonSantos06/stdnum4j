package io.github.jefersonsantos06.stdnum.international;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

/**
 * EAN (International Article Number) and the wider GTIN family.
 *
 * <p>Accepts EAN-8, UPC-A (12), EAN-13 and GTIN-14 numbers, all closed by
 * the GS1 check digit: alternating weights 1 and 3 from the right, sum
 * modulo 10 equal to zero.</p>
 */
public final class Ean implements StdNum {

    public static final Ean INSTANCE = new Ean();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("ean", "EAN")
                    .title("International Article Number")
                    .description("GS1 article numbers (EAN-8, UPC-A, EAN-13, GTIN-14) with the"
                            + " alternating 1-3 weighted check digit.")
                    .tags(Tag.PRODUCT)
                    .references("https://en.wikipedia.org/wiki/International_Article_Number")
                    .build();

    private Ean() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " -");
    }

    /**
     * The GS1 checksum of the full number (check digit included); valid
     * numbers yield 0.
     */
    public static int checksum(String number) {
        if (number == null || number.isEmpty() || !Strings.isDigits(number)) {
            throw new InvalidFormatException();
        }
        int sum = 0;
        for (int i = number.length() - 1, weight = 1; i >= 0; i--, weight = 4 - weight) {
            sum += (number.charAt(i) - '0') * weight;
        }
        return sum % 10;
    }

    /** The check digit to append to {@code base} to make it valid. */
    public static char calcCheckDigit(String base) {
        String b = INSTANCE.compact(base);
        int sum = checksum(b + "0");
        return (char) ('0' + (10 - sum) % 10);
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (!Strings.isDigits(n)) {
            throw new InvalidFormatException();
        }
        int length = n.length();
        if (length != 8 && length != 12 && length != 13 && length != 14) {
            throw new InvalidLengthException();
        }
        if (checksum(n) != 0) {
            throw new InvalidChecksumException();
        }
        return n;
    }
}
