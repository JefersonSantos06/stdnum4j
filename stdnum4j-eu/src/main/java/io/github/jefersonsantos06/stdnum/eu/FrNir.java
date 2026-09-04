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
 * NIR, the French social security number: fifteen characters encoding sex,
 * year and month of birth, place of birth and an order number, closed by two
 * check digits.
 *
 * <p>The department is the only non-digit part: Corsica uses {@code 2A} and
 * {@code 2B}, which stand in for 19 and 18 when the check digits are
 * computed.</p>
 */
public final class FrNir implements StdNum {

    public static final FrNir INSTANCE = new FrNir();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("fr.nir", "NIR")
                    .country("FR")
                    .title("Numero d'inscription au repertoire")
                    .description("French social security number: 13 characters describing the"
                            + " person plus two mod 97 check digits.")
                    .tags(Tag.PERSON, Tag.HEALTH)
                    .references("https://en.wikipedia.org/wiki/INSEE_code")
                    .build();

    private FrNir() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " .").toUpperCase(Locale.ROOT);
    }

    /**
     * The two check digits of a number, from its first thirteen characters.
     * A Corsican department is substituted before the arithmetic.
     */
    public static String calcCheckDigits(String number) {
        String n = INSTANCE.compact(number);
        String department = n.substring(5, 7);
        if (department.equals("2A")) {
            n = n.substring(0, 5) + "19" + n.substring(7);
        } else if (department.equals("2B")) {
            n = n.substring(0, 5) + "18" + n.substring(7);
        }
        long head = Long.parseLong(n.substring(0, 13));
        return String.format("%02d", 97 - head % 97);
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (n.length() != 15) {
            throw new InvalidLengthException();
        }
        if (!Strings.isDigits(n.substring(0, 5)) || !Strings.isDigits(n.substring(7))) {
            throw new InvalidFormatException();
        }
        String department = n.substring(5, 7);
        if (!Strings.isDigits(department)
                && !department.equals("2A") && !department.equals("2B")) {
            throw new InvalidFormatException();
        }
        if (!calcCheckDigits(n).equals(n.substring(13))) {
            throw new InvalidChecksumException();
        }
        return n;
    }

    @Override
    public String format(String number) {
        String n = validate(number);
        return String.join(" ", n.substring(0, 1), n.substring(1, 3), n.substring(3, 5),
                n.substring(5, 7), n.substring(7, 10), n.substring(10, 13), n.substring(13));
    }
}
