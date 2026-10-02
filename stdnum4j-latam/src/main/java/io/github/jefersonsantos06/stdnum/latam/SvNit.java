package io.github.jefersonsantos06.stdnum.latam;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
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


/**
 * NIT (Número de Identificación Tributaria), the Salvadoran tax number:
 * fourteen digits — a four-digit municipality (starting 0 or 1 for
 * nationals, 9 for foreigners), a six-digit date in DDMMYY, a three-digit
 * sequence and a check digit.
 *
 * <p>The check digit uses one of two weight sets, chosen by whether the
 * sequence number is at most 100 — the boundary between the old and the
 * current numbering.</p>
 *
 * <p>Since December 2021 an adult national's {@link SvDui} is also their
 * NIT, and the tax office takes the nine digits of the DUI wherever a NIT is
 * asked for. A nine-digit number is therefore validated as a DUI and written
 * with the DUI's mask.</p>
 */
public final class SvNit implements StdNum {

    public static final SvNit INSTANCE = new SvNit();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("sv.nit", "NIT")
                    .country("SV")
                    .title("Número de Identificación Tributaria")
                    .description("Salvadoran tax number: 14 digits with a weighted mod 11"
                            + " check digit in one of two schemes, or the 9-digit DUI"
                            + " that replaced it for adult nationals.")
                    .tags(Tag.TAX, Tag.VAT)
                    .references("https://www.mh.gob.sv/homologacion-de-nit-y-dui-simplifica-tramites/")
                    .build();

    private static final Mask MASK = Mask.of("####-######-###-#");
    private static final Mask DUI_MASK = Mask.of("########-#");

    private static final int[] OLD_WEIGHTS = {14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2};
    private static final int[] NEW_WEIGHTS = {2, 7, 6, 5, 4, 3, 2, 7, 6, 5, 4, 3, 2};

    private SvNit() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " -", "SV");
    }

    /** The check digit for a number whose first thirteen digits are known. */
    public static char calcCheckDigit(String number) {
        String n = INSTANCE.compact(number);
        boolean old = n.substring(10, 13).compareTo("100") <= 0;
        int[] weights = old ? OLD_WEIGHTS : NEW_WEIGHTS;
        int total = 0;
        for (int i = 0; i < weights.length && i < n.length(); i++) {
            total += weights[i] * (n.charAt(i) - '0');
        }
        return (char) ('0' + (old ? total % 11 % 10 : Math.floorMod(-total, 11) % 10));
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (n.length() == 9) {
            return SvDui.INSTANCE.validate(n);
        }
        if (n.length() != 14) {
            throw new InvalidLengthException();
        }
        if (!Strings.isDigits(n)) {
            throw new InvalidFormatException();
        }
        if ("019".indexOf(n.charAt(0)) < 0) {
            throw new InvalidComponentException(Message.of(SvNit.class, "nit.prefix",
                    "A NIT starts with 0, 1 or 9."));
        }
        if (Strings.allSame(n) && n.charAt(0) == '0') {
            throw new InvalidComponentException(Reasons.zeroSequence());
        }
        if (n.charAt(13) != calcCheckDigit(n)) {
            throw new InvalidChecksumException();
        }
        return n;
    }

    @Override
    public String format(String number) {
        return Mask.apply(masks(), validate(number));
    }

    @Override
    public List<Mask> masks() {
        return List.of(MASK, DUI_MASK);
    }
}
