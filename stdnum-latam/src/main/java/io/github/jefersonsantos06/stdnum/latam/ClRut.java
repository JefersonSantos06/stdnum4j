package io.github.jefersonsantos06.stdnum.latam;

import io.github.jefersonsantos06.stdnum.algo.Weighted;
import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.util.Locale;

/**
 * RUT (Rol Único Tributario), the Chilean tax and national identification
 * number: seven or eight digits and a check character (a digit or K)
 * computed modulo 11 with cyclic weights 2..7 from the right. An optional
 * {@code CL} prefix is accepted.
 */
public final class ClRut implements StdNum {

    public static final ClRut INSTANCE = new ClRut();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("cl.rut", "RUT")
                    .country("CL")
                    .title("Rol Único Tributario")
                    .description("Chilean tax/identity number: 7-8 digits and a mod 11"
                            + " check character (0-9 or K).")
                    .tags(Tag.TAX, Tag.PERSON, Tag.VAT)
                    .build();

    private ClRut() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        String n = Strings.compact(number, " -.").toUpperCase(Locale.ROOT);
        return n.startsWith("CL") ? n.substring(2) : n;
    }

    /** The check character for the base without its check position. */
    public static char calcCheckDigit(String base) {
        int[] weights = Weighted.cyclic(base.length(), 2, 3, 4, 5, 6, 7);
        int check = 11 - Weighted.weightedSum(base, weights) % 11;
        return check == 11 ? '0' : check == 10 ? 'K' : (char) ('0' + check);
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (n.length() != 8 && n.length() != 9) {
            throw new InvalidLengthException();
        }
        if (!Strings.isDigits(n.substring(0, n.length() - 1))) {
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
        int split = n.length() - 7;
        return n.substring(0, split) + "." + n.substring(split, split + 3) + "."
                + n.substring(split + 3, split + 6) + "-" + n.substring(n.length() - 1);
    }
}
