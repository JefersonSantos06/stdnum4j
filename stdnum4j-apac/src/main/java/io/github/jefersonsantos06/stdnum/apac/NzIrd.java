package io.github.jefersonsantos06.stdnum.apac;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidComponentException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.Message;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Mask;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.util.List;
import java.util.Locale;

/**
 * IRD number, used by the New Zealand Inland Revenue Department (Te Tari
 * Tāke): eight or nine digits in the issued range, closed by a check digit
 * that falls back to a second weight set when the first yields 10.
 *
 * <p>An eight-digit IRD number is the nine-digit one with its leading zero
 * dropped, not a different number, so {@code compact} restores the zero and
 * one number has one written form, {@code 049-098-576}. python-stdnum returns
 * each spelling unchanged and prints them differently ({@code 49-098-576} and
 * {@code 049-098-576}) — it pads to eight internally before weighting, so the
 * two validate alike there as well.</p>
 */
public final class NzIrd implements StdNum {

    public static final NzIrd INSTANCE = new NzIrd();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("nz.ird", "IRD")
                    .country("NZ")
                    .title("Inland Revenue Department number")
                    .description("New Zealand tax number: 8 or 9 digits with a weighted"
                            + " mod 11 check digit and a secondary weight set.")
                    .tags(Tag.TAX)
                    .build();

    private static final Mask MASK = Mask.of("###-###-###");

    private static final int[] PRIMARY = {3, 2, 7, 6, 5, 4, 3, 2};
    private static final int[] SECONDARY = {7, 4, 3, 2, 5, 2, 7, 6};

    private NzIrd() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        String n = Strings.compact(number, " -").toUpperCase(Locale.ROOT);
        if (n.startsWith("NZ")) {
            n = n.substring(2);
        }
        // an 8-digit number is the same number zero-padded to 9, so both
        // spellings normalise to one compact form
        return n.length() == 8 && Strings.isDigits(n) ? "0" + n : n;
    }

    /** The check digit for the base without its final digit (zero-padded to eight). */
    public static char calcCheckDigit(String base) {
        String b = base.length() < 8 ? "0".repeat(8 - base.length()) + base : base;
        int check = weightedRemainder(b, PRIMARY);
        if (check == 10) {
            check = weightedRemainder(b, SECONDARY);
        }
        return (char) ('0' + check);
    }

    private static int weightedRemainder(String base, int[] weights) {
        int sum = 0;
        for (int i = 0; i < weights.length && i < base.length(); i++) {
            sum += weights[i] * (base.charAt(i) - '0');
        }
        return Math.floorMod(-sum, 11);
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (!Strings.isDigits(n)) {
            throw new InvalidFormatException();
        }
        if (n.length() != 9) {
            throw new InvalidLengthException();
        }
        long value = Long.parseLong(n);
        if (value <= 10_000_000L || value >= 150_000_000L) {
            throw new InvalidComponentException(Message.of(NzIrd.class, "ird.range",
                    "The number is outside the issued range."));
        }
        if (n.charAt(n.length() - 1) != calcCheckDigit(n.substring(0, n.length() - 1))) {
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
