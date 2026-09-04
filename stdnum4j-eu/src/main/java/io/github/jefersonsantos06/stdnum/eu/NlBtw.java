package io.github.jefersonsantos06.stdnum.eu;

import io.github.jefersonsantos06.stdnum.algo.Mod97;
import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.util.Locale;

/**
 * Btw-identificatienummer, the Dutch VAT number: nine digits, the letter
 * {@code B} and a two-digit company sequence. Older numbers embed a BSN and
 * pass the elfproef; newer identification numbers instead pass the ISO 7064
 * MOD 97-10 check over {@code "NL" + number}. Either is accepted.
 */
public final class NlBtw implements StdNum {

    public static final NlBtw INSTANCE = new NlBtw();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("nl.btw", "Btw-nummer")
                    .country("NL")
                    .title("Btw-identificatienummer")
                    .description("Dutch VAT number: 9 digits, the letter B and a 2-digit"
                            + " sequence, checked with the elfproef or MOD 97-10.")
                    .tags(Tag.VAT)
                    .build();

    private NlBtw() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        String n = Strings.compact(number, " -.").toUpperCase(Locale.ROOT);
        if (n.startsWith("NL")) {
            n = n.substring(2);
        }
        return n.length() > 3 ? NlBsn.INSTANCE.compact(n.substring(0, n.length() - 3))
                + n.substring(n.length() - 3) : n;
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (n.length() != 12) {
            throw new InvalidLengthException();
        }
        if (!Strings.isDigits(n.substring(0, 9)) || Long.parseLong(n.substring(0, 9)) <= 0
                || n.charAt(9) != 'B'
                || !Strings.isDigits(n.substring(10)) || Integer.parseInt(n.substring(10)) <= 0) {
            throw new InvalidFormatException();
        }
        if (NlBsn.checksum(n.substring(0, 9)) != 0 && !Mod97.isValid("NL" + n)) {
            throw new InvalidChecksumException();
        }
        return n;
    }
}
