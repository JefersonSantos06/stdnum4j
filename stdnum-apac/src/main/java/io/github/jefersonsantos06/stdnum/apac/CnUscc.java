package io.github.jefersonsantos06.stdnum.apac;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.util.Locale;

/**
 * USCC (Unified Social Credit Code, 统一社会信用代码), the Chinese business
 * and tax number: eighteen characters over an alphabet that omits the
 * ambiguous letters I, O, Z, S and V. The first digit is the registering
 * authority, the second the entity type, digits 3-8 the region, 9-17 the
 * organisation code and the last a check character modulo 31.
 */
public final class CnUscc implements StdNum {

    public static final CnUscc INSTANCE = new CnUscc();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("cn.uscc", "USCC")
                    .country("CN")
                    .title("Unified Social Credit Code")
                    .description("Chinese business and tax number: 18 characters with a"
                            + " mod 31 check character.")
                    .tags(Tag.COMPANY, Tag.TAX, Tag.VAT)
                    .build();

    /** The USCC alphabet: digits and letters, without I, O, Z, S and V. */
    private static final String ALPHABET = "0123456789ABCDEFGHJKLMNPQRTUWXY";
    private static final int[] WEIGHTS =
            {1, 3, 9, 27, 19, 26, 16, 17, 20, 29, 25, 13, 8, 24, 10, 30, 28};

    private CnUscc() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " -").toUpperCase(Locale.ROOT);
    }

    /** The check character for a number whose first seventeen characters are known. */
    public static char calcCheckDigit(String base) {
        String b = INSTANCE.compact(base);
        int total = 0;
        for (int i = 0; i < WEIGHTS.length && i < b.length(); i++) {
            int value = ALPHABET.indexOf(b.charAt(i));
            if (value < 0) {
                throw new InvalidFormatException();
            }
            total += value * WEIGHTS[i];
        }
        return ALPHABET.charAt(Math.floorMod(31 - total, 31));
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (n.length() != 18) {
            throw new InvalidLengthException();
        }
        if (!Strings.isDigits(n.substring(0, 8))) {
            throw new InvalidFormatException();
        }
        for (int i = 8; i < n.length(); i++) {
            if (ALPHABET.indexOf(n.charAt(i)) < 0) {
                throw new InvalidFormatException();
            }
        }
        if (n.charAt(17) != calcCheckDigit(n)) {
            throw new InvalidChecksumException();
        }
        return n;
    }
}
