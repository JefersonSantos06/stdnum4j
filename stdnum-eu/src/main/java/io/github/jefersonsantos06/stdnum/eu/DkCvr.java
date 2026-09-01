package io.github.jefersonsantos06.stdnum.eu;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.util.Locale;

/**
 * CVR (Momsregistreringsnummer), the Danish VAT number: eight digits not
 * starting with zero, weighted 2,7,6,5,4,3,2,1 modulo 11.
 */
public final class DkCvr implements StdNum {

    public static final DkCvr INSTANCE = new DkCvr();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("dk.cvr", "CVR")
                    .country("DK")
                    .title("Momsregistreringsnummer (CVR)")
                    .description("Danish VAT number: 8 digits with a weighted mod 11 check.")
                    .tags(Tag.VAT, Tag.COMPANY)
                    .build();

    private static final int[] WEIGHTS = {2, 7, 6, 5, 4, 3, 2, 1};

    private DkCvr() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        String n = Strings.compact(number, " -.,/:").toUpperCase(Locale.ROOT);
        return n.startsWith("DK") ? n.substring(2) : n;
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
        int sum = 0;
        for (int i = 0; i < WEIGHTS.length; i++) {
            sum += WEIGHTS[i] * (n.charAt(i) - '0');
        }
        if (sum % 11 != 0) {
            throw new InvalidChecksumException();
        }
        return n;
    }
}
