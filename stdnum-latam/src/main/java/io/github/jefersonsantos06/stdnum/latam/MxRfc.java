package io.github.jefersonsantos06.stdnum.latam;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidComponentException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.spi.ValidationException;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.MonthDay;
import java.time.Year;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * RFC (Registro Federal de Contribuyentes), the Mexican tax number: four
 * name letters, a six-digit date and a three-character "homoclave" for
 * individuals, or three name letters for companies. The number may also
 * appear as ten or twelve characters without the homoclave.
 *
 * <p>The six date digits must name a real day, and a personal number may
 * not open with one of the words the SAT strikes out of its own name
 * algorithm. The check character that closes the homoclave is <em>not</em>
 * verified unless asked for: the SAT has issued, and recognises, numbers
 * whose check character is wrong — around one in seventy of those in
 * circulation — so a validator that insisted on it would turn away real
 * taxpayers. Pass {@code true} to {@link #validate(String, boolean)} to
 * demand it.</p>
 */
public final class MxRfc implements StdNum {

    public static final MxRfc INSTANCE = new MxRfc();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("mx.rfc", "RFC")
                    .country("MX")
                    .title("Registro Federal de Contribuyentes")
                    .description("Mexican tax number: 12 characters for companies or 13 for"
                            + " individuals, with a mod 11 check character.")
                    .tags(Tag.TAX, Tag.VAT)
                    .build();

    private static final Pattern PERSONAL = Pattern.compile("[A-Z&Ñ]{4}[0-9]{6}[0-9A-Z]{0,3}");
    private static final Pattern COMPANY = Pattern.compile("[A-Z&Ñ]{3}[0-9]{6}[0-9A-Z]{3}");
    private static final Pattern HOMOCLAVE = Pattern.compile("[1-9A-V][1-9A-Z][0-9A]");

    private static final String ALPHABET = "0123456789ABCDEFGHIJKLMN&OPQRSTUVWXYZ Ñ";

    /**
     * The words a personal number may not open with. The SAT's own name
     * algorithm produces them from ordinary surnames, and its rules then
     * strike them out, substituting an X for the last letter.
     */
    private static final Set<String> FORBIDDEN_PREFIXES = Set.of(
            "BUEI", "BUEY", "CACA", "CACO", "CAGA", "CAGO", "CAKA", "CAKO",
            "COGE", "COJA", "COJE", "COJI", "COJO", "CULO", "FETO", "GUEY",
            "JOTO", "KACA", "KACO", "KAGA", "KAGO", "KAKA", "KOGE", "KOJO",
            "KULO", "MAME", "MAMO", "MEAR", "MEAS", "MEON", "MION", "MOCO",
            "MULA", "PEDA", "PEDO", "PENE", "PUTA", "PUTO", "QULO", "RATA",
            "RUIN");

    private MxRfc() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, "-_ ").toUpperCase(Locale.ROOT);
    }

    /** The check character, computed over everything before it. */
    public static char calcCheckDigit(String base) {
        String b = INSTANCE.compact(base);
        // the calculation is right-aligned on a 12-character field
        String padded = b.length() < 12 ? " ".repeat(12 - b.length()) + b : b;
        int check = 0;
        for (int i = 0; i < padded.length(); i++) {
            int value = ALPHABET.indexOf(padded.charAt(i));
            if (value < 0) {
                throw new InvalidFormatException();
            }
            check += value * (13 - i);
        }
        check = 11 - check % 11;
        return check == 11 ? '0' : check == 10 ? 'A' : (char) ('0' + check);
    }

    /**
     * The birth or incorporation date encoded in the number.
     *
     * <p>The number records only two digits of the year, and neither a birth
     * nor an incorporation lies in the future, so the year is read as the
     * most recent one ending in those digits.</p>
     */
    public static LocalDate getDate(String number) {
        String n = INSTANCE.validate(number);
        int offset = n.length() == 12 ? 3 : 4;
        MonthDay monthDay = checkDate(n.substring(offset, offset + 6));
        int suffix = Integer.parseInt(n.substring(offset, offset + 2));
        int today = Year.now().getValue();
        int year = today - Math.floorMod(today - suffix, 100);
        try {
            return monthDay.atYear(year);
        } catch (DateTimeException e) {
            // 29 February, and that year is not a leap year: the day meant is
            // the same one a century earlier
            return monthDay.atYear(year - 100);
        }
    }

    /**
     * The day the six date digits name.
     *
     * @throws InvalidComponentException if they name no day at all. The
     *                                   century is not recorded, so 29
     *                                   February is read against a leap year
     *                                   and accepted.
     */
    private static MonthDay checkDate(String yymmdd) {
        try {
            return MonthDay.of(Integer.parseInt(yymmdd.substring(2, 4)),
                    Integer.parseInt(yymmdd.substring(4, 6)));
        } catch (DateTimeException | NumberFormatException e) {
            throw new InvalidComponentException("The number does not name a day.");
        }
    }

    @Override
    public String validate(String number) {
        return validate(number, false);
    }

    /**
     * Validates the number, optionally demanding the check character too.
     *
     * @param validateCheckDigits whether the homoclave must be well formed and
     *                            close with the character the checksum calls
     *                            for. Numbers the SAT itself issued fail this,
     *                            which is why it is off by default.
     */
    public String validate(String number, boolean validateCheckDigits) {
        String n = compact(number);
        if (n.length() == 10 || n.length() == 13) {
            if (!PERSONAL.matcher(n).matches()) {
                throw new InvalidFormatException();
            }
            if (FORBIDDEN_PREFIXES.contains(n.substring(0, 4))) {
                throw new InvalidComponentException(
                        "A personal number does not open with " + n.substring(0, 4) + ".");
            }
            checkDate(n.substring(4, 10));
        } else if (n.length() == 12) {
            if (!COMPANY.matcher(n).matches()) {
                throw new InvalidFormatException();
            }
            checkDate(n.substring(3, 9));
        } else {
            throw new InvalidLengthException();
        }
        if (validateCheckDigits && n.length() >= 12) {
            String homoclave = n.substring(n.length() - 3);
            if (!HOMOCLAVE.matcher(homoclave).matches()) {
                throw new InvalidComponentException("Malformed homoclave.");
            }
            if (n.charAt(n.length() - 1) != calcCheckDigit(n.substring(0, n.length() - 1))) {
                throw new InvalidChecksumException();
            }
        }
        return n;
    }

    /** Whether the number is valid, optionally demanding the check character. */
    public boolean isValid(String number, boolean validateCheckDigits) {
        try {
            return validate(number, validateCheckDigits) != null;
        } catch (ValidationException e) {
            return false;
        }
    }

    @Override
    public String format(String number) {
        String n = validate(number);
        int split = n.length() == 12 ? 3 : 4;
        return n.length() <= 10
                ? n.substring(0, split) + " " + n.substring(split)
                : n.substring(0, split) + " " + n.substring(split, split + 6)
                        + " " + n.substring(split + 6);
    }
}
