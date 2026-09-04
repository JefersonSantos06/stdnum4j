package io.github.jefersonsantos06.stdnum.eu;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;


/**
 * IČ DPH (Identifikačné číslo pre daň z pridanej hodnoty), the Slovak VAT
 * number: ten digits that are a multiple of 11, not starting with zero and
 * with the third digit in {@code 2,3,4,7,8,9}. A birth number
 * ({@link SkRc}) is also accepted, as sources disagree on whether it may be
 * used as a VAT number.
 */
public final class SkDph implements StdNum {

    public static final SkDph INSTANCE = new SkDph();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("sk.dph", "IČ DPH")
                    .country("SK")
                    .title("Identifikačné číslo pre daň z pridanej hodnoty")
                    .description("Slovak VAT number: 10 digits divisible by 11, or a birth"
                            + " number.")
                    .tags(Tag.VAT)
                    .build();

    private SkDph() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " -", "SK");
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
        if (SkRc.INSTANCE.isValid(n)) {
            return n;
        }
        if (n.charAt(0) == '0' || "234789".indexOf(n.charAt(2)) < 0) {
            throw new InvalidFormatException();
        }
        if (Long.parseLong(n) % 11 != 0) {
            throw new InvalidChecksumException();
        }
        return n;
    }
}
