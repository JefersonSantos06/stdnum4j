package io.github.jefersonsantos06.stdnum.eu;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.util.Locale;

/**
 * The card number of a Belgian electronic identity card: twelve digits whose
 * last two are the first ten modulo 97, with a remainder of 0 written as 97.
 *
 * <p>It identifies the card rather than its holder, who is identified by the
 * {@link BeNn} printed alongside it, so a replacement card carries a new
 * number.</p>
 */
public final class BeEid implements StdNum {

    public static final BeEid INSTANCE = new BeEid();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("be.eid", "eID")
                    .country("BE")
                    .title("Belgisch eID-kaartnummer")
                    .description("Card number of a Belgian electronic identity card: 12 digits"
                            + " whose last two are the first ten modulo 97.")
                    .tags(Tag.PERSON)
                    .references("https://www.ibz.rrn.fgov.be/")
                    .build();

    private BeEid() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " -./").toUpperCase(Locale.ROOT);
    }

    /** The two check digits of a card number, from its first ten digits. */
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
        if (!calcCheckDigits(n.substring(0, 10)).equals(n.substring(10))) {
            throw new InvalidChecksumException();
        }
        return n;
    }

    @Override
    public String format(String number) {
        String n = validate(number);
        return n.substring(0, 3) + '-' + n.substring(3, 10) + '-' + n.substring(10);
    }
}
