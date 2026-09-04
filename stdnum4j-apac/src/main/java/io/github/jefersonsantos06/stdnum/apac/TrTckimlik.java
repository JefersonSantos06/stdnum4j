package io.github.jefersonsantos06.stdnum.apac;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

/**
 * T.C. Kimlik No. (Türkiye Cumhuriyeti Kimlik Numarası), the Turkish
 * personal identification number: eleven digits not starting with zero,
 * where the last two are check digits over the first nine.
 */
public final class TrTckimlik implements StdNum {

    public static final TrTckimlik INSTANCE = new TrTckimlik();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("tr.tckimlik", "T.C. Kimlik No.")
                    .country("TR")
                    .title("Türkiye Cumhuriyeti Kimlik Numarası")
                    .description("Turkish personal identification number: 11 digits with two"
                            + " check digits.")
                    .tags(Tag.PERSON)
                    .build();

    private TrTckimlik() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " -");
    }

    /** The two check digits for the nine-digit base. */
    public static String calcCheckDigits(String base) {
        int weighted = 0;
        int total = 0;
        for (int i = 0; i < 9 && i < base.length(); i++) {
            int digit = base.charAt(i) - '0';
            weighted += (i % 2 == 0 ? 3 : 1) * digit;
            total += digit;
        }
        int check1 = Math.floorMod(10 - weighted, 10);
        int check2 = (check1 + total) % 10;
        return "" + check1 + check2;
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (!Strings.isDigits(n) || n.charAt(0) == '0') {
            throw new InvalidFormatException();
        }
        if (n.length() != 11) {
            throw new InvalidLengthException();
        }
        if (!n.endsWith(calcCheckDigits(n))) {
            throw new InvalidChecksumException();
        }
        return n;
    }
}
