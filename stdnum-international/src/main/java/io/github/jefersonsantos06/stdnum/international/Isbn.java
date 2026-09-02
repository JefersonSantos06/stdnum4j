package io.github.jefersonsantos06.stdnum.international;

import io.github.jefersonsantos06.stdnum.numdb.NumDb;
import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidComponentException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.Message;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.spi.ValidationException;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * ISBN (International Standard Book Number).
 *
 * <p>Accepts ISBN-10 (weighted mod 11, {@code X} check character) and
 * ISBN-13 (bookland prefix 978/979, GS1 check digit). {@code format()}
 * hyphenates the parts using the official range data in {@code isbn.dat}
 * when the registration group and publisher are known, and falls back to the
 * compact form otherwise. {@link #convertTo13(String)} and
 * {@link #convertTo10(String)} translate between the two forms.</p>
 */
public final class Isbn implements StdNum {

    public static final Isbn INSTANCE = new Isbn();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("isbn", "ISBN")
                    .title("International Standard Book Number")
                    .description("Book identifier in its 10-digit (mod 11) and 13-digit"
                            + " (bookland EAN) forms, hyphenated with the official"
                            + " registration group and publisher ranges.")
                    .tags(Tag.MEDIA)
                    .references("https://www.isbn-international.org/content/what-isbn/10",
                            "https://en.wikipedia.org/wiki/ISBN")
                    .build();

    private Isbn() {
    }

    private static NumDb ranges() {
        return NumDb.load(Isbn.class, "isbn.dat");
    }

    /** The parts of a hyphenated ISBN-13. */
    public record Parts(String prefix, String group, String publisher,
                        String item, char checkDigit) {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        String n = Strings.compact(number, " -").toUpperCase(Locale.ROOT);
        // an ISBN-10 whose leading zero was dropped is still an ISBN-10
        return n.length() == 9 ? "0" + n : n;
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (n.length() == 10) {
            validate10(n);
        } else if (n.length() == 13) {
            validate13(n);
        } else {
            if (!isIsbnAlphabet(n)) {
                throw new InvalidFormatException();
            }
            throw new InvalidLengthException();
        }
        return n;
    }

    private static boolean isIsbnAlphabet(String s) {
        if (s.isEmpty()) {
            return false;
        }
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if ((c < '0' || c > '9') && c != 'X') {
                return false;
            }
        }
        return true;
    }

    private static void validate10(String n) {
        if (!Strings.isDigits(n.substring(0, 9))
                || (!Strings.isDigits(n.substring(9)) && n.charAt(9) != 'X')) {
            throw new InvalidFormatException();
        }
        int sum = 0;
        for (int i = 0; i < 10; i++) {
            int value = n.charAt(i) == 'X' ? 10 : n.charAt(i) - '0';
            sum += (10 - i) * value;
        }
        if (sum % 11 != 0) {
            throw new InvalidChecksumException();
        }
    }

    private static void validate13(String n) {
        if (!Strings.isDigits(n)) {
            throw new InvalidFormatException();
        }
        if (!n.startsWith("978") && !n.startsWith("979")) {
            throw new InvalidComponentException(Message.of(Isbn.class, "isbn.bookland",
                    "An ISBN-13 must start with 978 or 979."));
        }
        if (Ean.checksum(n) != 0) {
            throw new InvalidChecksumException();
        }
    }

    /** The ISBN-10 check character for a 9-digit base. */
    public static char calcCheckDigit10(String base) {
        // note: not INSTANCE.compact, which zero-pads a 9-character *number*
        // to ISBN-10 — here nine characters are the expected base
        String b = Strings.compact(base, " -").toUpperCase(Locale.ROOT);
        if (!Strings.isDigits(b)) {
            throw new InvalidFormatException();
        }
        if (b.length() != 9) {
            throw new InvalidLengthException();
        }
        int sum = 0;
        for (int i = 0; i < 9; i++) {
            sum += (10 - i) * (b.charAt(i) - '0');
        }
        int check = (11 - sum % 11) % 11;
        return check == 10 ? 'X' : (char) ('0' + check);
    }

    /** Validates and returns the number in its 13-digit form. */
    public static String convertTo13(String number) {
        String n = INSTANCE.validate(number);
        if (n.length() == 13) {
            return n;
        }
        String base = "978" + n.substring(0, 9);
        return base + Ean.calcCheckDigit(base);
    }

    /**
     * Validates and returns the number in its 10-digit form; only numbers in
     * the 978 bookland prefix have one.
     */
    public static String convertTo10(String number) {
        String n = INSTANCE.validate(number);
        if (n.length() == 10) {
            return n;
        }
        if (!n.startsWith("978")) {
            throw new InvalidComponentException(Message.of(Isbn.class, "isbn.convert-10",
                    "Only 978-prefixed ISBNs have a 10-digit form."));
        }
        String base = n.substring(3, 12);
        return base + calcCheckDigit10(base);
    }

    /**
     * Splits a valid ISBN into prefix, registration group, publisher, item
     * and check digit using the official ranges; empty when the group or
     * publisher is not in the range data.
     */
    public static Optional<Parts> split(String number) {
        String isbn13 = convertTo13(number);
        List<NumDb.Entry> info = ranges().info(isbn13);
        if (info.size() != 4 || info.get(3).part().length() < 2) {
            return Optional.empty();
        }
        String rest = info.get(3).part();
        return Optional.of(new Parts(
                info.get(0).part(),
                info.get(1).part(),
                info.get(2).part(),
                rest.substring(0, rest.length() - 1),
                rest.charAt(rest.length() - 1)));
    }

    @Override
    public String format(String number) {
        String n = validate(number);
        Optional<Parts> parts = split(n);
        if (parts.isEmpty()) {
            return n;
        }
        Parts p = parts.get();
        if (n.length() == 13) {
            return p.prefix() + "-" + p.group() + "-" + p.publisher() + "-"
                    + p.item() + "-" + p.checkDigit();
        }
        return p.group() + "-" + p.publisher() + "-" + p.item() + "-" + n.charAt(9);
    }

    /** Which form a valid number is in. */
    public enum Type { ISBN10, ISBN13 }

    /** The form of the number, or empty when it is not a valid ISBN. */
    public static Optional<Type> isbnType(String number) {
        try {
            return Optional.of(INSTANCE.validate(number).length() == 10
                    ? Type.ISBN10 : Type.ISBN13);
        } catch (ValidationException e) {
            return Optional.empty();
        }
    }

}
