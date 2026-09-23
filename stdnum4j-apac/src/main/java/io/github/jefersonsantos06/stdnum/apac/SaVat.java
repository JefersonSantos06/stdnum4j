package io.github.jefersonsantos06.stdnum.apac;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidComponentException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.Message;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

/**
 * VAT registration number of Saudi Arabia, the tax number ZATCA gives a
 * business: fifteen digits that start and end with 3. It identifies the
 * seller on every tax invoice and the buyer on a B2B one.
 *
 * <p>No check digit is published, so validation is the one ZATCA's
 * e-invoicing rules apply (BR-KSA-39 and BR-KSA-40 for the seller, BR-KSA-44
 * for the buyer): fifteen digits, the first and the last a 3.</p>
 */
public final class SaVat implements StdNum {

    public static final SaVat INSTANCE = new SaVat();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("sa.vat", "VAT")
                    .country("SA")
                    .title("Saudi VAT registration number")
                    .description("Saudi VAT number: 15 digits starting and ending with 3.")
                    .tags(Tag.VAT, Tag.TAX)
                    .references(
                            "https://github.com/odoo/odoo/blob/master/addons/l10n_sa_edi/models/res_company.py")
                    .build();

    private SaVat() {
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
        if (n.length() != 15) {
            throw new InvalidLengthException();
        }
        if (n.charAt(0) != '3' || n.charAt(14) != '3') {
            throw new InvalidComponentException(Message.of(SaVat.class, "vat.sa-edges",
                    "A Saudi VAT number starts and ends with 3."));
        }
        return n;
    }
}
