package io.github.jefersonsantos06.stdnum.africa;

import io.github.jefersonsantos06.stdnum.algo.Luhn;
import io.github.jefersonsantos06.stdnum.spi.Dates;
import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidComponentException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.Message;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Mask;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.time.LocalDate;
import java.util.List;

/**
 * ID number (South African Identity Document number): thirteen digits
 * encoding a birth date, a gender-carrying sequence, a citizenship digit
 * and a Luhn check digit.
 *
 * <p>The year has only two digits, so the century is inferred from the
 * current one and may be wrong for people over a hundred years old.</p>
 */
public final class ZaIdnr implements StdNum {

    public static final ZaIdnr INSTANCE = new ZaIdnr();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("za.idnr", "ID number")
                    .country("ZA")
                    .title("South African Identity Document number")
                    .description("South African ID number: 13 digits encoding birth date,"
                            + " gender and citizenship, with a Luhn check digit.")
                    .tags(Tag.PERSON)
                    .build();

    private static final Mask MASK = Mask.of("###### #### ## #");

    private ZaIdnr() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " ");
    }

    /** The birth date encoded in the number; the century is inferred. */
    public static LocalDate getBirthDate(String number) {
        String n = Strings.requireDigits(INSTANCE.compact(number), 13);
        int century = LocalDate.now().getYear() / 100 * 100;
        int year = Integer.parseInt(n.substring(0, 2)) + century;
        int month = Integer.parseInt(n.substring(2, 4));
        int day = Integer.parseInt(n.substring(4, 6));
        LocalDate date = Dates.birthDate(year, month, day);
        return date.isAfter(LocalDate.now()) ? date.minusYears(100) : date;
    }

    /** The gender encoded in the number: {@code 'M'} or {@code 'F'}. */
    public static char getGender(String number) {
        return INSTANCE.validate(number).charAt(6) < '5' ? 'F' : 'M';
    }

    /** Either {@code "citizen"} or {@code "resident"}. */
    public static String getCitizenship(String number) {
        String n = INSTANCE.compact(number);
        if (n.length() != 13) {
            throw new InvalidLengthException();
        }
        return switch (n.charAt(10)) {
            case '0' -> "citizen";
            case '1' -> "resident";
            default -> throw new InvalidComponentException(Message.of(ZaIdnr.class, "idnr.citizenship",
                    "Unknown citizenship digit."));
        };
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (!Strings.isDigits(n)) {
            throw new InvalidFormatException();
        }
        if (n.length() != 13) {
            throw new InvalidLengthException();
        }
        getBirthDate(n);
        getCitizenship(n);
        Luhn.validate(n);
        return n;
    }

    @Override
    public String format(String number) {
        return MASK.fill(validate(number));
    }

    @Override
    public List<Mask> masks() {
        return List.of(MASK);
    }
}
