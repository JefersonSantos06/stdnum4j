package io.github.jefersonsantos06.stdnum.apac;

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
 * ABN (Australian Business Number): eleven digits whose first two are check
 * digits over the remaining nine, weighted with odd numbers modulo 89. It
 * is the Australian VAT (GST) identifier.
 */
public final class AuAbn implements StdNum {

    public static final AuAbn INSTANCE = new AuAbn();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("au.abn", "ABN")
                    .country("AU")
                    .title("Australian Business Number")
                    .description("Australian business and GST identifier: 11 digits opening"
                            + " with two check digits.")
                    .tags(Tag.COMPANY, Tag.VAT, Tag.TAX)
                    .build();

    private static final Mask MASK = Mask.of("## ### ### ###");

    private static final int[] WEIGHTS = {3, 5, 7, 9, 11, 13, 15, 17, 19};

    private AuAbn() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " ");
    }

    /** The two check digits to prepend to the nine-digit base. */
    public static String calcCheckDigits(String base) {
        int sum = 0;
        for (int i = 0; i < WEIGHTS.length && i < base.length(); i++) {
            sum -= WEIGHTS[i] * (base.charAt(i) - '0');
        }
        return Integer.toString(11 + Math.floorMod(sum - 1, 89));
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (!Strings.isDigits(n)) {
            throw new InvalidFormatException();
        }
        if (n.length() != 11) {
            throw new InvalidLengthException();
        }
        if (!n.startsWith(calcCheckDigits(n.substring(2)))) {
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
