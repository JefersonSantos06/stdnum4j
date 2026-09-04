package io.github.jefersonsantos06.stdnum.africa;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.Message;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * TIN, the Ghanaian tax identification number issued by the GRA: eleven
 * characters starting with a holder-type letter and {@code 00} —
 * {@code P} for individuals, {@code C} for companies, {@code G} for
 * government agencies, {@code Q} for foreign missions and {@code V} for
 * public institutions and trusts.
 */
public final class GhTin implements StdNum {

    public static final GhTin INSTANCE = new GhTin();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("gh.tin", "TIN")
                    .country("GH")
                    .title("Ghana Taxpayer Identification Number")
                    .description("Ghanaian tax number: a type letter, 00 and 8 characters"
                            + " with a weighted mod 11 check character.")
                    .tags(Tag.TAX)
                    .build();

    private static final Pattern STRUCTURE = Pattern.compile("[PCGQV]00[A-Z0-9]{8}");

    private GhTin() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " ").toUpperCase(Locale.ROOT);
    }

    /** The check character, computed over positions 2 to 10. */
    public static char calcCheckDigit(String number) {
        String n = INSTANCE.compact(number);
        int sum = 0;
        for (int i = 1; i < 10 && i < n.length(); i++) {
            char c = n.charAt(i);
            if (c < '0' || c > '9') {
                throw new InvalidFormatException(Message.of(GhTin.class, "tin.check-digits",
                        "The check character covers digits only."));
            }
            sum += i * (c - '0');
        }
        int check = sum % 11;
        return check == 10 ? 'X' : (char) ('0' + check);
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (n.length() != 11) {
            throw new InvalidLengthException();
        }
        if (!STRUCTURE.matcher(n).matches()) {
            throw new InvalidFormatException();
        }
        if (n.charAt(10) != calcCheckDigit(n)) {
            throw new InvalidChecksumException();
        }
        return n;
    }
}
