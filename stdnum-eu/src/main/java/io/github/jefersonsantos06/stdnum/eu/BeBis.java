package io.github.jefersonsantos06.stdnum.eu;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidComponentException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.time.LocalDate;

/**
 * The Belgian BIS number, issued to people who are not in the national
 * register but do have dealings with Belgian social security.
 *
 * <p>It is built exactly like the {@link BeNn} except that 20 is added to the
 * month of birth, or 40 once the sex has been established — which is why the
 * sex can only be read off a number in the second range.</p>
 */
public final class BeBis implements StdNum {

    public static final BeBis INSTANCE = new BeBis();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("be.bis", "BIS-nummer")
                    .country("BE")
                    .title("Belgisch BIS-nummer")
                    .description("Belgian social security number for people outside the"
                            + " national register: a national number with 20 or 40 added"
                            + " to the month of birth.")
                    .tags(Tag.PERSON)
                    .references("https://sma-help.bosa.belgium.be/en/faq/"
                            + "what-does-bis-number-mean")
                    .build();

    private BeBis() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return BeNn.INSTANCE.compact(number);
    }

    /** The birth date encoded in the number, or {@code null} when it carries none. */
    public static LocalDate getBirthDate(String number) {
        return BeNn.getBirthDate(number);
    }

    /**
     * The sex recorded in the number, or {@code null} for a number in the
     * 20..32 range, which was issued before the sex was established.
     */
    public static Character getGender(String number) {
        String n = INSTANCE.compact(number);
        if (!Strings.isDigits(n) || n.length() != 11) {
            throw new InvalidFormatException();
        }
        return Integer.parseInt(n.substring(2, 4)) >= 40 ? BeNn.getGender(n) : null;
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
        BeNn.getBirthDate(n);
        int month = Integer.parseInt(n.substring(2, 4));
        if ((month < 20 || month > 32) && (month < 40 || month > 52)) {
            throw new InvalidComponentException("The month must be in 20..32 or 40..52.");
        }
        return n;
    }

    @Override
    public String format(String number) {
        return BeNn.group(validate(number));
    }
}
