package io.github.jefersonsantos06.stdnum.eu;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Mask;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.util.List;

/**
 * OGM, the Belgian structured payment reference: twelve digits whose last two
 * are the first ten modulo 97, with a remainder of 0 written as 97 so the
 * check digits are never 00.
 *
 * <p>Quoting it on a transfer lets the creditor match the payment to the
 * invoice without anyone reading the message line.</p>
 */
public final class BeOgmVcs implements StdNum {

    public static final BeOgmVcs INSTANCE = new BeOgmVcs();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("be.ogm_vcs", "OGM")
                    .country("BE")
                    .title("Belgisch gestructureerde mededeling")
                    .description("Belgian structured payment reference: 12 digits whose last"
                            + " two are the first ten modulo 97.")
                    .tags(Tag.PAYMENT, Tag.BANK)
                    .references("https://nl.wikipedia.org/wiki/Gestructureerde_mededeling")
                    .build();

    private static final Mask MASK = Mask.of("###/####/#####");

    private BeOgmVcs() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " +/");
    }

    /** The two check digits of a reference, from its first ten digits. */
    public static String calcCheckDigits(String number) {
        String n = INSTANCE.compact(number);
        long check = Long.parseLong(n.substring(0, 10)) % 97;
        return String.format("%02d", check == 0 ? 97 : check);
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (!Strings.isDigits(n) || n.chars().allMatch(c -> c == '0')) {
            throw new InvalidFormatException();
        }
        if (n.length() != 12) {
            throw new InvalidLengthException();
        }
        if (!calcCheckDigits(n).equals(n.substring(10))) {
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
