package io.github.jefersonsantos06.stdnum.apac;

import io.github.jefersonsantos06.stdnum.algo.Luhn;
import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

/**
 * NPWP (Nomor Pokok Wajib Pajak), the Indonesian tax number: fifteen
 * digits in the pre-2024 format, or sixteen since then — either the old
 * number with a leading zero, or a citizen's NIK. The Luhn checksum
 * covers the first nine digits of the number proper.
 */
public final class IdNpwp implements StdNum {

    public static final IdNpwp INSTANCE = new IdNpwp();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("id.npwp", "NPWP")
                    .country("ID")
                    .title("Nomor Pokok Wajib Pajak")
                    .description("Indonesian tax number: the 15-digit format or the 16-digit"
                            + " one used since 2024, with a Luhn checksum.")
                    .tags(Tag.TAX, Tag.VAT)
                    .build();

    private IdNpwp() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " -.");
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (!Strings.isDigits(n)) {
            throw new InvalidFormatException();
        }
        if (n.length() == 15) {
            Luhn.validate(n.substring(0, 9));
        } else if (n.length() == 16) {
            if (n.charAt(0) != '0') {
                // a 16-digit number not starting with zero is a NIK, which
                // carries no checksum of its own
                return n;
            }
            Luhn.validate(n.substring(0, 10));
        } else {
            throw new InvalidLengthException();
        }
        return n;
    }

    @Override
    public String format(String number) {
        String n = validate(number);
        String b = n.length() == 16 ? n.substring(1) : n;
        return b.substring(0, 2) + "." + b.substring(2, 5) + "." + b.substring(5, 8)
                + "." + b.charAt(8) + "-" + b.substring(9, 12) + "." + b.substring(12);
    }
}
