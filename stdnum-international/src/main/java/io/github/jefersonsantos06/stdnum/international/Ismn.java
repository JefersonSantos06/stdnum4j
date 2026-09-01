package io.github.jefersonsantos06.stdnum.international;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidComponentException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.spi.ValidationException;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.util.Locale;
import java.util.Optional;

/**
 * ISMN (International Standard Music Number), identifying sheet music:
 * either the legacy ten-character form starting with {@code M}, or the
 * thirteen-digit form in the {@code 9790} bookland prefix. Both are
 * validated with the EAN check digit.
 */
public final class Ismn implements StdNum {

    public static final Ismn INSTANCE = new Ismn();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("ismn", "ISMN")
                    .title("International Standard Music Number")
                    .description("Sheet music identifier in its legacy 10-character and"
                            + " 13-digit forms, with the EAN check digit.")
                    .tags(Tag.MEDIA)
                    .references("https://en.wikipedia.org/wiki/International_Standard_Music_Number")
                    .build();

    private Ismn() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " -.").toUpperCase(Locale.ROOT);
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (n.length() == 10) {
            if (n.charAt(0) != 'M') {
                throw new InvalidFormatException("A 10-character ISMN starts with M.");
            }
            Ean.INSTANCE.validate("9790" + n.substring(1));
        } else if (n.length() == 13) {
            if (!n.startsWith("9790")) {
                throw new InvalidComponentException("A 13-digit ISMN starts with 9790.");
            }
            Ean.INSTANCE.validate(n);
        } else {
            throw new InvalidLengthException();
        }
        return n;
    }

    /** Validates and returns the number in its 13-digit form. */
    public static String convertTo13(String number) {
        String n = INSTANCE.validate(number);
        return n.length() == 13 ? n : "9790" + n.substring(1);
    }

    /** The five parts of an ISMN in its 13-digit form. */
    public record Parts(String bookland, String prefix, String publisher,
                        String item, String checkDigit) {
    }

    /** Publisher element ranges: length, then the inclusive low and high bounds. */
    private static final String[][] RANGES = {
            {"3", "000", "099"}, {"4", "1000", "3999"}, {"5", "40000", "69999"},
            {"6", "700000", "899999"}, {"7", "9000000", "9999999"}};

    /** The two lengths an ISMN comes in. */
    public enum Type { ISMN10, ISMN13 }

    /** The form of the number, or empty when it is not a valid ISMN. */
    public static Optional<Type> ismnType(String number) {
        try {
            return Optional.of(INSTANCE.validate(number).length() == 10
                    ? Type.ISMN10 : Type.ISMN13);
        } catch (ValidationException e) {
            return Optional.empty();
        }
    }

    /**
     * Splits the number into bookland prefix, ISMN prefix, publisher
     * element, item element and check digit, converting to the 13-digit
     * form first.
     */
    public static Parts split(String number) {
        String n = convertTo13(number);
        for (String[] range : RANGES) {
            int length = Integer.parseInt(range[0]);
            String candidate = n.substring(4, 4 + length);
            if (candidate.compareTo(range[1]) >= 0 && candidate.compareTo(range[2]) <= 0) {
                return new Parts(n.substring(0, 3), n.substring(3, 4), candidate,
                        n.substring(4 + length, n.length() - 1),
                        n.substring(n.length() - 1));
            }
        }
        throw new InvalidComponentException("The publisher element is outside every range.");
    }

    /**
     * Hyphenates the number between its parts, keeping the form it was
     * given in. The 10-character form carries the same check digit as its
     * 13-digit counterpart, so only the two leading elements differ.
     *
     * <p>python-stdnum prints the 10-character form as the 13-digit one, which
     * loses the distinction {@code compact} and {@code validate} keep; the
     * conversion is available as {@link #convertTo13}, so a caller who wants
     * that presentation asks for it.</p>
     */
    @Override
    public String format(String number) {
        String n = validate(number);
        Parts p = split(n);
        return n.length() == 13
                ? String.join("-", p.bookland(), p.prefix(), p.publisher(),
                        p.item(), p.checkDigit())
                : String.join("-", "M", p.publisher(), p.item(), p.checkDigit());
    }

}
