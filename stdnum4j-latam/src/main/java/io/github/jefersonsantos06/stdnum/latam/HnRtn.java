package io.github.jefersonsantos06.stdnum.latam;

import io.github.jefersonsantos06.stdnum.algo.Weighted;
import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidComponentException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.Message;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

/**
 * RTN (Registro Tributario Nacional), the Honduran tax number: fourteen
 * digits opening with the department and municipality where it was issued
 * and closing with a check digit. An individual's RTN is their thirteen-digit
 * DNI, whose next four digits are the year of birth, with the check digit
 * appended; a company's carries a 9 in fifth place.
 *
 * <p>The SAR does not publish the check digit. The rule here — the first
 * thirteen digits weighted 1, 7, 5, 3 over and over, modulo 11, with a
 * remainder of 10 written as 0 — holds for 350 of the 353 RTNs gathered from
 * public procurement records in python-stdnum issue #148, and all of them
 * name one of the 298 municipalities.</p>
 */
public final class HnRtn implements StdNum {

    public static final HnRtn INSTANCE = new HnRtn();

    private static final int[] WEIGHTS = {1, 7, 5, 3, 1, 7, 5, 3, 1, 7, 5, 3, 1};

    /** How many municipalities each department has, Atlántida (01) first. */
    private static final int[] MUNICIPALITIES =
            {8, 10, 21, 23, 12, 16, 19, 28, 6, 17, 4, 19, 28, 16, 23, 28, 9, 11};

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("hn.rtn", "RTN")
                    .country("HN")
                    .title("Registro Tributario Nacional")
                    .description("Honduran tax number: 14 digits opening with the department and"
                            + " municipality, with a weighted mod 11 check digit.")
                    .tags(Tag.TAX, Tag.VAT)
                    .references(
                            "https://www.sar.gob.hn/registro-tributario-nacional-rtn/",
                            "https://github.com/arthurdejong/python-stdnum/issues/148")
                    .build();

    private HnRtn() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " -");
    }

    /** The check digit for the thirteen digits that come before it. */
    public static char calcCheckDigit(String base) {
        Strings.requireDigits(base, 13);
        return (char) ('0' + Weighted.weightedSum(base, WEIGHTS) % 11 % 10);
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (!Strings.isDigits(n)) {
            throw new InvalidFormatException();
        }
        if (n.length() != 14) {
            throw new InvalidLengthException();
        }
        int department = Integer.parseInt(n.substring(0, 2));
        if (department < 1 || department > MUNICIPALITIES.length) {
            throw new InvalidComponentException(Message.of(HnRtn.class, "rtn.department",
                    "Not the code of a Honduran department."));
        }
        int municipality = Integer.parseInt(n.substring(2, 4));
        if (municipality < 1 || municipality > MUNICIPALITIES[department - 1]) {
            throw new InvalidComponentException(Message.of(HnRtn.class, "rtn.municipality",
                    "The department has no municipality with this code."));
        }
        if (n.charAt(13) != calcCheckDigit(n.substring(0, 13))) {
            throw new InvalidChecksumException();
        }
        return n;
    }
}
