package io.github.jefersonsantos06.stdnum.eu;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidComponentException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.Message;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Mask;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.util.Locale;

/**
 * Ondernemingsnummer (BTW, TVA, NWSt), the Belgian enterprise and VAT
 * number: ten digits starting with 0 or 1, valid when the first eight
 * digits plus the last two are a multiple of 97. Nine-digit numbers from
 * the old format are zero-padded.
 */
public final class BeVat implements StdNum {

    public static final BeVat INSTANCE = new BeVat();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("be.vat", "Ondernemingsnummer")
                    .country("BE")
                    .title("Ondernemingsnummer (BTW, TVA, NWSt)")
                    .description("Belgian enterprise/VAT number: 10 digits with a mod 97"
                            + " check on the last two.")
                    .tags(Tag.VAT, Tag.COMPANY)
                    .build();

    private static final Mask MASK = Mask.of("####.###.###");

    private BeVat() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        String n = Strings.compact(number, " -./").toUpperCase(Locale.ROOT);
        if (n.startsWith("BE")) {
            n = n.substring(2);
        }
        if (n.startsWith("(0)")) {
            n = "0" + n.substring(3);
        }
        return n.length() == 9 ? "0" + n : n;
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
        if (Long.parseLong(n) <= 0) {
            throw new InvalidFormatException(Message.of(BeVat.class, "vat.be.positive",
                    "An enterprise number must be greater than zero."));
        }
        if (n.charAt(0) != '0' && n.charAt(0) != '1') {
            throw new InvalidComponentException(Message.of(BeVat.class, "vat.be.prefix",
                    "A Belgian enterprise number starts with 0 or 1."));
        }
        if ((Long.parseLong(n.substring(0, 8)) + Long.parseLong(n.substring(8))) % 97 != 0) {
            throw new InvalidChecksumException();
        }
        return n;
    }

    @Override
    public String format(String number) {
        return MASK.fill(validate(number));
    }
}
