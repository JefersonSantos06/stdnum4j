package io.github.jefersonsantos06.stdnum.spi;

import java.time.DateTimeException;
import java.time.LocalDate;

/**
 * The date a number carries, or the rejection saying it carries none.
 *
 * <p>Two dozen personal identifiers open with a date of birth, and each has
 * its own rule for which century two digits mean — Poland encodes it in the
 * month, Slovenia in a three-digit year, South Africa slides the window.
 * Those rules stay where they are. What every one of them ends with is this:
 * hand three numbers to {@link LocalDate}, and turn a date that does not
 * exist into an {@link InvalidComponentException} rather than letting a
 * {@link DateTimeException} escape a validator.</p>
 *
 * <p>It lives beside {@link Reasons} because it is the same kind of thing:
 * the shared vocabulary for refusing a number, written once so that every
 * type refuses in the same words and in the same language.</p>
 */
public final class Dates {

    private Dates() {
    }

    /**
     * The birth date those three numbers name.
     *
     * @throws InvalidComponentException if they name no day — 31 April, 29
     *                                   February of a common year, month 13
     */
    public static LocalDate birthDate(int year, int month, int day) {
        try {
            return LocalDate.of(year, month, day);
        } catch (DateTimeException e) {
            throw new InvalidComponentException(Reasons.birthDate());
        }
    }
}
