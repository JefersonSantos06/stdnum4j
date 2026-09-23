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
 * CR, the number under which a Saudi business is entered in the commercial
 * register: ten digits. Older numbers open with the code of the registry
 * office that issued them — SABIC's, from Riyadh, is 1010010813. Since the
 * Commercial Register Law took effect in April 2025, an establishment's sole
 * identifier is its Unified National Number, ten digits beginning with 7,
 * which ZATCA's e-invoices carry under the scheme 700.
 *
 * <p>Neither kind carries a check digit, so validation is limited to ten
 * digits that do not start with 0.</p>
 */
public final class SaCr implements StdNum {

    public static final SaCr INSTANCE = new SaCr();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("sa.cr", "CR")
                    .country("SA")
                    .title("Commercial Registration number")
                    .description("Saudi commercial register number: 10 digits, the Unified National"
                            + " Number beginning with 7 since 2025.")
                    .tags(Tag.COMPANY)
                    .references(
                            "https://rulebook.sama.gov.sa/en/emphasizing-implementation-provisions-commercial-register-law-and-law-tradenames",
                            "https://github.com/odoo/odoo/blob/master/addons/l10n_sa_edi/models/res_partner.py")
                    .build();

    private SaCr() {
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
        if (n.length() != 10) {
            throw new InvalidLengthException();
        }
        if (n.charAt(0) == '0') {
            throw new InvalidComponentException(Message.of(SaCr.class, "cr.sa-office",
                    "A commercial register number does not start with 0."));
        }
        return n;
    }
}
