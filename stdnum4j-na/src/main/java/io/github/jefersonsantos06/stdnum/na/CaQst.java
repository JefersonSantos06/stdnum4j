package io.github.jefersonsantos06.stdnum.na;

import io.github.jefersonsantos06.stdnum.algo.Weighted;
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
 * QST (Quebec Sales Tax) registration number, the TVQ number Revenu Québec
 * gives a business: ten digits, the last a check digit, usually followed by
 * the program identifier {@code TQ} and a four-digit reference for the
 * account — {@code 1000042605 TQ0020}. Québec businesses print it on their
 * invoices beside their GST number, which is a {@link CaBn}.
 *
 * <p>Revenu Québec does not publish the check digit. The rule here, taken
 * from stdnum-js — the first nine digits weighted 4, 3, 2, 7, 6, 5, 4, 3, 2,
 * modulo 11, subtracted from 11 and taken modulo 10 — holds for the numbers
 * Hydro-Québec and three CN companies publish. The {@code NR} numbers of the
 * specified registration system for suppliers outside Québec are not
 * covered.</p>
 */
public final class CaQst implements StdNum {

    public static final CaQst INSTANCE = new CaQst();

    private static final int[] WEIGHTS = {4, 3, 2, 7, 6, 5, 4, 3, 2};

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("ca.qst", "QST")
                    .country("CA")
                    .title("Quebec Sales Tax number")
                    .description("Québec sales tax registration number: 10 digits with a weighted"
                            + " mod 11 check digit, optionally followed by TQ and a 4-digit reference.")
                    .tags(Tag.COMPANY, Tag.TAX)
                    .references(
                            "https://github.com/koblas/stdnum-js/blob/main/src/ca/qst.ts",
                            "https://www.cn.ca/-/media/Files/suppliers/Documents/gst-qst-registration-numbers-fr.pdf")
                    .build();

    private static final List<Mask> MASKS = List.of(Mask.of("9999999999 AA9999"));

    private CaQst() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " -").toUpperCase(Locale.ROOT);
    }

    /** The check digit for the nine digits that come before it. */
    public static char calcCheckDigit(String base) {
        Strings.requireDigits(base, 9);
        return (char) ('0' + (11 - Weighted.weightedSum(base, WEIGHTS) % 11) % 10);
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (n.length() != 10 && n.length() != 16) {
            throw new InvalidLengthException();
        }
        if (!Strings.isDigits(n.substring(0, 10))) {
            throw new InvalidFormatException();
        }
        if (n.length() == 16) {
            if (!n.startsWith("TQ", 10)) {
                throw new InvalidComponentException(Message.of(CaQst.class, "qst.program",
                        "The program identifier of a QST account is TQ."));
            }
            if (!Strings.isDigits(n.substring(12))) {
                throw new InvalidFormatException();
            }
            if (n.endsWith("0000")) {
                throw new InvalidComponentException(Message.of(CaQst.class, "qst.reference",
                        "An account reference is never 0000."));
            }
        }
        if (n.charAt(9) != calcCheckDigit(n.substring(0, 9))) {
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
