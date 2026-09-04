package io.github.jefersonsantos06.stdnum.apac;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Mask;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.util.List;

/**
 * 法人番号 (hōjin bangō), the Japanese Corporate Number assigned by the
 * National Tax Agency: thirteen digits whose <em>first</em> digit is the
 * check digit over the remaining twelve.
 */
public final class JpCn implements StdNum {

    public static final JpCn INSTANCE = new JpCn();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("jp.cn", "法人番号")
                    .country("JP")
                    .title("Corporate Number (hōjin bangō)")
                    .description("Japanese corporate number: 13 digits opening with a mod 9"
                            + " check digit.")
                    .tags(Tag.COMPANY, Tag.TAX)
                    .build();

    private static final Mask MASK = Mask.of("#-####-####-####");

    private JpCn() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, "- ");
    }

    /** The leading check digit for the twelve-digit body. */
    public static char calcCheckDigit(String body) {
        int sum = 0;
        for (int i = 0; i < body.length(); i++) {
            // weights alternate 1 and 2 reading right to left
            sum += (i % 2 == 0 ? 1 : 2) * (body.charAt(body.length() - 1 - i) - '0');
        }
        return (char) ('0' + 9 - sum % 9);
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (n.length() != 13) {
            throw new InvalidLengthException();
        }
        if (!Strings.isDigits(n)) {
            throw new InvalidFormatException();
        }
        if (n.charAt(0) != calcCheckDigit(n.substring(1))) {
            throw new InvalidChecksumException();
        }
        return n;
    }

    @Override
    public String format(String number) {
        return MASK.fill(validate(number));
    }

    @Override
    public List<Mask> masks() {
        return List.of(MASK);
    }
}
