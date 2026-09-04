package io.github.jefersonsantos06.stdnum.eu;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidComponentException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.Message;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Mask;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Set;

/**
 * Rijksregisternummer, the Belgian national number: the date of birth, a
 * serial number that also gives the sex, and two check digits.
 *
 * <p>The century is not written down. It is recovered from the checksum,
 * which is taken over the number as it stands for the 1900s and over the
 * number prefixed with a 2 for the 2000s. Because a birth year of, say, 45
 * cannot yet mean 2045, the second reading is only tried once that year has
 * passed — so a number can become valid with time, never invalid.</p>
 */
public final class BeNn implements StdNum {

    public static final BeNn INSTANCE = new BeNn();

    /** Serial numbers issued to people whose date of birth was unknown. */
    private static final Set<String> UNKNOWN_BIRTH_DATE = Set.of("000001", "002001", "004001");

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("be.nn", "Rijksregisternummer")
                    .country("BE")
                    .title("Belgisch Rijksregisternummer")
                    .description("Belgian national number: 11 digits giving the date of birth,"
                            + " the sex and two mod 97 check digits.")
                    .tags(Tag.PERSON)
                    .references("https://nl.wikipedia.org/wiki/Rijksregisternummer")
                    .build();

    static final Mask MASK = Mask.of("99.99.99-999.99");

    private BeNn() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " -.");
    }

    /**
     * The century the number was issued in, established by whichever reading
     * the check digits agree with.
     *
     * @throws InvalidChecksumException if neither reading agrees
     */
    public static int getCentury(String number) {
        String n = Strings.requireDigits(INSTANCE.compact(number), 11);
        int check = Integer.parseInt(n.substring(9));
        if (97 - Long.parseLong(n.substring(0, 9)) % 97 == check) {
            return 1900;
        }
        int year = Integer.parseInt(n.substring(0, 2));
        if (year + 2000 <= LocalDate.now().getYear()
                && 97 - Long.parseLong("2" + n.substring(0, 9)) % 97 == check) {
            return 2000;
        }
        throw new InvalidChecksumException();
    }

    /**
     * The birth date, part by part, with a zero standing for a part the
     * number does not record. A date is sometimes only partly known: the day
     * may be missing, or the month and day together, and a few serial numbers
     * stand for a date that was not known at all.
     *
     * @throws InvalidComponentException if the month is one no year has
     */
    private static int[] birthDateParts(String number) {
        String n = INSTANCE.compact(number);
        int century = getCentury(n);
        if (UNKNOWN_BIRTH_DATE.contains(n.substring(0, 6))) {
            return new int[] {0, 0, 0};
        }
        int year = Integer.parseInt(n.substring(0, 2)) + century;
        // the twenties and forties are the bis numbers, counting from the
        // same months; and a zero month is one that was never recorded, or a
        // day counter that ran out
        int month = Integer.parseInt(n.substring(2, 4)) % 20;
        int day = Integer.parseInt(n.substring(4, 6));
        if (month == 0) {
            return new int[] {year, 0, 0};
        }
        if (month > 12) {
            throw new InvalidComponentException(Message.of(BeNn.class, "nn.month",
                    "The month must be in 1..12."));
        }
        if (day == 0 || day > YearMonth.of(year, month).lengthOfMonth()) {
            return new int[] {year, month, 0};
        }
        return new int[] {year, month, day};
    }

    /**
     * The year of birth, or {@code null} when the number records none.
     *
     * @throws InvalidComponentException if the month is one no year has
     */
    public static Integer getBirthYear(String number) {
        int year = birthDateParts(number)[0];
        return year == 0 ? null : year;
    }

    /**
     * The month of birth, or {@code null} when the number records none. A
     * number can give the year and withhold the month.
     *
     * @throws InvalidComponentException if the month is one no year has
     */
    public static Integer getBirthMonth(String number) {
        int month = birthDateParts(number)[1];
        return month == 0 ? null : month;
    }

    /**
     * The birth date encoded in the number, or {@code null} when the number
     * does not record all three parts of it. What it does record is still
     * available from {@link #getBirthYear} and {@link #getBirthMonth}.
     */
    public static LocalDate getBirthDate(String number) {
        int[] parts = birthDateParts(number);
        return parts[2] == 0 ? null : LocalDate.of(parts[0], parts[1], parts[2]);
    }

    /** The sex recorded in the number, {@code 'M'} or {@code 'F'}. */
    public static char getGender(String number) {
        String n = Strings.requireDigits(INSTANCE.compact(number), 11);
        return Integer.parseInt(n.substring(6, 9)) % 2 == 1 ? 'M' : 'F';
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (!Strings.isDigits(n) || n.chars().allMatch(c -> c == '0')) {
            throw new InvalidFormatException();
        }
        if (n.length() != 11) {
            throw new InvalidLengthException();
        }
        getBirthDate(n);
        int month = Integer.parseInt(n.substring(2, 4));
        if (month > 12) {
            throw new InvalidComponentException(Message.of(BeNn.class, "nn.month",
                    "The month must be in 1..12."));
        }
        return n;
    }

    @Override
    public String format(String number) {
        return group(validate(number));
    }

    @Override
    public List<Mask> masks() {
        return List.of(MASK);
    }

    /** The shared presentation of an eleven-digit compact number. */
    static String group(String n) {
        return MASK.fill(n);
    }
}
