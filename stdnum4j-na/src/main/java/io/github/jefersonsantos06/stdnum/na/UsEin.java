package io.github.jefersonsantos06.stdnum.na;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidComponentException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.Message;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Mask;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * EIN (U.S. Employer Identification Number), also called FEIN: a two-digit
 * campus prefix and a seven-digit serial. There is no check digit, so the
 * prefix is checked against the campuses the IRS actually assigns.
 */
public final class UsEin implements StdNum {

    public static final UsEin INSTANCE = new UsEin();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("us.ein", "EIN")
                    .country("US")
                    .title("Employer Identification Number")
                    .description("US business tax identifier: 9 digits with no check digit.")
                    .tags(Tag.COMPANY, Tag.TAX)
                    .build();

    private static final Mask MASK = Mask.of("99-9999999");

    /** A separator, if written at all, sits after the campus prefix. */
    private static final Pattern STRUCTURE = Pattern.compile("[0-9]{2}-?[0-9]{7}");

    /**
     * The campus or office that issues each prefix, as published by the IRS.
     * Prefixes outside this set are not assigned.
     */
    private static final Map<String, String> CAMPUSES = buildCampuses();

    private static Map<String, String> buildCampuses() {
        Map<String, String> m = new HashMap<>();
        put(m, "Brookhaven", "01", "02", "03", "04", "05", "06", "11", "13", "14", "16",
                "21", "22", "23", "25", "34", "51", "52", "54", "55", "56", "57", "58", "59", "65");
        put(m, "Andover", "10", "12");
        put(m, "Fresno", "15", "24");
        put(m, "Internet", "20", "26", "27", "45", "46", "47", "81", "82", "83", "84");
        put(m, "Cincinnati", "30", "32", "35", "36", "37", "38", "61");
        put(m, "Small Business Administration (SBA)", "31");
        put(m, "Philadelphia", "33", "39", "41", "42", "43", "46", "48", "62", "63", "64",
                "66", "68", "71", "72", "73", "74", "75", "76", "77", "85", "86", "87", "88",
                "91", "92", "93", "98", "99");
        put(m, "Kansas City", "40", "44");
        put(m, "Austin", "50", "53");
        put(m, "Atlanta", "60", "67");
        put(m, "Ogden", "80", "90");
        put(m, "Memphis", "94", "95");
        return Map.copyOf(m);
    }

    private static void put(Map<String, String> m, String campus, String... prefixes) {
        for (String prefix : prefixes) {
            m.putIfAbsent(prefix, campus);
        }
    }

    private UsEin() {
    }

    /** The IRS campus or office that issued the number. */
    public static Optional<String> getCampus(String number) {
        return Optional.ofNullable(CAMPUSES.get(INSTANCE.validate(number).substring(0, 2)));
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
        // the separator, when present, must sit in its documented position
        if (!STRUCTURE.matcher(Strings.compact(number, "")).matches()) {
            throw new InvalidFormatException();
        }
        String n = compact(number);
        if (!CAMPUSES.containsKey(n.substring(0, 2))) {
            throw new InvalidComponentException(Message.of(UsEin.class, "ein.prefix",
                    "This EIN prefix is not assigned."));
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
