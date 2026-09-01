package io.github.jefersonsantos06.stdnum.eu;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidComponentException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Steuernummer, the German tax number a Land issues to a taxpayer. It carries
 * no check digit: what can be checked is its shape, which differs from Land
 * to Land.
 *
 * <p>Every number exists in two forms — the regional one the Land uses and
 * the thirteen-digit country-wide one that opens with the Land's code — and
 * this type accepts and converts between both.</p>
 */
public final class DeStnr implements StdNum {

    public static final DeStnr INSTANCE = new DeStnr();

    /** A run of one letter in a template, standing for that many digits. */
    private static final Pattern RUN = Pattern.compile("([FBUP])\\1*");

    /**
     * The two forms per Land, written as templates: F is the tax office, B
     * the district, U the taxpayer and P the trailing digit, and anything
     * else is literal.
     */
    private static final Map<String, Format[]> FORMATS = new LinkedHashMap<>();

    static {
        put("Baden-Wurttemberg", "FFBBBUUUUP", "28FF0BBBUUUUP");
        put("Bayern", "FFFBBBUUUUP", "9FFF0BBBUUUUP");
        put("Berlin", "FFBBBUUUUP", "11FF0BBBUUUUP");
        put("Brandenburg", "0FFBBBUUUUP", "30FF0BBBUUUUP");
        put("Bremen", "FFBBBUUUUP", "24FF0BBBUUUUP");
        put("Hamburg", "FFBBBUUUUP", "22FF0BBBUUUUP");
        put("Hessen", "0FFBBBUUUUP", "26FF0BBBUUUUP");
        put("Mecklenburg-Vorpommern", "0FFBBBUUUUP", "40FF0BBBUUUUP");
        put("Niedersachsen", "FFBBBUUUUP", "23FF0BBBUUUUP");
        put("Nordrhein-Westfalen", "FFFBBBBUUUP", "5FFF0BBBBUUUP");
        put("Rheinland-Pfalz", "FFBBBUUUUP", "27FF0BBBUUUUP");
        put("Saarland", "0FFBBBUUUUP", "10FF0BBBUUUUP");
        put("Sachsen", "2FFBBBUUUUP", "32FF0BBBUUUUP");
        put("Sachsen-Anhalt", "1FFBBBUUUUP", "31FF0BBBUUUUP");
        put("Schleswig-Holstein", "FFBBBUUUUP", "21FF0BBBUUUUP");
        put("Thuringen", "1FFBBBUUUUP", "41FF0BBBUUUUP");
    }

    private static void put(String region, String regional, String country) {
        FORMATS.put(region, new Format[] {new Format(regional), new Format(country)});
    }

    /** The names of the Lander, as this type spells them. */
    public static final List<String> REGIONS = List.copyOf(FORMATS.keySet());

    /** One of the two written forms of a number. */
    private static final class Format {

        private final String template;
        private final Pattern pattern;

        Format(String template) {
            this.template = template;
            StringBuilder sb = new StringBuilder();
            Matcher m = RUN.matcher(template);
            int last = 0;
            while (m.find()) {
                sb.append(Pattern.quote(template.substring(last, m.start())));
                sb.append("([0-9]{").append(m.end() - m.start()).append("})");
                last = m.end();
            }
            sb.append(Pattern.quote(template.substring(last)));
            this.pattern = Pattern.compile(sb.toString());
        }

        Matcher matcher(String number) {
            return pattern.matcher(number);
        }

        /** The template with each run of F, B, U and P filled in, in order. */
        String replace(Matcher parts) {
            StringBuilder sb = new StringBuilder();
            Matcher m = RUN.matcher(template);
            int last = 0;
            int group = 1;
            while (m.find()) {
                sb.append(template, last, m.start()).append(parts.group(group++));
                last = m.end();
            }
            return sb.append(template.substring(last)).toString();
        }
    }

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("de.stnr", "Steuernummer")
                    .country("DE")
                    .title("Deutsche Steuernummer")
                    .description("German tax number: 10, 11 or 13 digits whose layout depends"
                            + " on the Land that issued it.")
                    .tags(Tag.PERSON, Tag.COMPANY, Tag.TAX)
                    .references("https://de.wikipedia.org/wiki/Steuernummer")
                    .build();

    private DeStnr() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " -./,");
    }

    /**
     * A region name reduced to the letters that identify it, so that spelling
     * and punctuation do not matter.
     */
    private static String key(String region) {
        StringBuilder sb = new StringBuilder();
        for (char c : region.toLowerCase(Locale.ROOT).toCharArray()) {
            // u is left out so that Wurttemberg and Thuringen match however
            // their umlaut was written
            if (c >= 'a' && c <= 'z' && c != 'u') {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /** The formats to try: one region's, or every region's. */
    private static List<Format[]> formatsFor(String region) {
        if (region == null || region.isEmpty()) {
            return List.copyOf(FORMATS.values());
        }
        String wanted = key(region);
        List<Format[]> found = FORMATS.entrySet().stream()
                .filter(e -> key(e.getKey()).equals(wanted))
                .map(Map.Entry::getValue)
                .toList();
        if (found.isEmpty()) {
            throw new InvalidComponentException(region + " is not a German Land.");
        }
        return found;
    }

    /** Every Land whose layout the number could belong to. */
    public static List<String> guessRegions(String number) {
        String n = INSTANCE.compact(number);
        return FORMATS.entrySet().stream()
                .filter(e -> e.getValue()[0].matcher(n).matches()
                        || e.getValue()[1].matcher(n).matches())
                .map(Map.Entry::getKey)
                .sorted()
                .toList();
    }

    /** The regional form of a country-wide number. */
    public static String toRegionalNumber(String number) {
        String n = INSTANCE.compact(number);
        for (Format[] forms : FORMATS.values()) {
            Matcher m = forms[1].matcher(n);
            if (m.matches()) {
                return forms[0].replace(m);
            }
        }
        throw new InvalidFormatException();
    }

    /** The country-wide form of a regional number. */
    public static String toCountryNumber(String number) {
        return toCountryNumber(number, null);
    }

    /**
     * The country-wide form of a regional number.
     *
     * @param region the Land it was issued by; without it a number whose
     *               layout more than one Land uses cannot be converted
     */
    public static String toCountryNumber(String number, String region) {
        String n = INSTANCE.compact(number);
        String converted = null;
        for (Format[] forms : formatsFor(region)) {
            Matcher m = forms[0].matcher(n);
            if (m.matches()) {
                if (converted != null) {
                    throw new InvalidComponentException(
                            "More than one Land uses this layout: name the one that issued it.");
                }
                converted = forms[1].replace(m);
            }
        }
        if (converted == null) {
            throw new InvalidFormatException();
        }
        return converted;
    }

    @Override
    public String validate(String number) {
        return validate(number, null);
    }

    /**
     * Validates the number, optionally checking that it is one the given Land
     * could have issued.
     */
    public String validate(String number, String region) {
        String n = compact(number);
        if (!Strings.isDigits(n)) {
            throw new InvalidFormatException();
        }
        if (n.length() != 10 && n.length() != 11 && n.length() != 13) {
            throw new InvalidLengthException();
        }
        boolean matches = formatsFor(region).stream()
                .anyMatch(f -> f[0].matcher(n).matches() || f[1].matcher(n).matches());
        if (!matches) {
            throw new InvalidFormatException();
        }
        return n;
    }
}
