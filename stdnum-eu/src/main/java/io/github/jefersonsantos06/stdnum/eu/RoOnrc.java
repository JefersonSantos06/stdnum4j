package io.github.jefersonsantos06.stdnum.eu;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidComponentException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.Message;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.time.LocalDate;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The number of the Romanian trade register: a letter for the kind of entity,
 * the county, a serial number and the year of registration.
 *
 * <p>Two forms are in circulation. The old {@code J40/1234/2000} is being
 * phased out in favour of a fourteen-digit form that puts the year first and
 * closes with a check digit; both are accepted.</p>
 */
public final class RoOnrc implements StdNum {

    public static final RoOnrc INSTANCE = new RoOnrc();

    private static final Pattern SEPARATORS = Pattern.compile("[ /\\\\-]+");
    private static final Pattern OLD_FULL_DATE =
            Pattern.compile("([A-Z][0-9]+/[0-9]+/)[0-9]{2}\\.[0-9]{2}\\.([0-9]{4})");
    private static final Pattern OLD = Pattern.compile("[A-Z][0-9]+/[0-9]+/[0-9]+");

    /** The last year the old form was issued in. */
    private static final int LAST_OLD_YEAR = 2024;
    private static final int FIRST_YEAR = 1990;

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("ro.onrc", "ONRC")
                    .country("RO")
                    .title("Numarul de ordine in registrul comertului")
                    .description("Romanian trade register number: an entity letter, a county,"
                            + " a serial number and the year of registration.")
                    .tags(Tag.COMPANY)
                    .references("https://www.onrc.ro/")
                    .build();

    private RoOnrc() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    /** Whether {@code county} is one of the 41 counties or one of Bucharest's two codes. */
    private static boolean isCounty(int county) {
        return (county >= 1 && county <= 40) || county == 51 || county == 52;
    }

    @Override
    public String compact(String number) {
        String n = SEPARATORS.matcher(Strings.compact(number, "").toUpperCase(Locale.ROOT))
                .replaceAll("/");
        // the entity letter is sometimes set off from the county, and the
        // county is sometimes written without its leading zero
        if (n.length() > 1 && n.charAt(1) == '/') {
            n = n.charAt(0) + n.substring(2);
        }
        if (n.length() > 2 && n.charAt(2) == '/') {
            n = n.charAt(0) + "0" + n.substring(1);
        }
        Matcher m = OLD_FULL_DATE.matcher(n);
        return m.matches() ? m.group(1) + m.group(2) : n;
    }

    /** The check digit of a new-form number, from its first thirteen characters. */
    public static char calcCheckDigit(String number) {
        String n = INSTANCE.compact(number);
        if (n.isEmpty()) {
            throw new InvalidFormatException();
        }
        // the entity letter counts as its character code modulo ten
        int sum = n.charAt(0) % 10;
        for (int i = 1; i < n.length() - 1; i++) {
            if (n.charAt(i) < '0' || n.charAt(i) > '9') {
                throw new InvalidFormatException();
            }
            sum += n.charAt(i) - '0';
        }
        return (char) ('0' + sum % 10);
    }

    private static void validateOld(String n) {
        if (!OLD.matcher(n).matches()) {
            throw new InvalidFormatException();
        }
        String[] parts = n.substring(1).split("/", -1);
        if (parts[1].length() > 5 || parts[2].length() != 4) {
            throw new InvalidLengthException();
        }
        if (parts[0].length() > 2 || !isCounty(Integer.parseInt(parts[0]))) {
            throw new InvalidComponentException(Message.of(RoOnrc.class, "onrc.county",
                    "Not the code of a county."));
        }
        int year = Integer.parseInt(parts[2]);
        if (year < FIRST_YEAR || year > LAST_OLD_YEAR) {
            throw new InvalidComponentException(Message.of(RoOnrc.class, "onrc.year",
                    "The year is outside the range of this form."));
        }
    }

    private static void validateNew(String n) {
        if (!Strings.isDigits(n.substring(1))) {
            throw new InvalidFormatException();
        }
        if (n.length() != 14) {
            throw new InvalidLengthException();
        }
        int year = Integer.parseInt(n.substring(1, 5));
        if (year < FIRST_YEAR || year > LocalDate.now().getYear()) {
            throw new InvalidComponentException(Message.of(RoOnrc.class, "onrc.year",
                    "The year is outside the range of this form."));
        }
        int county = Integer.parseInt(n.substring(11, 13));
        // the county was dropped when the register went national during 2024
        boolean countyAllowed = year < LAST_OLD_YEAR ? isCounty(county)
                : year == LAST_OLD_YEAR ? isCounty(county) || county == 0
                : county == 0;
        if (!countyAllowed) {
            throw new InvalidComponentException(Message.of(RoOnrc.class, "onrc.county",
                    "Not the code of a county."));
        }
        if (n.charAt(13) != calcCheckDigit(n)) {
            throw new InvalidChecksumException();
        }
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (n.isEmpty() || "JFC".indexOf(n.charAt(0)) < 0) {
            throw new InvalidComponentException(Message.of(RoOnrc.class, "onrc.entity-kind",
                    "The number names a legal entity (J), a sole trader (F) or a cooperative (C)."));
        }
        if (n.indexOf('/') >= 0) {
            validateOld(n);
        } else {
            validateNew(n);
        }
        return n;
    }
}
