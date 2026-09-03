package io.github.jefersonsantos06.stdnum.eu;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;


/**
 * VAT number (tax reference number) of Ireland: eight or nine characters in
 * either the new system (seven digits and one or two letters) or the old
 * one (a letter or symbol in the second position). The check character is
 * derived modulo 23 over the alphabet {@code WABC...V}.
 */
public final class IeVat implements StdNum {

    public static final IeVat INSTANCE = new IeVat();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("ie.vat", "VAT")
                    .country("IE")
                    .title("Irish tax reference number")
                    .description("Irish VAT number: 8 or 9 characters with a mod 23 check"
                            + " letter, in the old or new system.")
                    .tags(Tag.VAT)
                    .build();

    private static final String ALPHABET = "WABCDEFGHIJKLMNOPQRSTUV";
    private static final String OLD_SECOND = "ABCDEFGHIJKLMNOPQRSTUVWXYZ+*";

    private IeVat() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " -", "IE");
    }

    /**
     * The check character for a base of seven digits optionally followed by
     * the trailing letter of the new system. Shorter bases are zero-padded.
     */
    public static char calcCheckDigit(String base) {
        String b = base.length() < 7 ? "0".repeat(7 - base.length()) + base : base;
        int sum = 0;
        for (int i = 0; i < 7; i++) {
            sum += (8 - i) * (b.charAt(i) - '0');
        }
        if (b.length() > 7) {
            sum += 9 * ALPHABET.indexOf(b.charAt(7));
        }
        return ALPHABET.charAt(sum % 23);
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (n.length() != 8 && n.length() != 9) {
            throw new InvalidLengthException();
        }
        if (!Strings.isDigits(n.substring(0, 1)) || !Strings.isDigits(n.substring(2, 7))) {
            throw new InvalidFormatException();
        }
        for (int i = 7; i < n.length(); i++) {
            if (ALPHABET.indexOf(n.charAt(i)) < 0) {
                throw new InvalidFormatException();
            }
        }
        char expected;
        if (Strings.isDigits(n.substring(0, 7))) {
            // new system: seven digits, then the check letter, then an optional letter
            expected = calcCheckDigit(n.substring(0, 7) + n.substring(8));
        } else if (OLD_SECOND.indexOf(n.charAt(1)) >= 0) {
            // old system: the second character is a letter or symbol
            expected = calcCheckDigit(n.substring(2, 7) + n.charAt(0));
        } else {
            throw new InvalidFormatException();
        }
        if (n.charAt(7) != expected) {
            throw new InvalidChecksumException();
        }
        return n;
    }

    /**
     * Rewrites an old-style number, whose second character is a letter or
     * symbol, into the new form where only the trailing character is a
     * letter. Numbers already in the new form are returned unchanged.
     */
    public static String convert(String number) {
        String n = INSTANCE.compact(number);
        if (n.length() == 8 && !Strings.isDigits(n.substring(1, 2))) {
            return "0" + n.substring(2, 7) + n.charAt(0) + n.substring(7);
        }
        return n;
    }

}
