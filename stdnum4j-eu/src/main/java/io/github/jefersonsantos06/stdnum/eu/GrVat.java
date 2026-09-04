package io.github.jefersonsantos06.stdnum.eu;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.util.Locale;

/**
 * ΑΦΜ (Αριθμός Φορολογικού Μητρώου), the Greek VAT number: nine digits
 * whose check digit doubles a running sum. Eight-digit numbers from the old
 * format are zero-padded, and both the {@code EL} and {@code GR} prefixes
 * are accepted.
 */
public final class GrVat implements StdNum {

    public static final GrVat INSTANCE = new GrVat();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("gr.vat", "ΑΦΜ")
                    .country("GR")
                    .title("Αριθμός Φορολογικού Μητρώου")
                    .description("Greek VAT number: 9 digits with a doubling-sum check digit.")
                    .tags(Tag.VAT)
                    .build();

    private GrVat() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        String n = Strings.compact(number, " -./:").toUpperCase(Locale.ROOT);
        if (n.startsWith("EL") || n.startsWith("GR")) {
            n = n.substring(2);
        }
        return n.length() == 8 ? "0" + n : n;
    }

    /** The check digit for the eight-digit base. */
    public static char calcCheckDigit(String base) {
        int checksum = 0;
        for (int i = 0; i < base.length(); i++) {
            checksum = checksum * 2 + (base.charAt(i) - '0');
        }
        return (char) ('0' + checksum * 2 % 11 % 10);
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (!Strings.isDigits(n)) {
            throw new InvalidFormatException();
        }
        if (n.length() != 9) {
            throw new InvalidLengthException();
        }
        if (n.charAt(8) != calcCheckDigit(n.substring(0, 8))) {
            throw new InvalidChecksumException();
        }
        return n;
    }
}
