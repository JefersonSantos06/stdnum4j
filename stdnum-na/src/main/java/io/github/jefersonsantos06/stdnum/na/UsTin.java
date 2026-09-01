package io.github.jefersonsantos06.stdnum.na;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.spi.ValidationException;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.util.List;

/**
 * The US Taxpayer Identification Number, which is whichever of the five kinds
 * of number a taxpayer holds: a {@link UsSsn}, a {@link UsItin}, a
 * {@link UsEin}, a {@link UsPtin} or a {@link UsAtin}.
 *
 * <p>They are tried in that order, so the kind with the tightest rules wins.
 * The ATIN comes last because the IRS publishes no structure for it, which
 * makes it accept any nine digits.</p>
 *
 * <p>{@code format} hands the number to the first kind that accepts it. A
 * number that is no kind of TIN has no presentation and is refused, where
 * python-stdnum returns its argument back verbatim, uncleaned.</p>
 */
public final class UsTin implements StdNum {

    public static final UsTin INSTANCE = new UsTin();

    /** Tried in order; the first that accepts the number decides. */
    private static final List<StdNum> KINDS = List.of(
            UsSsn.INSTANCE, UsItin.INSTANCE, UsEin.INSTANCE,
            UsPtin.INSTANCE, UsAtin.INSTANCE);

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("us.tin", "TIN")
                    .country("US")
                    .title("Taxpayer Identification Number")
                    .description("US taxpayer number: whichever of the SSN, ITIN, EIN, PTIN"
                            + " and ATIN the taxpayer holds.")
                    .tags(Tag.TAX)
                    .references("https://www.irs.gov/individuals/international-taxpayers/"
                            + "taxpayer-identification-numbers-tin")
                    .build();

    private UsTin() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, "-");
    }

    /** Every kind of number this one could be; often more than one. */
    public static List<StdNum> kindsOf(String number) {
        return KINDS.stream().filter(kind -> kind.isValid(number)).toList();
    }

    @Override
    public String validate(String number) {
        for (StdNum kind : KINDS) {
            try {
                return kind.validate(number);
            } catch (ValidationException e) {
                // not this kind of taxpayer number; try the next
            }
        }
        throw new InvalidFormatException();
    }

    @Override
    public String format(String number) {
        List<StdNum> kinds = kindsOf(number);
        if (kinds.isEmpty()) {
            throw new InvalidFormatException("No kind of TIN has this shape.");
        }
        return kinds.get(0).format(number);
    }
}
