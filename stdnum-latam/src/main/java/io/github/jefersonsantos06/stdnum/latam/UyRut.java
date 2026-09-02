package io.github.jefersonsantos06.stdnum.latam;

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

import java.util.Locale;

/**
 * RUT (Registro Único Tributario), the Uruguayan tax number for legal
 * entities: a two-digit registration number (01-22), a six-digit sequence,
 * the literal {@code 001} and a check digit.
 */
public final class UyRut implements StdNum {

    public static final UyRut INSTANCE = new UyRut();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("uy.rut", "RUT")
                    .country("UY")
                    .title("Registro Único Tributario")
                    .description("Uruguayan tax number: 12 digits with a weighted mod 11"
                            + " check digit.")
                    .tags(Tag.TAX, Tag.VAT, Tag.COMPANY)
                    .build();

    private static final int[] WEIGHTS = {4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};

    private UyRut() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        String n = Strings.compact(number, " -").toUpperCase(Locale.ROOT);
        return n.startsWith("UY") ? n.substring(2) : n;
    }

    /** The check digit for the eleven-digit base. */
    public static char calcCheckDigit(String base) {
        int sum = 0;
        for (int i = 0; i < WEIGHTS.length && i < base.length(); i++) {
            sum += WEIGHTS[i] * (base.charAt(i) - '0');
        }
        return (char) ('0' + Math.floorMod(-sum, 11));
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (n.length() != 12) {
            throw new InvalidLengthException();
        }
        if (!Strings.isDigits(n)) {
            throw new InvalidFormatException();
        }
        String registration = n.substring(0, 2);
        if (registration.compareTo("01") < 0 || registration.compareTo("22") > 0) {
            throw new InvalidComponentException(Message.of(UyRut.class, "rut.registration",
                    "Unknown RUT registration number."));
        }
        if (n.startsWith("000000", 2)) {
            throw new InvalidComponentException(Reasons.zeroSequence());
        }
        if (!n.startsWith("001", 8)) {
            throw new InvalidComponentException(Message.of(UyRut.class, "rut.constant",
                    "A RUT carries 001 before the check digit."));
        }
        if (n.charAt(11) != calcCheckDigit(n.substring(0, 11))) {
            throw new InvalidChecksumException();
        }
        return n;
    }

    @Override
    public String format(String number) {
        String n = validate(number);
        return n.substring(0, 2) + "-" + n.substring(2, 8) + "-"
                + n.substring(8, 11) + "-" + n.substring(11);
    }
}
