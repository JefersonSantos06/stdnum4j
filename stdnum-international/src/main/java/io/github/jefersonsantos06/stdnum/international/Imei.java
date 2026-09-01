package io.github.jefersonsantos06.stdnum.international;

import io.github.jefersonsantos06.stdnum.algo.Luhn;
import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.util.Locale;

/**
 * IMEI (International Mobile Equipment Identity), the identifier of mobile
 * phones: 14 digits without the check digit, 15 with it (Luhn-checked), or
 * 16 for an IMEISV, which carries a software version instead.
 */
public final class Imei implements StdNum {

    public static final Imei INSTANCE = new Imei();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("imei", "IMEI")
                    .title("International Mobile Equipment Identity")
                    .description("Mobile phone identifier: 14, 15 (Luhn-checked) or 16"
                            + " digits (IMEISV).")
                    .tags(Tag.TELECOM)
                    .references("https://en.wikipedia.org/wiki/International_Mobile_Equipment_Identity")
                    .build();

    private Imei() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " -").toUpperCase(Locale.ROOT);
    }

    /** Whether the number carries a software version instead of a check digit. */
    public static boolean isImeiSv(String number) {
        return INSTANCE.validate(number).length() == 16;
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (!Strings.isDigits(n)) {
            throw new InvalidFormatException();
        }
        if (n.length() == 15) {
            // only the 15-digit form carries a check digit
            Luhn.validate(n);
        } else if (n.length() != 14 && n.length() != 16) {
            throw new InvalidLengthException();
        }
        return n;
    }

    @Override
    public String format(String number) {
        String n = validate(number);
        return n.substring(0, 2) + "-" + n.substring(2, 8) + "-"
                + n.substring(8, 14) + (n.length() > 14 ? "-" + n.substring(14) : "");
    }
}
