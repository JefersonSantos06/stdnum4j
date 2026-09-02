package io.github.jefersonsantos06.stdnum.apac;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.Message;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.spi.ValidationException;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.util.List;

/**
 * The Thai tax identification number. Both kinds of taxpayer are covered by
 * one thirteen-digit number: a juristic person carries the {@link ThMoa}
 * issued by the Ministry of Commerce, an individual the {@link ThPin} of
 * their identity card.
 */
public final class ThTin implements StdNum {

    public static final ThTin INSTANCE = new ThTin();

    /** Tried in order; the first that accepts the number decides. */
    private static final List<StdNum> KINDS = List.of(ThMoa.INSTANCE, ThPin.INSTANCE);

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("th.tin", "TIN")
                    .country("TH")
                    .title("Thai Tax Identification Number")
                    .description("Thai tax number: the 13-digit registration number of a"
                            + " juristic person or the personal identification number.")
                    .tags(Tag.TAX)
                    .references("https://www.oecd.org/content/dam/oecd/en/topics/policy-issue-focus/aeoi/"
                            + "thailand-tin.pdf")
                    .build();

    private ThTin() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " -");
    }

    /** The type of taxpayer the number belongs to, or {@code null} if it is not valid. */
    public static StdNum kindOf(String number) {
        for (StdNum kind : KINDS) {
            if (kind.isValid(number)) {
                return kind;
            }
        }
        return null;
    }

    @Override
    public String validate(String number) {
        for (StdNum kind : KINDS) {
            try {
                return kind.validate(number);
            } catch (ValidationException e) {
                // not this kind of taxpayer; try the next
            }
        }
        throw new InvalidFormatException();
    }

    @Override
    public String format(String number) {
        StdNum kind = kindOf(number);
        if (kind == null) {
            throw new InvalidFormatException(Message.of(ThTin.class, "tin.kind",
                    "Neither a personal nor a company number."));
        }
        return kind.format(number);
    }
}
