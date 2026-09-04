package io.github.jefersonsantos06.stdnum.na;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * PTIN, the US Preparer Tax Identification Number: the letter P and eight
 * digits, which a paid tax preparer quotes on every return they sign in
 * place of their own Social Security number.
 */
public final class UsPtin implements StdNum {

    public static final UsPtin INSTANCE = new UsPtin();

    private static final Pattern PATTERN = Pattern.compile("P[0-9]{8}");

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("us.ptin", "PTIN")
                    .country("US")
                    .title("Preparer Tax Identification Number")
                    .description("US paid tax preparer number: the letter P and 8 digits.")
                    .tags(Tag.PERSON, Tag.TAX)
                    .references("https://www.irs.gov/tax-professionals/ptin-requirements-for-tax-return-preparers")
                    .build();

    private UsPtin() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, "-").toUpperCase(Locale.ROOT);
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (!PATTERN.matcher(n).matches()) {
            throw new InvalidFormatException();
        }
        return n;
    }
}
