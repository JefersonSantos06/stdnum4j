package io.github.jefersonsantos06.stdnum.eu;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidComponentException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.util.Locale;

/**
 * UNP, the Belarusian taxpayer number: nine characters whose first two name
 * the region and whose last is a weighted mod 11 check digit.
 *
 * <p>The region is written either as two digits or as two letters. Those
 * letters are Cyrillic, but ten of them are shaped like Latin ones and are
 * routinely typed that way, so both spellings are accepted and folded to
 * Latin.</p>
 */
public final class ByUnp implements StdNum {

    public static final ByUnp INSTANCE = new ByUnp();

    /** The ten Cyrillic letters shaped like the Latin ones below them. */
    private static final String CYRILLIC =
            "АВЕКМНОРСТ";
    private static final String LATIN = "ABEKMHOPCT";
    /** The letters a region code is drawn from, in the order the checksum values them. */
    private static final String REGION_LETTERS = "ABCEHKMOPT";
    private static final String ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int[] WEIGHTS = {29, 23, 19, 17, 13, 7, 5, 3};

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("by.unp", "UNP")
                    .country("BY")
                    .title("Belarusian taxpayer number")
                    .description("Belarusian taxpayer number: 9 characters opening with a"
                            + " region code and closing with a weighted mod 11 check digit.")
                    .tags(Tag.COMPANY, Tag.TAX)
                    .references("https://www.portal.nalog.gov.by/grp/")
                    .build();

    private ByUnp() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        String n = Strings.compact(number, " ").toUpperCase(Locale.ROOT);
        for (String prefix : new String[] {"УНП", "UNP"}) {
            if (n.startsWith(prefix)) {
                n = n.substring(prefix.length());
                break;
            }
        }
        StringBuilder sb = new StringBuilder(n.length());
        for (int i = 0; i < n.length(); i++) {
            int cyrillic = CYRILLIC.indexOf(n.charAt(i));
            sb.append(cyrillic < 0 ? n.charAt(i) : LATIN.charAt(cyrillic));
        }
        return sb.toString();
    }

    /**
     * The check digit of a number, from its first eight characters.
     *
     * @throws InvalidChecksumException when the weighted sum leaves a
     *                                  remainder above 9, for which no check
     *                                  digit exists
     */
    public static char calcCheckDigit(String number) {
        String n = INSTANCE.compact(number);
        if (!Strings.isDigits(n)) {
            // a lettered region contributes its position in the region alphabet
            if (n.length() < 2 || REGION_LETTERS.indexOf(n.charAt(1)) < 0) {
                throw new InvalidFormatException();
            }
            n = n.charAt(0) + Integer.toString(REGION_LETTERS.indexOf(n.charAt(1)))
                    + n.substring(2);
        }
        int sum = 0;
        for (int i = 0; i < WEIGHTS.length && i < n.length(); i++) {
            int value = ALPHABET.indexOf(n.charAt(i));
            if (value < 0) {
                throw new InvalidFormatException();
            }
            sum += WEIGHTS[i] * value;
        }
        int check = sum % 11;
        if (check > 9) {
            throw new InvalidChecksumException("No valid check digit exists for this number.");
        }
        return (char) ('0' + check);
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (n.length() != 9) {
            throw new InvalidLengthException();
        }
        if (!Strings.isDigits(n.substring(2))) {
            throw new InvalidFormatException();
        }
        if (!Strings.isDigits(n.substring(0, 2))
                && (REGION_LETTERS.indexOf(n.charAt(0)) < 0
                    || REGION_LETTERS.indexOf(n.charAt(1)) < 0)) {
            throw new InvalidFormatException();
        }
        if ("1234567ABCEHKM".indexOf(n.charAt(0)) < 0) {
            throw new InvalidComponentException("Not the code of a region.");
        }
        if (n.charAt(8) != calcCheckDigit(n)) {
            throw new InvalidChecksumException();
        }
        return n;
    }
}
