package io.github.jefersonsantos06.stdnum.eu;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidComponentException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.Message;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Kennitala, the Icelandic identity code for people and organisations: a
 * six-digit date, two random digits, a check digit and a century marker
 * ({@code 9} for the 1900s, {@code 0} from 2000 on). Organisations use
 * their registration date with 4 added to the first digit.
 */
public final class IsKennitala implements StdNum {

    public static final IsKennitala INSTANCE = new IsKennitala();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("is.kennitala", "Kennitala")
                    .country("IS")
                    .title("Kennitala")
                    .description("Icelandic personal and organisation identity code:"
                            + " 10 digits with an embedded date and a mod 11 check digit.")
                    .tags(Tag.PERSON, Tag.COMPANY)
                    .build();

    private static final Pattern STRUCTURE =
            Pattern.compile("([01234567]\\d)([01]\\d)(\\d\\d)(\\d\\d)(\\d)([09])");

    private static final int[] WEIGHTS = {3, 2, 7, 6, 5, 4, 3, 2, 1, 0};

    private IsKennitala() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, "-").toUpperCase(Locale.ROOT);
    }

    /** The birth (or registration) date encoded in the number. */
    public static LocalDate getBirthDate(String number) {
        String n = INSTANCE.compact(number);
        if (!STRUCTURE.matcher(n).matches()) {
            throw new InvalidFormatException();
        }
        int day = Integer.parseInt(n.substring(0, 2));
        int month = Integer.parseInt(n.substring(2, 4));
        int year = Integer.parseInt(n.substring(4, 6)) + (n.charAt(9) == '9' ? 1900 : 2000);
        // organisations add 40 to the day of their registration date
        if (day > 40) {
            day -= 40;
        }
        try {
            return LocalDate.of(year, month, day);
        } catch (DateTimeException e) {
            throw new InvalidComponentException(Message.of(IsKennitala.class, "kennitala.date",
                    "The number does not contain a valid date."));
        }
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (!STRUCTURE.matcher(n).matches()) {
            throw new InvalidFormatException();
        }
        getBirthDate(n);
        int sum = 0;
        for (int i = 0; i < WEIGHTS.length; i++) {
            sum += WEIGHTS[i] * (n.charAt(i) - '0');
        }
        if (sum % 11 != 0) {
            throw new InvalidChecksumException();
        }
        return n;
    }

    @Override
    public String format(String number) {
        String n = validate(number);
        return n.substring(0, 6) + "-" + n.substring(6);
    }
}
