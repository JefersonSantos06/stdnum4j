package io.github.jefersonsantos06.stdnum.na;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Mask;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.util.List;
import java.util.regex.Pattern;

/**
 * ATIN, the US Adoption Taxpayer Identification Number: a nine-digit number
 * the IRS issues for a child being adopted, so the adopting parents can claim
 * the child before a Social Security number comes through.
 *
 * <p>The IRS publishes no structure for it beyond its length, so only the
 * shape and the placement of the separators can be checked.</p>
 */
public final class UsAtin implements StdNum {

    public static final UsAtin INSTANCE = new UsAtin();

    private static final Pattern PATTERN = Pattern.compile("[0-9]{3}-?[0-9]{2}-?[0-9]{4}");

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("us.atin", "ATIN")
                    .country("US")
                    .title("Adoption Taxpayer Identification Number")
                    .description("US taxpayer number for a child being adopted: 9 digits,"
                            + " usually written in three groups.")
                    .tags(Tag.PERSON, Tag.TAX)
                    .references("https://www.irs.gov/individuals/adoption-taxpayer-identification-number")
                    .build();

    private static final Mask MASK = Mask.of("999-99-9999");

    private UsAtin() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, "-");
    }

    @Override
    public String validate(String number) {
        if (!PATTERN.matcher(Strings.compact(number, "")).matches()) {
            throw new InvalidFormatException();
        }
        return compact(number);
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
