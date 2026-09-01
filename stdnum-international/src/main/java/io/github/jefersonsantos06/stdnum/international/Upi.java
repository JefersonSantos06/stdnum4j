package io.github.jefersonsantos06.stdnum.international;

import io.github.jefersonsantos06.stdnum.algo.Iso7064;
import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidComponentException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.util.Locale;

/**
 * UPI, the identifier of an over-the-counter derivative product: twelve
 * characters opening with QZ and closing with a check character.
 *
 * <p>Its alphabet leaves out the vowels, so that no identifier can spell a
 * word, along with the letters that look like digits. That makes the
 * alphabet thirty characters long, so the ISO 7064 hybrid system it uses is
 * a MOD 31,30 rather than the usual MOD 37,36.</p>
 */
public final class Upi implements StdNum {

    public static final Upi INSTANCE = new Upi();

    /** The digits, less the vowels and the letters that read as digits. */
    private static final String ALPHABET = "0123456789BCDFGHJKLMNPQRSTVWXZ";
    private static final Iso7064.HybridSystem MOD_31_30 = new Iso7064.HybridSystem(ALPHABET);

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("upi", "UPI")
                    .title("Unique Product Identifier")
                    .description("Identifier of an OTC derivative product: 12 characters"
                            + " starting with QZ, on an ISO 7064 MOD 31,30 check character.")
                    .tags(Tag.FINANCIAL, Tag.PRODUCT)
                    .references("https://www.anna-dsb.com/upi-overview/")
                    .build();

    private Upi() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " ").toUpperCase(Locale.ROOT);
    }

    /** The check character that makes {@code number} valid. */
    public static char calcCheckDigit(String number) {
        return MOD_31_30.calcCheckDigit(INSTANCE.compact(number));
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        for (int i = 0; i < n.length(); i++) {
            if (ALPHABET.indexOf(n.charAt(i)) < 0) {
                throw new InvalidFormatException();
            }
        }
        if (n.length() != 12) {
            throw new InvalidLengthException();
        }
        if (!n.startsWith("QZ")) {
            throw new InvalidComponentException("A product identifier starts with QZ.");
        }
        return MOD_31_30.validate(n);
    }
}
