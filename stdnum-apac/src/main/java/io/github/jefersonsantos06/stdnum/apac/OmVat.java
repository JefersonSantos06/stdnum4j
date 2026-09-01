package io.github.jefersonsantos06.stdnum.apac;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * VAT identification number of Oman: the literal {@code OM}, nine digits
 * and a check character that may be {@code X}.
 *
 * <p>Only the weights of the last five digits are publicly known, so the
 * first four of the body do not take part in the check — as in the
 * reference implementation this was mined from.</p>
 */
public final class OmVat implements StdNum {

    public static final OmVat INSTANCE = new OmVat();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("om.vat", "VAT")
                    .country("OM")
                    .title("Oman VAT identification number")
                    .description("Omani VAT number: OM, 9 digits and a check character that"
                            + " may be X.")
                    .tags(Tag.VAT)
                    .build();

    private static final Pattern STRUCTURE = Pattern.compile("OM[0-9]{9}[0-9X]");
    private static final int[] WEIGHTS = {1, 6, 3, 7, 9};

    private OmVat() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " -").toUpperCase(Locale.ROOT);
    }

    /** The check character, computed over the last five digits of the body. */
    public static char calcCheckDigit(String number) {
        String n = INSTANCE.compact(number);
        int check = 1;
        for (int i = 0; i < WEIGHTS.length && 6 + i < n.length(); i++) {
            check += WEIGHTS[i] * (n.charAt(6 + i) - '0');
        }
        check %= 11;
        return check == 10 ? 'X' : (char) ('0' + check);
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (n.length() != 12) {
            throw new InvalidLengthException();
        }
        if (!STRUCTURE.matcher(n).matches()) {
            throw new InvalidFormatException();
        }
        if (n.charAt(11) != calcCheckDigit(n)) {
            throw new InvalidChecksumException();
        }
        return n;
    }
}
