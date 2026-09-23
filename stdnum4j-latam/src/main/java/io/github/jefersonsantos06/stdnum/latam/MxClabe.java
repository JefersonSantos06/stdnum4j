package io.github.jefersonsantos06.stdnum.latam;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Mask;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.util.List;

/**
 * CLABE (Clave Bancaria Estandarizada), the Mexican interbank account number
 * every SPEI transfer is addressed to: eighteen digits — the bank, the
 * branch city (plaza), the account and a check digit. Each of the first
 * seventeen digits is weighted 3, 7, 1 in turn and reduced modulo 10, and the
 * check digit brings their sum to a multiple of ten.
 *
 * <p>The bank and plaza codes are not checked against a table: institutions
 * keep joining SPEI, and a table would reject their accounts the day it went
 * out of date.</p>
 */
public final class MxClabe implements StdNum {

    public static final MxClabe INSTANCE = new MxClabe();

    private static final int[] WEIGHTS = {3, 7, 1};

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("mx.clabe", "CLABE")
                    .country("MX")
                    .title("Clave Bancaria Estandarizada")
                    .description("Mexican interbank account number: 18 digits as the bank, the plaza,"
                            + " the account and a check digit weighted 3,7,1 modulo 10.")
                    .tags(Tag.BANK, Tag.PAYMENT)
                    .references(
                            "https://en.wikipedia.org/wiki/CLABE",
                            "https://github.com/koblas/stdnum-js/blob/main/src/mx/clabe.ts")
                    .build();

    private static final Mask MASK = Mask.of("999 999 99999999999 9");

    private MxClabe() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " -");
    }

    /** The check digit for the seventeen digits that come before it. */
    public static char calcCheckDigit(String base) {
        Strings.requireDigits(base, 17);
        int sum = 0;
        for (int i = 0; i < base.length(); i++) {
            sum += (base.charAt(i) - '0') * WEIGHTS[i % 3] % 10;
        }
        return (char) ('0' + (10 - sum % 10) % 10);
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (!Strings.isDigits(n)) {
            throw new InvalidFormatException();
        }
        if (n.length() != 18) {
            throw new InvalidLengthException();
        }
        if (n.charAt(17) != calcCheckDigit(n.substring(0, 17))) {
            throw new InvalidChecksumException();
        }
        return n;
    }

    @Override
    public String format(String number) {
        return MASK.fill(validate(number));
    }

    @Override
    public List<Mask> masks() {
        return List.of(MASK);
    }
}
