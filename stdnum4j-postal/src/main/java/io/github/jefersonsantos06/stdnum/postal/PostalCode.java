package io.github.jefersonsantos06.stdnum.postal;

import io.github.jefersonsantos06.stdnum.numdb.NumDb;
import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Mask;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Pattern;

/**
 * The postal code of a country or territory, in the shape Google's address
 * metadata — the data behind libaddressinput — documents for it.
 *
 * <p>There is one instance per region the metadata gives a pattern for, each
 * a full {@link StdNum} registered as {@code <cc>.postal_code}:</p>
 *
 * <pre>{@code
 * PostalCode.of("BR").orElseThrow().format("40301110");   // "40301-110"
 * PostalCode.of("GB").orElseThrow().validate("sw1a 1aa"); // "SW1A1AA"
 * StdNums.byId("jp.postal_code");                         // via the registry
 * }</pre>
 *
 * <p>The metadata describes a code as written, separators and all; this
 * class matches the compact form, so a Japanese code passes with or without
 * its hyphen and a British one with or without its space. That is more
 * lenient than libaddressinput itself, which is the point of a
 * {@code compact}. What a regex cannot do is tell a wrong character from a
 * wrong length, so every rejection is an {@link InvalidFormatException}.</p>
 *
 * <p>{@link #format} writes the code the way the metadata's own examples are
 * written. A shape those examples do not show — {@code LIMA 1}, a British
 * Forces code with four digits, a six-character Maltese code — is presented
 * compact rather than guessed at.</p>
 *
 * <p>Countries whose postal code has a hand-written type in a regional
 * module — richer than a pattern, such as Austria's list of codes in use —
 * are left to that type; see {@link #HAND_WRITTEN}.</p>
 */
public final class PostalCode implements StdNum {

    private static final String SOURCE = "https://chromium-i18n.appspot.com/ssl-address/data/";

    /**
     * Countries whose postal code is validated by a hand-written type in a
     * regional module. The data file still describes them; no instance is
     * built for them, so that a country never has two postal code types that
     * disagree. A test in {@code stdnum4j-all} names the country to add here
     * when a new hand-written one appears.
     */
    private static final Set<String> HAND_WRITTEN = Set.of("AT", "ES", "NL", "SE");

    private static final Map<String, PostalCode> BY_COUNTRY = load();

    private static Map<String, PostalCode> load() {
        Map<String, PostalCode> byCountry = new TreeMap<>();
        for (NumDb.Entry entry : NumDb.load(PostalCode.class, "postal-codes.dat").entries()) {
            if (!HAND_WRITTEN.contains(entry.part())) {
                byCountry.put(entry.part(), new PostalCode(entry.part(), entry.properties()));
            }
        }
        return Collections.unmodifiableMap(byCountry);
    }

    private final String country;
    private final Pattern pattern;
    private final List<Mask> masks;
    private final List<String> strip;
    private final List<String> examples;
    private final Descriptor descriptor;

    private PostalCode(String country, Map<String, String> data) {
        this.country = country;
        this.pattern = Pattern.compile(data.get("pattern"), Pattern.CASE_INSENSITIVE);
        this.masks = split(data.get("masks")).stream().map(Mask::of).toList();
        this.strip = split(data.get("strip"));
        this.examples = split(data.get("examples"));
        String name = data.get("name");
        String noun = switch (data.getOrDefault("kind", "postal")) {
            case "zip" -> "ZIP code";
            case "eircode" -> "Eircode";
            case "pin" -> "PIN code";
            default -> "postal code";
        };
        this.descriptor = Descriptor
                .of(country.toLowerCase(Locale.ROOT) + ".postal_code",
                        noun.equals("postal code") ? "Postal code" : noun)
                .country(country)
                .title(name + " " + noun)
                .description("Postal code of " + name + ", written like "
                        + String.join(" or ", examples.subList(0, Math.min(2, examples.size())))
                        + ".")
                .tags(Tag.POSTAL)
                .references(SOURCE + country)
                .build();
    }

    private static List<String> split(String csv) {
        return csv == null || csv.isEmpty() ? List.of() : List.of(csv.split(","));
    }

    /**
     * The postal code type of a country, by its ISO 3166-1 alpha-2 code —
     * empty for a country the metadata gives no pattern for, and for one
     * whose code has a hand-written type of its own.
     */
    public static Optional<PostalCode> of(String countryCode) {
        return countryCode == null
                ? Optional.empty()
                : Optional.ofNullable(BY_COUNTRY.get(countryCode.toUpperCase(Locale.ROOT)));
    }

    /** Every postal code type, in country code order. */
    public static Collection<PostalCode> all() {
        return BY_COUNTRY.values();
    }

    /** The ISO 3166-1 alpha-2 code of the country this instance validates. */
    public String country() {
        return country;
    }

    /** The codes the metadata gives as examples, written as it writes them. */
    public List<String> examples() {
        return examples;
    }

    @Override
    public Descriptor descriptor() {
        return descriptor;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Upper case, without spaces or hyphens, and without a prefix the code
     * is often written with but the pattern does not want — the ISO code, or
     * the {@code L-} in front of a Luxembourg code. A prefix is only removed
     * when it helps: {@code MTP 1234} stays whole because Malta's pattern
     * takes it as it is.</p>
     */
    @Override
    public String compact(String number) {
        String n = Strings.compact(number, " -").toUpperCase(Locale.ROOT);
        for (String prefix : strip) {
            if (n.length() > prefix.length() && n.startsWith(prefix)
                    && !pattern.matcher(n).matches()
                    && pattern.matcher(n.substring(prefix.length())).matches()) {
                return n.substring(prefix.length());
            }
        }
        return n;
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (!pattern.matcher(n).matches()) {
            throw new InvalidFormatException();
        }
        return n;
    }

    @Override
    public String format(String number) {
        return Mask.apply(masks, validate(number));
    }

    /**
     * The shapes this country's examples show, empty for the 145 whose codes
     * the source writes without a separator.
     */
    @Override
    public List<Mask> masks() {
        return masks;
    }
}
