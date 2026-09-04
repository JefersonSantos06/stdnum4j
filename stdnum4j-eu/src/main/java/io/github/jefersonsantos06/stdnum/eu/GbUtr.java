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
 * UTR, the United Kingdom unique taxpayer reference: ten digits whose
 * <em>first</em> digit is the check digit. HMRC correspondence often appends
 * a K to the number; it is not part of it and is stripped.
 */
public final class GbUtr implements StdNum {

    public static final GbUtr INSTANCE = new GbUtr();

    private static final int[] WEIGHTS = {6, 7, 8, 9, 10, 5, 4, 3, 2};
    private static final String CHECK_DIGITS = "21987654321";

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("gb.utr", "UTR")
                    .country("GB")
                    .title("Unique Taxpayer Reference")
                    .description("United Kingdom taxpayer reference: 10 digits led by a"
                            + " weighted mod 11 check digit.")
                    .tags(Tag.TAX)
                    .references("https://www.gov.uk/find-utr-number")
                    .build();

    private GbUtr() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        String n = Strings.compact(number, " ").toUpperCase(Locale.ROOT);
        int start = 0;
        while (start < n.length() && n.charAt(start) == 'K') {
            start++;
        }
        return n.substring(start);
    }

    /** The leading check digit, from the nine digits that follow it. */
    public static char calcCheckDigit(String base) {
        int sum = 0;
        for (int i = 0; i < WEIGHTS.length && i < base.length(); i++) {
            sum += WEIGHTS[i] * (base.charAt(i) - '0');
        }
        return CHECK_DIGITS.charAt(sum % 11);
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
        if (n.charAt(0) != calcCheckDigit(n.substring(1))) {
            throw new InvalidChecksumException();
        }
        return n;
    }
}
