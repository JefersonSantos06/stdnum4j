package io.github.jefersonsantos06.stdnum.latam;

import io.github.jefersonsantos06.stdnum.algo.Weighted;
import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidComponentException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.util.Set;

/**
 * CUIT (Código Único de Identificación Tributaria), the Argentinian tax
 * number: a two-digit type, an eight-digit id and a check digit computed
 * with weights 5,4,3,2,7,6,5,4,3,2 (remainder 0 gives 0, remainder 1
 * gives 9).
 */
public final class ArCuit implements StdNum {

    public static final ArCuit INSTANCE = new ArCuit();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("ar.cuit", "CUIT")
                    .country("AR")
                    .title("Código Único de Identificación Tributaria")
                    .description("Argentinian tax number: type prefix, 8-digit id and a"
                            + " weighted mod 11 check digit.")
                    .tags(Tag.TAX, Tag.VAT)
                    .build();

    private static final int[] WEIGHTS = {5, 4, 3, 2, 7, 6, 5, 4, 3, 2};

    /** Individuals (20, 23, 24, 27), companies (30, 33, 34) and international (50, 51, 55). */
    private static final Set<String> TYPES =
            Set.of("20", "23", "24", "27", "30", "33", "34", "50", "51", "55");

    private ArCuit() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " -");
    }

    /** The check digit for the given 10-digit base. */
    public static char calcCheckDigit(String base) {
        int remainder = Weighted.weightedSum(base, WEIGHTS) % 11;
        return "012345678990".charAt(11 - remainder);
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
        if (!TYPES.contains(n.substring(0, 2))) {
            throw new InvalidComponentException("Unknown CUIT type prefix.");
        }
        if (n.charAt(10) != calcCheckDigit(n.substring(0, 10))) {
            throw new InvalidChecksumException();
        }
        return n;
    }

    @Override
    public String format(String number) {
        String n = validate(number);
        return n.substring(0, 2) + "-" + n.substring(2, 10) + "-" + n.substring(10);
    }
}
