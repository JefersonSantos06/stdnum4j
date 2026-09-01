package io.github.jefersonsantos06.stdnum.africa;

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
 * NINEA (Numéro d'Identification National des Entreprises et Associations),
 * the Senegalese business tax identifier: seven or nine digits with a
 * mod 10 checksum, optionally followed by a three-character COFI giving the
 * company's tax status, tax centre and legal structure.
 */
public final class SnNinea implements StdNum {

    public static final SnNinea INSTANCE = new SnNinea();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("sn.ninea", "NINEA")
                    .country("SN")
                    .title("Numéro d'Identification National des Entreprises et Associations")
                    .description("Senegalese business tax number: 7 or 9 digits with a mod 10"
                            + " checksum, optionally followed by the 3-character COFI.")
                    .tags(Tag.TAX, Tag.COMPANY)
                    .build();

    private static final int[] WEIGHTS = {1, 2, 1, 2, 1, 2, 1, 2, 1};

    /** Tax centre letters used in the second position of the COFI. */
    private static final String TAX_CENTRES = "ABCDEFGHJKLMNPQRSTUVWZ";

    private SnNinea() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " -/,").toUpperCase(Locale.ROOT);
    }

    /** The mod 10 checksum of the numeric part; valid numbers yield 0. */
    static int checksum(String base) {
        String b = base.length() < 9 ? "0".repeat(9 - base.length()) + base : base;
        int sum = 0;
        for (int i = 0; i < WEIGHTS.length && i < b.length(); i++) {
            sum += WEIGHTS[i] * (b.charAt(i) - '0');
        }
        return sum % 10;
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        String cofi = "";
        String digits = n;
        if (n.length() > 9) {
            cofi = n.substring(n.length() - 3);
            digits = n.substring(0, n.length() - 3);
        }
        if (digits.length() != 7 && digits.length() != 9) {
            throw new InvalidLengthException();
        }
        if (!Strings.isDigits(digits)) {
            throw new InvalidFormatException();
        }
        if (!cofi.isEmpty()) {
            validateCofi(cofi);
        }
        if (checksum(digits) != 0) {
            throw new InvalidChecksumException();
        }
        return digits + cofi;
    }

    /** The COFI: a tax status digit, a tax centre letter and a structure digit. */
    private static void validateCofi(String cofi) {
        if ("012".indexOf(cofi.charAt(0)) < 0) {
            throw new InvalidComponentException("Unknown COFI tax status.");
        }
        if (TAX_CENTRES.indexOf(cofi.charAt(1)) < 0) {
            throw new InvalidComponentException("Unknown COFI tax centre.");
        }
        if (cofi.charAt(2) < '0' || cofi.charAt(2) > '9') {
            throw new InvalidComponentException("Unknown COFI legal structure.");
        }
    }
}
