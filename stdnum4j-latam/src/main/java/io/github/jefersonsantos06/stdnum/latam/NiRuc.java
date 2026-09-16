package io.github.jefersonsantos06.stdnum.latam;

import io.github.jefersonsantos06.stdnum.spi.*;
import io.github.jefersonsantos06.stdnum.text.Strings;

/**
 * RUC (Registro Único de Contribuyentes), the Nicaraguan taxpayer number:
 * fourteen characters in one of two shapes. A legal entity carries a J and
 * thirteen digits; a natural person carries their {@link NiCedula}, which is
 * where the check letter is.
 *
 * <p>The tax authority gave the J form no published check digit and no
 * published meaning for its digits, so it is taken on its character set, its
 * length and its prefix alone. Ten-digit numbers were retired on 1 July 2013
 * and are refused.</p>
 */
public final class NiRuc implements StdNum {

    public static final NiRuc INSTANCE = new NiRuc();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("ni.ruc", "RUC")
                    .country("NI")
                    .title("Registro Único de Contribuyentes")
                    .description("Nicaraguan taxpayer number: 14 characters, either a J and"
                            + " 13 digits for a legal entity or a natural person's cédula.")
                    .tags(Tag.TAX, Tag.VAT)
                    .references("http://nicaragua.justia.com/nacionales/disposiciones-administrativas"
                            + "/inscripcion-en-la-ventanilla-electronica-tributaria-vet-y-actualizacion"
                            + "-de-numero-ruc-jan-30-2013/gdoc/")
                    .build();

    private NiRuc() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return NiCedula.INSTANCE.compact(number);
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (n.length() != 14) {
            throw new InvalidLengthException();
        }
        char first = n.charAt(0);
        if (first >= 'A' && first <= 'Z') {
            if (!Strings.isDigits(n.substring(1))) {
                throw new InvalidFormatException();
            }
            if (first != 'J') {
                throw new InvalidComponentException(Message.of(NiRuc.class, "ruc.prefix",
                        "A RUC of a legal entity starts with J."));
            }
            return n;
        }
        return NiCedula.INSTANCE.validate(n);
    }
}
