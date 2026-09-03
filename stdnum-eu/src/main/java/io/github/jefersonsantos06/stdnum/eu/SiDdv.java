package io.github.jefersonsantos06.stdnum.eu;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;


/**
 * ID za DDV (Davčna številka), the Slovenian VAT number: eight digits not
 * starting with zero, weighted 8..2 modulo 11 (a remainder of 10 gives a
 * check digit of 0).
 */
public final class SiDdv implements StdNum {

    public static final SiDdv INSTANCE = new SiDdv();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("si.ddv", "ID za DDV")
                    .country("SI")
                    .title("Davčna številka")
                    .description("Slovenian VAT number: 8 digits with a weighted mod 11 check.")
                    .tags(Tag.VAT)
                    .build();

    private SiDdv() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " -", "SI");
    }

    /** The check digit for the seven-digit base. */
    public static char calcCheckDigit(String base) {
        int sum = 0;
        for (int i = 0; i < base.length(); i++) {
            sum += (8 - i) * (base.charAt(i) - '0');
        }
        int check = 11 - sum % 11;
        return (char) ('0' + (check == 10 ? 0 : check));
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (!Strings.isDigits(n) || n.charAt(0) == '0') {
            throw new InvalidFormatException();
        }
        if (n.length() != 8) {
            throw new InvalidLengthException();
        }
        if (n.charAt(7) != calcCheckDigit(n.substring(0, 7))) {
            throw new InvalidChecksumException();
        }
        return n;
    }
}
