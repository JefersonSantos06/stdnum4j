package io.github.jefersonsantos06.stdnum.latam;

import io.github.jefersonsantos06.stdnum.spi.*;
import io.github.jefersonsantos06.stdnum.text.Strings;

/**
 * RUC (Registro Único de Contribuyentes), the Nicaraguan taxpayer number:
 * fourteen characters in one of two shapes. A natural person with a cédula
 * carries their {@link NiCedula}, which is where the check letter is. Everyone
 * else carries a letter and thirteen digits: J for a legal entity, N for a
 * national without a cédula, R for a resident foreigner and E for a
 * non-resident one.
 *
 * <p>The tax authority gave the lettered forms no published check digit and
 * no published meaning for their digits, so they are taken on their character
 * set, their length and their letter alone, and thirteen zeros are refused.
 * Its 2022 notice on valid RUC numbers also refuses any that opens with four
 * zeros. Ten-digit numbers were retired on 1 July 2013 and are refused.</p>
 */
public final class NiRuc implements StdNum {

    public static final NiRuc INSTANCE = new NiRuc();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("ni.ruc", "RUC")
                    .country("NI")
                    .title("Registro Único de Contribuyentes")
                    .description("Nicaraguan taxpayer number: 14 characters, either a natural"
                            + " person's cédula or a J, N, R or E letter and 13 digits.")
                    .tags(Tag.TAX, Tag.VAT)
                    .references("https://www.dgi.gob.ni/pdfNoticia/1715",
                            "http://nicaragua.justia.com/nacionales/disposiciones-administrativas"
                            + "/inscripcion-en-la-ventanilla-electronica-tributaria-vet-y-actualizacion"
                            + "-de-numero-ruc-jan-30-2013/gdoc/")
                    .build();

    private static final Message LEADING_ZEROS = Message.of(NiRuc.class, "ruc.ni.leading-zeros",
            "A RUC does not start with four zeros.");

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
            if ("JNRE".indexOf(first) < 0) {
                throw new InvalidComponentException(Message.of(NiRuc.class, "ruc.prefix",
                        "A RUC opens with J, N, R or E, or is a cédula."));
            }
            if (n.startsWith("0000000000000", 1)) {
                throw new InvalidComponentException(Reasons.zeroSequence());
            }
            return n;
        }
        if (Strings.isDigits(n.substring(0, 13)) && n.startsWith("0000")) {
            throw new InvalidComponentException(LEADING_ZEROS);
        }
        return NiCedula.INSTANCE.validate(n);
    }
}
