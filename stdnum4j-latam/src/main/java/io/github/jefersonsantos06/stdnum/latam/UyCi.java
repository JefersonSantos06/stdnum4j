package io.github.jefersonsantos06.stdnum.latam;

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
 * CI (Cédula de Identidad), the Uruguayan identity card number: a number of
 * up to seven digits and a check digit, written {@code 1.234.567-2}. The
 * number is weighted 2, 9, 8, 7, 6, 3, 4 — read as seven digits, zeros in
 * front — and the check digit brings the sum to a multiple of ten.
 *
 * <p>The card is issued to citizens and resident foreigners alike, and it is
 * the document an individual buyer gives on an e-Ticket.</p>
 */
public final class UyCi implements StdNum {

    public static final UyCi INSTANCE = new UyCi();

    private static final int[] WEIGHTS = {2, 9, 8, 7, 6, 3, 4};

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("uy.ci", "CI")
                    .country("UY")
                    .title("Cédula de Identidad")
                    .description("Uruguayan identity card number: up to 7 digits and a check digit"
                            + " weighted 2,9,8,7,6,3,4 modulo 10.")
                    .tags(Tag.PERSON)
                    .references(
                            "https://ciuy.readthedocs.io/es/latest/about.html",
                            "https://es.wikipedia.org/wiki/C%C3%A9dula_de_Identidad_(Uruguay)")
                    .build();

    private static final List<Mask> MASKS = List.of(Mask.of("9.999.999-9"), Mask.of("999.999-9"));

    private UyCi() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " .-");
    }

    /** The check digit for the number that comes before it, of up to seven digits. */
    public static char calcCheckDigit(String body) {
        if (!Strings.isDigits(body)) {
            throw new InvalidFormatException();
        }
        if (body.length() > 7) {
            throw new InvalidLengthException();
        }
        String padded = Strings.padStart(body, 7);
        int sum = 0;
        for (int i = 0; i < WEIGHTS.length; i++) {
            sum += WEIGHTS[i] * (padded.charAt(i) - '0');
        }
        return (char) ('0' + (10 - sum % 10) % 10);
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
        String body = n.substring(0, n.length() - 1);
        if (Strings.allSame(body) && body.charAt(0) == '0') {
            throw new InvalidComponentException(Reasons.zeroSequence());
        }
        if (n.charAt(n.length() - 1) != calcCheckDigit(body)) {
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
