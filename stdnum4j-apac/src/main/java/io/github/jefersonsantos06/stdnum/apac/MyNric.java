package io.github.jefersonsantos06.stdnum.apac;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidComponentException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.Message;
import io.github.jefersonsantos06.stdnum.spi.Reasons;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Mask;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.List;

/**
 * NRIC No. (Malaysian National Registration Identity Card Number): twelve
 * digits — a six-digit birth date, two for the place of birth and four
 * more, whose last digit is odd for males and even for females. There is
 * no check digit.
 *
 * <p>The birth-place code is only range-checked: codes 00, 17 to 20, 69,
 * 70, 73, 80, 81, 94 to 97 and 99 are not assigned. The year has two
 * digits, so the century is inferred and may be wrong.</p>
 */
public final class MyNric implements StdNum {

    public static final MyNric INSTANCE = new MyNric();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("my.nric", "NRIC No.")
                    .country("MY")
                    .title("National Registration Identity Card Number")
                    .description("Malaysian identity card number: 12 digits encoding birth"
                            + " date, birth place and gender, with no check digit.")
                    .tags(Tag.PERSON)
                    .build();

    private static final Mask MASK = Mask.of("######-##-####");

    private MyNric() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " -*");
    }

    /** The birth date encoded in the number; the century is inferred. */
    public static LocalDate getBirthDate(String number) {
        String n = Strings.requireDigits(INSTANCE.compact(number), 12);
        int year = Integer.parseInt(n.substring(0, 2));
        int month = Integer.parseInt(n.substring(2, 4));
        int day = Integer.parseInt(n.substring(4, 6));
        try {
            LocalDate date = LocalDate.of(year + 1900, month, day);
            // a date that would be in the future belongs to the 2000s
            return date.isAfter(LocalDate.now()) ? date.plusYears(100) : date;
        } catch (DateTimeException e) {
            try {
                return LocalDate.of(year + 2000, month, day);
            } catch (DateTimeException e2) {
                throw new InvalidComponentException(Reasons.birthDate());
            }
        }
    }

    /** The gender encoded in the number: {@code 'M'} or {@code 'F'}. */
    public static char getGender(String number) {
        String n = INSTANCE.validate(number);
        return (n.charAt(11) - '0') % 2 == 0 ? 'F' : 'M';
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (n.length() != 12) {
            throw new InvalidLengthException();
        }
        if (!Strings.isDigits(n)) {
            throw new InvalidFormatException();
        }
        getBirthDate(n);
        int place = Integer.parseInt(n.substring(6, 8));
        boolean unassigned = place == 0 || (place >= 17 && place <= 20)
                || place == 69 || place == 70 || place == 73
                || place == 80 || place == 81
                || (place >= 94 && place <= 97) || place == 99;
        if (unassigned) {
            throw new InvalidComponentException(Message.of(MyNric.class, "birth-place.unknown",
                    "Unknown place of birth code."));
        }
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
