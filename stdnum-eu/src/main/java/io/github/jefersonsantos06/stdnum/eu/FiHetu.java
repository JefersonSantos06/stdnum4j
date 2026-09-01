package io.github.jefersonsantos06.stdnum.eu;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidComponentException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * HETU, the Finnish personal identity code: the date of birth, a character
 * giving the century, a serial number and a check character.
 *
 * <p>Finland ran out of century characters for people born in the 2000s and
 * added more in 2023, which is why several characters map to the same
 * century.</p>
 */
public final class FiHetu implements StdNum {

    public static final FiHetu INSTANCE = new FiHetu();

    private static final String CHECK_CHARACTERS = "0123456789ABCDEFHJKLMNPRSTUVWXY";
    private static final Pattern PATTERN = Pattern.compile(
            "([0-3][0-9])([01][0-9])([0-9]{2})([-+ABCDEFYXWVU])([0-9]{3})"
                    + "([0-9ABCDEFHJKLMNPRSTUVWXY])");

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("fi.hetu", "HETU")
                    .country("FI")
                    .title("Suomalainen henkilotunnus")
                    .description("Finnish personal identity code: the date of birth, a century"
                            + " character, a serial number and a mod 31 check character.")
                    .tags(Tag.PERSON)
                    .references("https://en.wikipedia.org/wiki/National_identification_number#Finland")
                    .build();

    private FiHetu() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, "").toUpperCase(Locale.ROOT);
    }

    /** The century the character in seventh position stands for. */
    private static int century(char code) {
        if (code == '+') {
            return 1800;
        }
        return "-YXWVU".indexOf(code) >= 0 ? 1900 : 2000;
    }

    /** The check character of a number, from the date and serial it carries. */
    public static char calcCheckDigit(String number) {
        Matcher m = PATTERN.matcher(INSTANCE.compact(number));
        if (!m.matches()) {
            throw new InvalidFormatException();
        }
        return CHECK_CHARACTERS.charAt(
                Integer.parseInt(m.group(1) + m.group(2) + m.group(3) + m.group(5)) % 31);
    }

    /** The birth date encoded in the number. */
    public static LocalDate getBirthDate(String number) {
        Matcher m = PATTERN.matcher(INSTANCE.compact(number));
        if (!m.matches()) {
            throw new InvalidFormatException();
        }
        try {
            return LocalDate.of(century(m.group(4).charAt(0)) + Integer.parseInt(m.group(3)),
                    Integer.parseInt(m.group(2)), Integer.parseInt(m.group(1)));
        } catch (DateTimeException e) {
            throw new InvalidComponentException(
                    "The number does not contain a valid birth date.");
        }
    }

    @Override
    public String validate(String number) {
        return validate(number, false);
    }

    /**
     * Validates the number, optionally accepting the 900..999 serial range
     * that is set aside for temporary identifiers.
     */
    public String validate(String number, boolean allowTemporary) {
        String n = compact(number);
        Matcher m = PATTERN.matcher(n);
        if (!m.matches()) {
            throw new InvalidFormatException();
        }
        getBirthDate(n);
        int individual = Integer.parseInt(m.group(5));
        if (individual < 2) {
            throw new InvalidComponentException("The serial number starts at 002.");
        }
        if (individual >= 900 && !allowTemporary) {
            throw new InvalidComponentException("This is a temporary identity code.");
        }
        if (m.group(6).charAt(0) != calcCheckDigit(n)) {
            throw new InvalidChecksumException();
        }
        return n;
    }
}
