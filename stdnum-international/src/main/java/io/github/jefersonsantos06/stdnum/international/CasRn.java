package io.github.jefersonsantos06.stdnum.international;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.util.regex.Pattern;

/**
 * CAS Registry Number, the identifier the Chemical Abstracts Service
 * assigns to a substance: two to seven digits, two digits and a check
 * digit, written with hyphens ({@code 87-86-5}). Unhyphenated input is
 * hyphenated by {@link #compact(String)}.
 */
public final class CasRn implements StdNum {

    public static final CasRn INSTANCE = new CasRn();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("casrn", "CAS RN")
                    .title("CAS Registry Number")
                    .description("Chemical substance identifier: up to 10 digits in three"
                            + " hyphenated groups with a mod 10 check digit.")
                    .tags(Tag.OTHER)
                    .references("https://en.wikipedia.org/wiki/CAS_Registry_Number")
                    .build();

    private static final Pattern STRUCTURE = Pattern.compile("[1-9][0-9]{1,6}-[0-9]{2}-[0-9]");

    private CasRn() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        String n = Strings.compact(number, " ");
        if (n.indexOf('-') < 0 && n.length() > 3) {
            n = n.substring(0, n.length() - 3) + "-"
                    + n.substring(n.length() - 3, n.length() - 1) + "-"
                    + n.substring(n.length() - 1);
        }
        return n;
    }

    /** The check digit for the base without its final digit. */
    public static char calcCheckDigit(String base) {
        String digits = base.replace("-", "");
        int sum = 0;
        for (int i = 0; i < digits.length(); i++) {
            sum += (i + 1) * (digits.charAt(digits.length() - 1 - i) - '0');
        }
        return (char) ('0' + sum % 10);
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (n.length() < 7 || n.length() > 12) {
            throw new InvalidLengthException();
        }
        if (!STRUCTURE.matcher(n).matches()) {
            throw new InvalidFormatException();
        }
        if (n.charAt(n.length() - 1) != calcCheckDigit(n.substring(0, n.length() - 1))) {
            throw new InvalidChecksumException();
        }
        return n;
    }
}
