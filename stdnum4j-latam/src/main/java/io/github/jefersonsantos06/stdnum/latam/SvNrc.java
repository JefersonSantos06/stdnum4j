package io.github.jefersonsantos06.stdnum.latam;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidComponentException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.Reasons;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

/**
 * NRC (Número de Registro de Contribuyente), the number under which the
 * Salvadoran Ministry of Finance registers a taxpayer for IVA: a sequence and
 * a verification digit, written {@code 123456-7}. A tax credit invoice
 * (Comprobante de Crédito Fiscal) carries the NRC of both parties beside
 * their {@link SvNit}.
 *
 * <p>The Ministry keeps the verification digit's algorithm to itself, so
 * only the shape is checked: the electronic invoice schemas take the NRC as
 * up to eight digits, without the hyphen.</p>
 */
public final class SvNrc implements StdNum {

    public static final SvNrc INSTANCE = new SvNrc();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("sv.nrc", "NRC")
                    .country("SV")
                    .title("Número de Registro de Contribuyente")
                    .description("Salvadoran IVA registration number: up to 7 digits and a"
                            + " verification digit, written 123456-7.")
                    .tags(Tag.TAX)
                    .references(
                            "https://github.com/hherzl/InvoizR/blob/HEAD/Resources/MH/svfe-json-schemas/fe-ccf-v3.json")
                    .build();

    private SvNrc() {
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
        if (n.length() < 2 || n.length() > 8) {
            throw new InvalidLengthException();
        }
        String sequence = n.substring(0, n.length() - 1);
        if (Strings.allSame(sequence) && sequence.charAt(0) == '0') {
            throw new InvalidComponentException(Reasons.zeroSequence());
        }
        return n;
    }

    @Override
    public String format(String number) {
        String n = validate(number);
        return n.substring(0, n.length() - 1) + '-' + n.charAt(n.length() - 1);
    }
}
