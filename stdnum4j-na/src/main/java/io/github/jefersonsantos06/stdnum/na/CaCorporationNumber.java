package io.github.jefersonsantos06.stdnum.na;

import io.github.jefersonsantos06.stdnum.algo.Weighted;
import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidComponentException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.Reasons;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Mask;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.util.List;

/**
 * The corporation number Corporations Canada gives a corporation made under
 * federal law (the Canada Business Corporations Act, the Canada Not-for-profit
 * Corporations Act and the like): seven digits for the older corporations,
 * eight for the newer ones, the last a check digit. Corporations
 * Canada writes it with a hyphen before that digit — {@code 430805-1},
 * {@code 1646901-9} — and a numbered company is named after it:
 * {@code 4308051 CANADA INC.}
 *
 * <p>It is not the {@link CaBn business number} the CRA gives the same
 * corporation, nor the number a provincial registry gives a corporation made
 * under provincial law.</p>
 *
 * <p>Corporations Canada does not publish the check digit. The rule here was
 * read off 323 numbers, 15 of them from Corporations Canada's own profiles,
 * old and new, and is the only weighted mod 11 rule that fits all of them:
 * the six digits before the check digit weighted 7, 6, 5, 4, 3, 2, the sum
 * modulo 11 subtracted from 11 and taken modulo 10 — the same arithmetic as
 * {@link CaQst}. The first digit of an eight-digit number carries no weight;
 * so far it is always 1.</p>
 *
 * <p>A number is taken with its leading zero, as Corporations Canada writes it
 * ({@code 019799-8}); a database that dropped the zero holds six digits, and
 * six digits are refused.</p>
 */
public final class CaCorporationNumber implements StdNum {

    public static final CaCorporationNumber INSTANCE = new CaCorporationNumber();

    private static final int[] WEIGHTS = Weighted.descending(7, 6);

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("ca.corporation_number", "Corporation number")
                    .country("CA")
                    .title("Federal corporation number")
                    .description("Number Corporations Canada gives a federal corporation: 7 or 8"
                            + " digits, the last a weighted mod 11 check digit.")
                    .tags(Tag.COMPANY)
                    .references("https://lookuptax.com/validate/canada/corporation-number")
                    .build();

    private static final List<Mask> MASKS = List.of(Mask.of("999999-9"), Mask.of("9999999-9"));

    private CaCorporationNumber() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " -");
    }

    /** The check digit for the six or seven digits that come before it. */
    public static char calcCheckDigit(String base) {
        String b = INSTANCE.compact(base);
        if (!Strings.isDigits(b)) {
            throw new InvalidFormatException();
        }
        if (b.length() != 6 && b.length() != 7) {
            throw new InvalidLengthException();
        }
        int sum = Weighted.weightedSum(b.substring(b.length() - 6), WEIGHTS);
        return (char) ('0' + (11 - sum % 11) % 10);
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (!Strings.isDigits(n)) {
            throw new InvalidFormatException();
        }
        if (n.length() != 7 && n.length() != 8) {
            throw new InvalidLengthException();
        }
        String base = n.substring(0, n.length() - 1);
        if (Strings.allSame(base) && base.charAt(0) == '0') {
            throw new InvalidComponentException(Reasons.zeroSequence());
        }
        if (n.charAt(n.length() - 1) != calcCheckDigit(base)) {
            throw new InvalidChecksumException();
        }
        return n;
    }

    @Override
    public String format(String number) {
        return Mask.apply(MASKS, validate(number));
    }

    @Override
    public List<Mask> masks() {
        return MASKS;
    }
}
