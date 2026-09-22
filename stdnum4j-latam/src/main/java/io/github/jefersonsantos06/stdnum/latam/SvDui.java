package io.github.jefersonsantos06.stdnum.latam;

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
 * DUI (Documento Único de Identidad), the Salvadoran identity card number:
 * eight digits and a check digit, the eight weighted 9 down to 2 and the sum
 * taken modulo 10.
 *
 * <p>Since December 2021 an adult national's DUI is also their tax number,
 * standing in for the fourteen-digit {@link SvNit}, which minors, foreigners
 * and companies keep.</p>
 */
public final class SvDui implements StdNum {

    public static final SvDui INSTANCE = new SvDui();

    private static final int[] WEIGHTS = Weighted.descending(9, 8);

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("sv.dui", "DUI")
                    .country("SV")
                    .title("Documento Único de Identidad")
                    .description("Salvadoran identity card number: 8 digits and a weighted"
                            + " mod 10 check digit; also the tax number of adult nationals.")
                    .tags(Tag.PERSON, Tag.TAX)
                    .references(
                            "https://www.mh.gob.sv/homologacion-de-nit-y-dui-simplifica-tramites/",
                            "https://github.com/avalon-tech/idsv-js")
                    .build();

    private static final Mask MASK = Mask.of("########-#");

    private SvDui() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " -");
    }

    /** The check digit for the eight digits that come before it. */
    public static char calcCheckDigit(String base) {
        Strings.requireDigits(base, 8);
        int sum = Weighted.weightedSum(base, WEIGHTS);
        return (char) ('0' + (10 - sum % 10) % 10);
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
        if (n.equals("000000000")) {
            throw new InvalidComponentException(Reasons.zeroSequence());
        }
        if (n.charAt(8) != calcCheckDigit(n.substring(0, 8))) {
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
