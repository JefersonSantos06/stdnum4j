package io.github.jefersonsantos06.stdnum.eu;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * PPS number, the Irish personal public service number: seven digits, a check
 * letter and sometimes a second letter.
 *
 * <p>The trailing A, B or H is part of what the check letter is computed over
 * — it marks a number shared with a spouse — whereas W, T and X are not, being
 * historical markers the checksum ignores.</p>
 */
public final class IePps implements StdNum {

    public static final IePps INSTANCE = new IePps();

    private static final Pattern PATTERN = Pattern.compile("[0-9]{7}[A-W][ABHWTX]?");

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("ie.pps", "PPS No")
                    .country("IE")
                    .title("Personal Public Service Number")
                    .description("Irish personal public service number: 7 digits, a weighted"
                            + " mod 23 check letter and an optional second letter.")
                    .tags(Tag.PERSON, Tag.TAX)
                    .references("https://en.wikipedia.org/wiki/Personal_Public_Service_Number")
                    .build();

    private IePps() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " -").toUpperCase(Locale.ROOT);
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (!PATTERN.matcher(n).matches()) {
            throw new InvalidFormatException();
        }
        // A, B and H take part in the checksum; W, T and X do not
        String base = n.length() == 9 && "ABH".indexOf(n.charAt(8)) >= 0
                ? n.substring(0, 7) + n.substring(8)
                : n.substring(0, 7);
        if (n.charAt(7) != IeVat.calcCheckDigit(base)) {
            throw new InvalidChecksumException();
        }
        return n;
    }
}
