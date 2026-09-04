package io.github.jefersonsantos06.stdnum.eu;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidComponentException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.Message;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Mask;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.List;

/**
 * CPR (personnummer), the Danish citizen number: ten digits in the form
 * DDMMYY-SSSS, where the first digit of the sequence encodes the century.
 *
 * <p>The number used to carry a mod 11 checksum, abandoned in 2007 when the
 * sequence numbers ran out, so validation checks the embedded birth date
 * instead: it must be a real date that is not in the future.</p>
 */
public final class DkCpr implements StdNum {

    public static final DkCpr INSTANCE = new DkCpr();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("dk.cpr", "CPR")
                    .country("DK")
                    .title("CPR-nummer (personnummer)")
                    .description("Danish citizen number: 10 digits encoding a birth date and"
                            + " a century-carrying sequence. The former checksum was"
                            + " abandoned in 2007.")
                    .tags(Tag.PERSON)
                    .build();

    private static final Mask MASK = Mask.of("######-####");

    private DkCpr() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " -");
    }

    /** The birth date encoded in the number. */
    public static LocalDate getBirthDate(String number) {
        String n = Strings.requireDigits(INSTANCE.compact(number), 10);
        int day = Integer.parseInt(n.substring(0, 2));
        int month = Integer.parseInt(n.substring(2, 4));
        int year = Integer.parseInt(n.substring(4, 6));
        char century = n.charAt(6);
        if ("5678".indexOf(century) >= 0 && year >= 58) {
            year += 1800;
        } else if ("0123".indexOf(century) >= 0 || ("49".indexOf(century) >= 0 && year >= 37)) {
            year += 1900;
        } else {
            year += 2000;
        }
        try {
            return LocalDate.of(year, month, day);
        } catch (DateTimeException e) {
            throw new InvalidComponentException(Message.of(DkCpr.class, "cpr.birth-date",
                    "The number does not contain valid birth date information."));
        }
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (!Strings.isDigits(n)) {
            throw new InvalidFormatException();
        }
        if (n.length() != 10) {
            throw new InvalidLengthException();
        }
        if (getBirthDate(n).isAfter(LocalDate.now())) {
            throw new InvalidComponentException(Message.of(DkCpr.class, "cpr.birth-date.future",
                    "The birth date is valid, but this person has not been born yet."));
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

    /**
     * The legacy mod 11 checksum, which valid numbers used to fold to zero.
     *
     * <p>It was abandoned in 2007 when the sequence numbers ran out, so
     * {@link #validate(String)} deliberately does not apply it. It remains
     * available for numbers known to predate the change.</p>
     */
    public static int checksum(String number) {
        String n = Strings.requireDigits(INSTANCE.compact(number), 10);
        int[] weights = {4, 3, 2, 7, 6, 5, 4, 3, 2, 1};
        int sum = 0;
        for (int i = 0; i < weights.length; i++) {
            sum += weights[i] * (n.charAt(i) - '0');
        }
        return sum % 11;
    }

}
