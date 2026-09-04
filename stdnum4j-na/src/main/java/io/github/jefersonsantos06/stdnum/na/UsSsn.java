package io.github.jefersonsantos06.stdnum.na;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidComponentException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.Message;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Mask;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * SSN (U.S. Social Security Number): a three-digit area, a two-digit group
 * and a four-digit serial. There is no check digit, so validation rejects
 * the ranges that are never issued (area 000, 666 and 900-999; group 00;
 * serial 0000) plus the numbers famously published in advertising.
 */
public final class UsSsn implements StdNum {

    public static final UsSsn INSTANCE = new UsSsn();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("us.ssn", "SSN")
                    .country("US")
                    .title("Social Security Number")
                    .description("US Social Security Number: 9 digits with no check digit;"
                            + " never-issued ranges and known invalid numbers are rejected.")
                    .tags(Tag.PERSON, Tag.TAX)
                    .build();

    private static final Mask MASK = Mask.of("###-##-####");

    /** Numbers that were published in advertising and are permanently void. */
    private static final Set<String> BLACKLIST =
            Set.of("078051120", "457555462", "219099999");

    /** The separators, if written, sit after the area and the group. */
    private static final Pattern STRUCTURE = Pattern.compile("[0-9]{3}-?[0-9]{2}-?[0-9]{4}");

    private UsSsn() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, "-");
    }

    @Override
    public String validate(String number) {
        if (!STRUCTURE.matcher(Strings.compact(number, "")).matches()) {
            throw new InvalidFormatException();
        }
        String n = compact(number);
        if (!Strings.isDigits(n) || n.length() != 9) {
            throw new InvalidFormatException();
        }
        String area = n.substring(0, 3);
        String group = n.substring(3, 5);
        String serial = n.substring(5);
        if (area.equals("000") || area.equals("666") || area.charAt(0) == '9'
                || group.equals("00") || serial.equals("0000")) {
            throw new InvalidComponentException(Message.of(UsSsn.class, "ssn.range",
                    "This range of SSNs is never issued."));
        }
        if (BLACKLIST.contains(n)) {
            throw new InvalidComponentException(Message.of(UsSsn.class, "ssn.blacklist",
                    "This SSN is permanently void."));
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
