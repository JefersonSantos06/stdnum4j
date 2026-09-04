package io.github.jefersonsantos06.stdnum.eu;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;


/**
 * VAT registration number of Malta: eight digits not starting with zero,
 * weighted 3,4,6,7,8,9,10,1 modulo 37.
 */
public final class MtVat implements StdNum {

    public static final MtVat INSTANCE = new MtVat();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("mt.vat", "VAT")
                    .country("MT")
                    .title("Maltese VAT number")
                    .description("Maltese VAT number: 8 digits with a weighted mod 37 check.")
                    .tags(Tag.VAT)
                    .build();

    private static final int[] WEIGHTS = {3, 4, 6, 7, 8, 9, 10, 1};

    private MtVat() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " -", "MT");
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
        if (sum % 37 != 0) {
            throw new InvalidChecksumException();
        }
        return n;
    }
}
