package io.github.jefersonsantos06.stdnum.apac;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Mask;
import io.github.jefersonsantos06.stdnum.text.Strings;

/**
 * ACN (Australian Company Number), issued by ASIC: nine digits with a
 * descending-weight modulo 10 check digit.
 */
public final class AuAcn implements StdNum {

    public static final AuAcn INSTANCE = new AuAcn();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("au.acn", "ACN")
                    .country("AU")
                    .title("Australian Company Number")
                    .description("Australian company identifier: 9 digits with a weighted"
                            + " mod 10 check digit.")
                    .tags(Tag.COMPANY)
                    .build();

    private static final Mask MASK = Mask.of("### ### ###");

    private AuAcn() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " ");
    }

    /** The check digit derived from the first eight digits. */
    public static char calcCheckDigit(String base) {
        int sum = 0;
        for (int i = 0; i < 8 && i < base.length(); i++) {
            sum += (base.charAt(i) - '0') * (8 - i);
        }
        return (char) ('0' + Math.floorMod(-sum, 10));
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (!Strings.isDigits(n)) {
            throw new InvalidFormatException();
        }
        if (n.length() != 9) {
            throw new InvalidLengthException();
        }
        if (n.charAt(8) != calcCheckDigit(n)) {
            throw new InvalidChecksumException();
        }
        return n;
    }

    @Override
    public String format(String number) {
        return MASK.fill(validate(number));
    }

    /** The ABN derived from this company number, by prepending its check digits. */
    public static String toAbn(String number) {
        String n = INSTANCE.validate(number);
        return AuAbn.calcCheckDigits(n) + n;
    }

}
