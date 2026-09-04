package io.github.jefersonsantos06.stdnum;

import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.StdNumProvider;
import io.github.jefersonsantos06.stdnum.spi.Tag;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.TreeMap;

/**
 * The registry of all {@link StdNum} types on the classpath.
 *
 * <p>Number types are discovered once, lazily, through {@link ServiceLoader}
 * ({@link StdNumProvider} entries in {@code META-INF/services}) and indexed by
 * their descriptor id. This replaces the dynamic per-country imports of
 * python-stdnum without reflection: a dispatcher such as a generic VAT
 * validator asks {@code byCountry("br", "vat")} and treats an empty result as
 * an unsupported country.</p>
 */
public final class StdNums {

    private StdNums() {
    }

    /** Lazy, thread-safe initialisation via the class-holder idiom. */
    private static final class Holder {

        static final Map<String, StdNum> BY_ID = load();

        private static Map<String, StdNum> load() {
            Map<String, StdNum> byId = new TreeMap<>();
            ServiceLoader<StdNumProvider> loader =
                    ServiceLoader.load(StdNumProvider.class, StdNums.class.getClassLoader());
            for (StdNumProvider provider : loader) {
                for (StdNum number : provider.numbers()) {
                    String id = number.descriptor().id();
                    StdNum previous = byId.putIfAbsent(id, number);
                    if (previous != null && previous != number) {
                        throw new IllegalStateException(
                                "Duplicate standard number id '" + id + "' provided by "
                                        + previous.getClass().getName() + " and "
                                        + number.getClass().getName());
                    }
                }
            }
            return Collections.unmodifiableMap(byId);
        }
    }

    /** All registered number types, ordered by id. */
    public static Collection<StdNum> all() {
        return Holder.BY_ID.values();
    }

    /** Looks up a number type by its descriptor id ({@code "br.cpf"}), case-insensitive. */
    public static Optional<StdNum> byId(String id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(
                Holder.BY_ID.get(id.strip().toLowerCase(Locale.ROOT)));
    }

    /**
     * Looks up a country-local number type by country code and local name:
     * {@code byCountry("BR", "cpf")} resolves the id {@code "br.cpf"}.
     */
    public static Optional<StdNum> byCountry(String countryCode, String name) {
        if (countryCode == null || name == null) {
            return Optional.empty();
        }
        return byId(countryCode + "." + name);
    }

    /**
     * Convenience overload extracting the country from a {@link java.util.Locale}
     * (as obtained from a user session); the locale's language is irrelevant.
     */
    public static List<StdNum> byCountry(java.util.Locale locale) {
        return locale == null ? List.of() : byCountry(locale.getCountry());
    }

    /** All number types registered for the given ISO 3166-1 alpha-2 country code. */
    public static List<StdNum> byCountry(String countryCode) {
        if (countryCode == null) {
            return List.of();
        }
        List<StdNum> result = new ArrayList<>();
        for (StdNum number : Holder.BY_ID.values()) {
            if (countryCode.equalsIgnoreCase(number.descriptor().countryCode())) {
                result.add(number);
            }
        }
        return List.copyOf(result);
    }

    /** All number types carrying the given tag. */
    public static List<StdNum> byTag(Tag tag) {
        if (tag == null) {
            return List.of();
        }
        List<StdNum> result = new ArrayList<>();
        for (StdNum number : Holder.BY_ID.values()) {
            if (number.descriptor().tags().contains(tag)) {
                result.add(number);
            }
        }
        return List.copyOf(result);
    }
}
