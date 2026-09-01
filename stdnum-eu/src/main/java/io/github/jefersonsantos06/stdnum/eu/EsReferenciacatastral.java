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
 * The Spanish cadastral reference, which identifies a property: the parcel,
 * the building on it, the property within the building and two check
 * letters.
 *
 * <p>The two check letters are computed over different halves of the
 * reference, both taken together with the property, so a transposition
 * between the parcel and the building is caught.</p>
 */
public final class EsReferenciacatastral implements StdNum {

    public static final EsReferenciacatastral INSTANCE = new EsReferenciacatastral();

    /** The Spanish alphabet, N-with-tilde included, followed by the digits. */
    private static final String ALPHABET = "ABCDEFGHIJKLMNÑOPQRSTUVWXYZ0123456789";
    private static final String CHECK_LETTERS = "MQWERTYUIOPASDFGHJKLBZX";
    private static final int[] WEIGHTS = {13, 15, 12, 5, 4, 17, 9, 21, 3, 7, 1};

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("es.referenciacatastral", "Referencia catastral")
                    .country("ES")
                    .title("Referencia catastral espanola")
                    .description("Spanish cadastral reference of a property: 20 characters"
                            + " with two check letters.")
                    .tags(Tag.LOCATION)
                    .references("https://www.catastro.minhap.es/")
                    .build();

    private EsReferenciacatastral() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " -").toUpperCase(Locale.ROOT);
    }

    /** One check letter, over eleven characters. */
    private static char checkDigit(String part) {
        int sum = 0;
        for (int i = 0; i < WEIGHTS.length && i < part.length(); i++) {
            char c = part.charAt(i);
            int value = c >= '0' && c <= '9' ? c - '0' : ALPHABET.indexOf(c) + 1;
            sum += WEIGHTS[i] * value;
        }
        return CHECK_LETTERS.charAt(sum % 23);
    }

    /** The two check letters of a reference, from the parts they cover. */
    public static String calcCheckDigits(String number) {
        String n = INSTANCE.compact(number);
        return "" + checkDigit(n.substring(0, 7) + n.substring(14, 18))
                + checkDigit(n.substring(7, 14) + n.substring(14, 18));
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        for (int i = 0; i < n.length(); i++) {
            if (ALPHABET.indexOf(n.charAt(i)) < 0) {
                throw new InvalidFormatException();
            }
        }
        if (n.length() != 20) {
            throw new InvalidLengthException();
        }
        if (!calcCheckDigits(n).equals(n.substring(18))) {
            throw new InvalidChecksumException();
        }
        return n;
    }

    @Override
    public String format(String number) {
        String n = validate(number);
        return String.join(" ", n.substring(0, 7), n.substring(7, 14),
                n.substring(14, 18), n.substring(18));
    }
}
