package io.github.jefersonsantos06.stdnum.latam;

import io.github.jefersonsantos06.stdnum.algo.Luhn;
import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Resources;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.util.Set;

/**
 * Cédula, the Dominican national identification number: eleven digits
 * validated with the Luhn checksum.
 *
 * <p>A companion file lists the cédulas the authority issued that do not
 * satisfy the check digit; they are accepted before the checksum runs.</p>
 */
public final class DoCedula implements StdNum {

    public static final DoCedula INSTANCE = new DoCedula();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("do.cedula", "Cédula")
                    .country("DO")
                    .title("Cédula de identidad y electoral")
                    .description("Dominican identity number: 11 digits with a Luhn checksum.")
                    .tags(Tag.PERSON)
                    .build();

    private DoCedula() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " -");
    }

    /** Cedulas the authority issued that do not satisfy the check digit. */
    private static Set<String> whitelist() {
        return Resources.lines(DoCedula.class, "do-cedula-whitelist.txt");
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (!Strings.isDigits(n)) {
            throw new InvalidFormatException();
        }
        // the whitelist is consulted before length and checksum, because some
        // of its entries are shorter than eleven digits
        if (whitelist().contains(n)) {
            return n;
        }
        if (n.length() != 11) {
            throw new InvalidLengthException();
        }
        Luhn.validate(n);
        return n;
    }

    @Override
    public String format(String number) {
        String n = validate(number);
        // a handful of whitelisted cedulas are shorter than the canonical
        // eleven digits and have no place to put the separators
        return n.length() == 11
                ? n.substring(0, 3) + "-" + n.substring(3, 10) + "-" + n.substring(10)
                : n;
    }
}
