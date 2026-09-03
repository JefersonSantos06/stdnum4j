package io.github.jefersonsantos06.stdnum.eu;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Mask;
import io.github.jefersonsantos06.stdnum.text.Strings;


/**
 * NIP (Numer Identyfikacji Podatkowej), the Polish VAT number: ten digits
 * weighted 6,5,7,2,3,4,5,6,7 against the final check digit, modulo 11.
 */
public final class PlNip implements StdNum {

    public static final PlNip INSTANCE = new PlNip();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("pl.nip", "NIP")
                    .country("PL")
                    .title("Numer Identyfikacji Podatkowej")
                    .description("Polish VAT number: 10 digits with a weighted mod 11 check.")
                    .tags(Tag.VAT)
                    .build();

    private static final Mask MASK = Mask.of("###-###-##-##");

    private static final int[] WEIGHTS = {6, 5, 7, 2, 3, 4, 5, 6, 7, -1};

    private PlNip() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " -", "PL");
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
        int sum = 0;
        for (int i = 0; i < WEIGHTS.length; i++) {
            sum += WEIGHTS[i] * (n.charAt(i) - '0');
        }
        if (Math.floorMod(sum, 11) != 0) {
            throw new InvalidChecksumException();
        }
        return n;
    }

    @Override
    public String format(String number) {
        return MASK.fill(validate(number));
    }
}
