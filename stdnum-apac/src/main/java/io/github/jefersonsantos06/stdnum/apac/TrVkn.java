package io.github.jefersonsantos06.stdnum.apac;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

/**
 * VKN, the Turkish tax identification number: ten digits ending in a check
 * digit. The first nine used to be derived from the company name; today they
 * are simply issued in sequence.
 *
 * <p>The checksum is unusual: each digit is offset by its position, then
 * scaled by a power of two and folded modulo 9, with a zero result standing
 * for nine.</p>
 */
public final class TrVkn implements StdNum {

    public static final TrVkn INSTANCE = new TrVkn();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("tr.vkn", "VKN")
                    .country("TR")
                    .title("Vergi Kimlik Numarasi")
                    .description("Turkish tax identification number: 10 digits ending in a"
                            + " position-weighted mod 9 check digit.")
                    .tags(Tag.COMPANY, Tag.TAX)
                    .references("https://www.turkiye.gov.tr/gib-intvrg-vergi-kimlik-numarasi-dogrulama")
                    .build();

    private TrVkn() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, "");
    }

    /** The check digit of a number, from its first nine digits. */
    public static char calcCheckDigit(String number) {
        String n = INSTANCE.compact(number);
        int last = Math.min(9, n.length());
        int sum = 0;
        for (int i = 1; i <= last; i++) {
            int digit = n.charAt(last - i) - '0';
            int offset = (digit + i) % 10;
            if (offset != 0) {
                int scaled = (offset << i) % 9;
                sum += scaled == 0 ? 9 : scaled;
            }
        }
        return (char) ('0' + (10 - sum % 10) % 10);
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
        if (n.charAt(9) != calcCheckDigit(n)) {
            throw new InvalidChecksumException();
        }
        return n;
    }
}
