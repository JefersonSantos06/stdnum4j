package io.github.jefersonsantos06.stdnum.eu;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidComponentException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.Message;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Resources;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.text.Normalizer;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The German Handelsregisternummer: the court that keeps the register, the
 * register the company is entered in, and its number there — as in
 * {@code Amberg HRB 1234}.
 *
 * <p>There is no check digit. What can be checked is that the court is one
 * that keeps a register and that the register is one of the five that exist,
 * and the court is returned under the name it uses itself however it was
 * written.</p>
 */
public final class DeHandelsregisternummer implements StdNum {

    public static final DeHandelsregisternummer INSTANCE = new DeHandelsregisternummer();

    /** The registers a company can be entered in. */
    public static final List<String> REGISTRY_TYPES = List.of("HRA", "HRB", "PR", "GnR", "VR");

    /** The register each legal form belongs in. */
    private static final Map<String, String> REGISTRY_BY_COMPANY_FORM = Map.ofEntries(
            Map.entry("e.K.", "HRA"),
            Map.entry("e.V.", "VR"),
            Map.entry("Verein", "VR"),
            Map.entry("OHG", "HRA"),
            Map.entry("KG", "HRA"),
            Map.entry("KGaA", "HRB"),
            Map.entry("Vor-GmbH", "HRB"),
            Map.entry("GmbH", "HRB"),
            Map.entry("UG", "HRB"),
            Map.entry("UG i.G.", "HRB"),
            Map.entry("AG", "HRB"),
            Map.entry("e.G.", "GnR"),
            Map.entry("PartG", "PR"));

    /** Names a court is also known by, and the court they name. */
    private static final String[][] ALIASES = {
            {"Allgau", "Kempten (Allgau)"},
            {"Bad Homburg", "Bad Homburg v.d.H."},
            {"Berlin", "Berlin (Charlottenburg)"},
            {"Charlottenburg", "Berlin (Charlottenburg)"},
            {"Charlottenburg (Berlin)", "Berlin (Charlottenburg)"},
            // Koln reaches us as Kaln often enough to be worth resolving
            {"Kaln", "Koln"},
            {"Kempten", "Kempten (Allgau)"},
            {"Ludwigshafen am Rhein (Ludwigshafen)", "Ludwigshafen a.Rhein (Ludwigshafen)"},
            {"Ludwigshafen am Rhein", "Ludwigshafen a.Rhein (Ludwigshafen)"},
            {"Ludwigshafen", "Ludwigshafen a.Rhein (Ludwigshafen)"},
            {"Oldenburg", "Oldenburg (Oldenburg)"},
            {"St. Ingbert", "St. Ingbert (St Ingbert)"},
            {"St. Wendel", "St. Wendel (St Wendel)"},
            {"Weiden in der Oberpfalz", "Weiden i. d. OPf."},
            {"Weiden", "Weiden i. d. OPf."},
            {"Paderborn fruher Hoxter", "Paderborn"},
    };

    private static final String REGISTRY = String.join("|", REGISTRY_TYPES);
    private static final String NUMBER = "(?<nr>[1-9][0-9]{0,5})(\\s*(?<x>[A-ZÖ]{1,3}))?";
    private static final Pattern[] FORMATS = {
            Pattern.compile("(?<registry>" + REGISTRY + ")\\s+" + NUMBER + ",?\\s+(?<court>.*)",
                    Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE),
            Pattern.compile("(?<court>.*),?\\s+(?<registry>" + REGISTRY + ")\\s+" + NUMBER,
                    Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE),
    };

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("de.handelsregisternummer", "Handelsregisternummer")
                    .country("DE")
                    .title("Deutsche Handelsregisternummer")
                    .description("German commercial register number: the registry court, the"
                            + " register and the number the company has in it.")
                    .tags(Tag.COMPANY)
                    .references("https://www.handelsregister.de/")
                    .build();

    private DeHandelsregisternummer() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    /** The courts, keyed by their name reduced to bare letters. */
    private static Map<String, String> courts() {
        return Holder.COURTS;
    }

    /** Loaded on first use, as the court list is a resource. */
    private static final class Holder {

        private static final Map<String, String> COURTS = load();

        private static Map<String, String> load() {
            Map<String, String> courts = new LinkedHashMap<>();
            for (String court : Resources.lines(DeHandelsregisternummer.class, "de-courts.txt")) {
                courts.put(key(court), court);
            }
            for (String[] alias : ALIASES) {
                courts.put(key(alias[0]), courts.getOrDefault(key(alias[1]), alias[1]));
            }
            return Map.copyOf(courts);
        }
    }

    /**
     * A court name reduced to its bare letters, so that case, punctuation and
     * however the umlauts were written stop mattering.
     */
    private static String key(String court) {
        String folded = Normalizer.normalize(court.toLowerCase(Locale.ROOT), Normalizer.Form.NFD);
        StringBuilder sb = new StringBuilder(folded.length());
        for (int i = 0; i < folded.length(); i++) {
            char c = folded.charAt(i);
            if (c >= 'a' && c <= 'z') {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /** The court, register and number a written form breaks down into. */
    private static Matcher split(String number) {
        String n = Strings.compact(number, "");
        for (Pattern format : FORMATS) {
            Matcher m = format.matcher(n);
            if (m.matches()) {
                return m;
            }
        }
        throw new InvalidFormatException();
    }

    /** The parts joined back up, with the court spelled as {@code court}. */
    private static String join(String court, Matcher m) {
        StringBuilder sb = new StringBuilder(court).append(' ')
                .append(m.group("registry")).append(' ').append(m.group("nr"));
        if (m.group("x") != null) {
            sb.append(' ').append(m.group("x"));
        }
        return sb.toString();
    }

    /**
     * {@inheritDoc}
     *
     * <p>The court is resolved to the name it uses itself, so that a number
     * written under one of its other names compacts to the same thing.</p>
     */
    @Override
    public String compact(String number) {
        Matcher m = split(number);
        String court = m.group("court").strip();
        return join(courts().getOrDefault(key(court), court), m);
    }

    @Override
    public String validate(String number) {
        return validate(number, null);
    }

    /**
     * Validates the number, optionally checking that the register is the one
     * the given legal form belongs in — GmbH and PartG, for two.
     */
    public String validate(String number, String companyForm) {
        Matcher m = split(number);
        String court = courts().get(key(m.group("court").strip()));
        if (court == null) {
            throw new InvalidComponentException(Message.of(DeHandelsregisternummer.class, "handelsregisternummer.court",
                    "Not a court that keeps a register."));
        }
        if (companyForm != null
                && !m.group("registry").equals(REGISTRY_BY_COMPANY_FORM.get(companyForm))) {
            throw new InvalidComponentException(Message.of(DeHandelsregisternummer.class, "handelsregisternummer.company-form",
                    "A {0} does not belong in this register.", companyForm));
        }
        return join(court, m);
    }
}
