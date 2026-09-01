package io.github.jefersonsantos06.stdnum.eu;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidComponentException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;

import java.time.LocalDate;

/**
 * The Belgian social security number, which is either a
 * {@link BeNn national number} or, for people outside the national register,
 * a {@link BeBis BIS number}. The two differ only in the month of birth, so
 * the number itself says which it is.
 */
public final class BeSsn implements StdNum {

    public static final BeSsn INSTANCE = new BeSsn();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("be.ssn", "INSZ")
                    .country("BE")
                    .title("Belgisch identificatienummer van de sociale zekerheid")
                    .description("Belgian social security number: either the national number"
                            + " or the BIS number of someone outside the national register.")
                    .tags(Tag.PERSON, Tag.HEALTH)
                    .references("https://www.socialsecurity.be/")
                    .build();

    private BeSsn() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return BeNn.INSTANCE.compact(number);
    }

    /**
     * Which of the two kinds of number this is, or {@code null} if it is
     * neither.
     */
    public static StdNum kindOf(String number) {
        if (BeNn.INSTANCE.isValid(number)) {
            return BeNn.INSTANCE;
        }
        return BeBis.INSTANCE.isValid(number) ? BeBis.INSTANCE : null;
    }

    /** The birth date encoded in the number, or {@code null} when it carries none. */
    public static LocalDate getBirthDate(String number) {
        return BeNn.getBirthDate(number);
    }

    /**
     * The sex recorded in the number, or {@code null} for a BIS number issued
     * before the sex was established.
     */
    public static Character getGender(String number) {
        if (BeNn.INSTANCE.isValid(number)) {
            return BeNn.getGender(number);
        }
        return BeBis.INSTANCE.isValid(number) ? BeBis.getGender(number) : null;
    }

    @Override
    public String validate(String number) {
        try {
            return BeBis.INSTANCE.validate(number);
        } catch (InvalidComponentException e) {
            // the month is outside the BIS ranges, so read it as a national number
            return BeNn.INSTANCE.validate(number);
        }
    }

    @Override
    public String format(String number) {
        return BeNn.group(validate(number));
    }
}
