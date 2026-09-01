package io.github.jefersonsantos06.stdnum.latam;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidComponentException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

/**
 * RUC (Registro Único de Contribuyentes), the Peruvian tax number: eleven
 * digits whose first two give the kind of taxpayer (10, 15, 17 or 20).
 * Personal numbers carry the DNI in positions 3 to 10.
 */
public final class PeRuc implements StdNum {

    public static final PeRuc INSTANCE = new PeRuc();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("pe.ruc", "RUC")
                    .country("PE")
                    .title("Registro Único de Contribuyentes")
                    .description("Peruvian tax number: 11 digits with a type prefix and a"
                            + " weighted mod 11 check digit.")
                    .tags(Tag.TAX, Tag.VAT)
                    .build();

    private static final int[] WEIGHTS = {5, 4, 3, 2, 7, 6, 5, 4, 3, 2};

    private PeRuc() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " ");
    }

    /** The check digit for the ten-digit base. */
    public static char calcCheckDigit(String base) {
        int sum = 0;
        for (int i = 0; i < WEIGHTS.length && i < base.length(); i++) {
            sum += WEIGHTS[i] * (base.charAt(i) - '0');
        }
        return (char) ('0' + (11 - sum % 11) % 10);
    }

    /** The DNI embedded in a personal RUC (one starting with 10). */
    public static String toDni(String number) {
        String n = INSTANCE.validate(number);
        if (!n.startsWith("10")) {
            throw new InvalidComponentException("Only personal RUC numbers carry a DNI.");
        }
        return n.substring(2, 10);
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (n.length() != 11) {
            throw new InvalidLengthException();
        }
        if (!Strings.isDigits(n)) {
            throw new InvalidFormatException();
        }
        String type = n.substring(0, 2);
        if (!type.equals("10") && !type.equals("15")
                && !type.equals("17") && !type.equals("20")) {
            throw new InvalidComponentException("Unknown RUC taxpayer type.");
        }
        if (n.charAt(10) != calcCheckDigit(n.substring(0, 10))) {
            throw new InvalidChecksumException();
        }
        return n;
    }
}
