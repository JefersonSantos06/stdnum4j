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
 * TRN (Tax Registration Number), the VAT number the Federal Tax Authority of
 * the United Arab Emirates gives a registered business: fifteen digits that
 * start with 1 and end with 03. A tax invoice must carry the supplier's.
 *
 * <p>No check digit is published, so validation is the one the UAE's
 * electronic invoicing specification (PINT AE, rule IBR-132-AE) applies to
 * every VAT identifier: fifteen digits, a 1 first and 03 last.</p>
 */
public final class AeTrn implements StdNum {

    public static final AeTrn INSTANCE = new AeTrn();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("ae.trn", "TRN")
                    .country("AE")
                    .title("Tax Registration Number")
                    .description("UAE VAT registration number: 15 digits starting with 1 and"
                            + " ending with 03.")
                    .tags(Tag.VAT, Tag.TAX)
                    .references(
                            "https://docs.peppol.eu/poac/ae/v1.0.2/pint-ae-sb/trn-creditnote/rule/PINT-jurisdiction-aligned-rules/")
                    .build();

    private AeTrn() {
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
        if (n.charAt(0) != '1' || !n.endsWith("03")) {
            throw new InvalidComponentException(Message.of(AeTrn.class, "trn.edges",
                    "A TRN starts with 1 and ends with 03."));
        }
        return n;
    }
}
