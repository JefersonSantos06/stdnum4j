package io.github.jefersonsantos06.stdnum.eu;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidComponentException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.Message;
import io.github.jefersonsantos06.stdnum.spi.Reasons;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Codice fiscale, the Italian tax code: sixteen characters derived from the
 * name, the date and place of birth and the sex, closed by a check letter.
 * A company carries an eleven-digit {@link ItIva} instead, which this type
 * also accepts.
 *
 * <p>Where two people would end up with the same code, the tax office
 * replaces digits with letters — L for 0, M for 1 and so on. Those letters
 * therefore read as digits wherever the code holds a number.</p>
 */
public final class ItCodicefiscale implements StdNum {

    public static final ItCodicefiscale INSTANCE = new ItCodicefiscale();

    private static final String LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    /** The substitutes for the digits 0..9 in a de-duplicated code. */
    private static final String DIGIT_LETTERS = "LMNPQRSTUV";
    /** The month of birth, January first. */
    private static final String MONTH_LETTERS = "ABCDEHLMPRST";
    private static final int[] ODD_VALUES = {
            1, 0, 5, 7, 9, 13, 15, 17, 19, 21, 2, 4, 18, 20, 11, 3, 6, 8,
            12, 14, 16, 10, 22, 25, 24, 23};

    private static final Pattern PATTERN = Pattern.compile(
            "[A-Z]{6}[0-9LMNPQRSTUV]{2}[ABCDEHLMPRST][0-9LMNPQRSTUV]{2}"
                    + "[A-Z][0-9LMNPQRSTUV]{3}[A-Z]");

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("it.codicefiscale", "Codice fiscale")
                    .country("IT")
                    .title("Codice fiscale italiano")
                    .description("Italian tax code: 16 characters derived from the name, the"
                            + " birth date and place and the sex, or a company VAT number.")
                    .tags(Tag.PERSON, Tag.TAX)
                    .references("https://it.wikipedia.org/wiki/Codice_fiscale")
                    .build();

    private ItCodicefiscale() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " -:").toUpperCase(Locale.ROOT);
    }

    /** The value of a character standing in a numeric position. */
    private static int dateDigit(char c) {
        int substitute = DIGIT_LETTERS.indexOf(c);
        if (substitute >= 0) {
            return substitute;
        }
        if (c < '0' || c > '9') {
            throw new InvalidFormatException();
        }
        return c - '0';
    }

    /** The check letter of a code, from its first fifteen characters. */
    public static char calcCheckDigit(String base) {
        String b = INSTANCE.compact(base);
        int code = 0;
        for (int i = 0; i < b.length(); i++) {
            char c = b.charAt(i);
            int value = c >= '0' && c <= '9' ? c - '0' : LETTERS.indexOf(c);
            if (value < 0) {
                throw new InvalidFormatException();
            }
            code += i % 2 == 0 ? ODD_VALUES[value] : value;
        }
        return LETTERS.charAt(code % 26);
    }

    /** The birth date encoded in the code, read into the century from 1920 on. */
    public static LocalDate getBirthDate(String number) {
        return getBirthDate(number, 1920);
    }

    /**
     * The birth date encoded in the code. Only two digits of the year are
     * stored, so the date is placed in the hundred years starting at
     * {@code minYear}.
     */
    public static LocalDate getBirthDate(String number, int minYear) {
        String n = INSTANCE.compact(number);
        if (n.length() != 16) {
            throw new InvalidComponentException(Message.of(ItCodicefiscale.class, "codicefiscale.birth-date-length",
                    "Only a 16-character code carries a birth date."));
        }
        int day = (dateDigit(n.charAt(9)) * 10 + dateDigit(n.charAt(10))) % 40;
        int month = MONTH_LETTERS.indexOf(n.charAt(8)) + 1;
        if (month == 0) {
            throw new InvalidComponentException(Message.of(ItCodicefiscale.class, "codicefiscale.month",
                    "The character in ninth position is not a month."));
        }
        int year = dateDigit(n.charAt(6)) * 10 + dateDigit(n.charAt(7)) + minYear / 100 * 100;
        if (year < minYear) {
            year += 100;
        }
        try {
            return LocalDate.of(year, month, day);
        } catch (DateTimeException e) {
            throw new InvalidComponentException(Reasons.birthDate());
        }
    }

    /** The sex recorded in the code: a woman's day of birth is offset by 40. */
    public static char getGender(String number) {
        String n = INSTANCE.compact(number);
        if (n.length() != 16) {
            throw new InvalidComponentException(Message.of(ItCodicefiscale.class, "codicefiscale.sex-length",
                    "Only a 16-character code carries a sex."));
        }
        return dateDigit(n.charAt(9)) * 10 + dateDigit(n.charAt(10)) < 32 ? 'M' : 'F';
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (n.length() == 11) {
            return ItIva.INSTANCE.validate(n);
        }
        if (n.length() != 16) {
            throw new InvalidLengthException();
        }
        if (!PATTERN.matcher(n).matches()) {
            throw new InvalidFormatException();
        }
        if (calcCheckDigit(n.substring(0, 15)) != n.charAt(15)) {
            throw new InvalidChecksumException();
        }
        getBirthDate(n);
        return n;
    }
}
