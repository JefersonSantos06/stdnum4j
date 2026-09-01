package io.github.jefersonsantos06.stdnum.apac;

import io.github.jefersonsantos06.stdnum.algo.Luhn;
import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidComponentException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * GSTIN (Goods and Services Tax identification number), the Indian VAT
 * number: a two-digit state code, the holder's {@link InPan}, a
 * registration counter, the literal {@code Z} and a Luhn mod 36 check
 * character.
 */
public final class InGstin implements StdNum {

    public static final InGstin INSTANCE = new InGstin();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("in.gstin", "GSTIN")
                    .country("IN")
                    .title("Goods and Services Tax identification number")
                    .description("Indian VAT number: a state code, the PAN, a registration"
                            + " counter, Z and a Luhn mod 36 check character.")
                    .tags(Tag.VAT, Tag.TAX)
                    .build();

    private static final Pattern STRUCTURE =
            Pattern.compile("[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z][0-9A-Z]{2}[0-9A-Z]");
    private static final String ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    /** State codes assigned by the GST Council. */
    private static final Set<String> STATE_CODES = Set.of(
            "01", "02", "03", "04", "05", "06", "07", "08", "09", "10",
            "11", "12", "13", "14", "15", "16", "17", "18", "19", "20",
            "21", "22", "23", "24", "25", "26", "27", "28", "29", "30",
            "31", "32", "33", "34", "35", "36", "37", "38", "97", "99");

    private InGstin() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " -").toUpperCase(Locale.ROOT);
    }

    /** The PAN embedded in the number. */
    public static String toPan(String number) {
        return INSTANCE.validate(number).substring(2, 12);
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (n.length() != 15) {
            throw new InvalidLengthException();
        }
        if (!STRUCTURE.matcher(n).matches()) {
            throw new InvalidFormatException();
        }
        if (!STATE_CODES.contains(n.substring(0, 2))) {
            throw new InvalidComponentException("Unknown state code.");
        }
        if (n.charAt(12) == '0') {
            throw new InvalidComponentException("The registration counter must not be zero.");
        }
        if (n.charAt(13) != 'Z') {
            throw new InvalidComponentException("The fourteenth character of a GSTIN is Z.");
        }
        InPan.INSTANCE.validate(n.substring(2, 12));
        Luhn.validate(n, ALPHABET);
        return n;
    }
}
