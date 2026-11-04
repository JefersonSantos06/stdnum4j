package io.github.jefersonsantos06.stdnum.na;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidComponentException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.Message;
import io.github.jefersonsantos06.stdnum.spi.Reasons;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Mask;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.util.List;
import java.util.Locale;

/**
 * PST, the provincial sales tax registration number of British Columbia:
 * the letters {@code PST} and eight digits, {@code PST-1234-5678}. BC
 * businesses print it on their invoices beside their GST number, which is a
 * {@link CaBn}. Eight digits given without the prefix are accepted and
 * returned with it.
 *
 * <p>The province publishes only the shape of the number, and no check digit
 * is known, so only the shape is checked. The sales taxes of the other
 * provinces — Saskatchewan's PST, Manitoba's RST, Québec's {@link CaQst QST}
 * — are numbers of their own and are not taken here.</p>
 */
public final class CaPst implements StdNum {

    public static final CaPst INSTANCE = new CaPst();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("ca.pst", "PST")
                    .country("CA")
                    .title("British Columbia Provincial Sales Tax number")
                    .description("British Columbia provincial sales tax registration number: PST"
                            + " followed by 8 digits, with no check digit.")
                    .tags(Tag.COMPANY, Tag.TAX)
                    .references(
                            "https://www2.gov.bc.ca/gov/content/taxes/sales-taxes/pst/register",
                            "https://github.com/koblas/stdnum-js/blob/main/src/ca/pst.ts")
                    .build();

    private static final Mask MASK = Mask.of("AAA-9999-9999");

    private CaPst() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        String n = Strings.compact(number, " -").toUpperCase(Locale.ROOT);
        return n.length() == 8 && Strings.isDigits(n) ? "PST" + n : n;
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (n.length() != 11) {
            throw new InvalidLengthException();
        }
        if (!n.startsWith("PST")) {
            throw new InvalidComponentException(Message.of(CaPst.class, "pst.prefix",
                    "A British Columbia PST number starts with PST."));
        }
        String digits = n.substring(3);
        if (!Strings.isDigits(digits)) {
            throw new InvalidFormatException();
        }
        if (Strings.allSame(digits) && digits.charAt(0) == '0') {
            throw new InvalidComponentException(Reasons.zeroSequence());
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
