package io.github.jefersonsantos06.stdnum.latam;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidComponentException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.Message;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.util.Set;

/**
 * NIT (Número de Identificación Tributaria), the Bolivian tax number: a base
 * number, a two-digit taxpayer code and a final digit. A person born in
 * Bolivia, or their sole proprietorship, takes the number of their identity
 * card and the code 01; the SIN numbers companies itself, with the code 02,
 * and foreign residents, with the code 04.
 *
 * <p>The number carries no check digit, so validation is limited to its
 * length and the taxpayer code. The SIN calls the last digit random; it does
 * close a Verhoeff check in 57 of the 89 NITs gathered in python-stdnum issue
 * #134, too few to be a rule, and some published NITs end in 0 although the
 * SIN says it is never 0 — so it is not checked either.</p>
 */
public final class BoNit implements StdNum {

    public static final BoNit INSTANCE = new BoNit();

    /** People and sole proprietorships (01), companies (02), foreign residents (04). */
    private static final Set<String> TAXPAYER_CODES = Set.of("01", "02", "04");

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("bo.nit", "NIT")
                    .country("BO")
                    .title("Número de Identificación Tributaria")
                    .description("Bolivian tax number: a base number, a 2-digit taxpayer code"
                            + " (01, 02 or 04) and a final digit.")
                    .tags(Tag.TAX, Tag.VAT)
                    .references(
                            "https://siatinfo.impuestos.gob.bo/index.php/requisitos-para-la-inscripcion/conceptos-generales/generacion-del-nit",
                            "https://github.com/arthurdejong/python-stdnum/issues/134")
                    .build();

    private BoNit() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " -");
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (!Strings.isDigits(n)) {
            throw new InvalidFormatException();
        }
        if (n.length() < 8 || n.length() > 12) {
            throw new InvalidLengthException();
        }
        if (!TAXPAYER_CODES.contains(n.substring(n.length() - 3, n.length() - 1))) {
            throw new InvalidComponentException(Message.of(BoNit.class, "nit.taxpayer-code",
                    "Not a taxpayer code: 01, 02 or 04."));
        }
        return n;
    }
}
