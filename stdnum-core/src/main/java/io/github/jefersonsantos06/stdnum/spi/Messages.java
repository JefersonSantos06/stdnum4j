package io.github.jefersonsantos06.stdnum.spi;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Says a validation failure in one language.
 *
 * <p>The library never picks the language: a caller makes a {@code Messages}
 * for the locale it already has — from a request header, a user profile, a
 * command line — and keeps it. There is no default instance, no static
 * setter, and nothing consults {@link Locale#getDefault()}, so two callers in
 * one process can render the same failure differently and a test cannot be
 * poisoned by the machine it runs on.</p>
 *
 * <pre>{@code
 * Messages pt = Messages.forLocale(Locale.forLanguageTag("pt-BR"));
 * switch (cpf.check(input)) {
 *     case Check.Valid v   -> store(v.compact());
 *     case Check.Invalid i -> reject(pt.render(i));
 * }
 * }</pre>
 *
 * <p>A sentence is looked for in three places, in order:</p>
 *
 * <ol>
 *   <li>the message's own {@link Message#code() code}, in the package of its
 *       {@link Message#anchor() anchor} — so a module ships the translations
 *       of its own numbers, inside its own jar;</li>
 *   <li>{@code error.<NAME>} for the {@link ValidationError}, in this
 *       package — four sentences that every translation carries, so every
 *       failure has something true to say even when nothing else is
 *       translated;</li>
 *   <li>the English {@link Message#defaultText() defaultText} the validator
 *       was written with.</li>
 * </ol>
 *
 * <p>Which means a translation is never all-or-nothing, and a missing key
 * costs a less specific sentence rather than an English one.</p>
 *
 * <p>Translations are {@code messages_<language>[_<COUNTRY>].properties} read
 * as UTF-8 from the anchor's package. There is deliberately no
 * {@code messages.properties}: English lives in the Java source, once, where
 * the failure is thrown. Rendering never throws — a translation file that
 * does not parse is treated as absent, because a typo by a translator must
 * not be able to break validation.</p>
 *
 * <p>Instances are immutable, thread-safe, and cache what they have read.</p>
 */
public final class Messages {

    private static final Map<String, String> NONE = Map.of();

    private final Locale locale;
    /** Least specific first, so that a more specific file overlays it. */
    private final List<String> candidates;
    private final Map<Class<?>, Map<String, String>> bundles = new ConcurrentHashMap<>();

    private Messages(Locale locale) {
        this.locale = locale;
        this.candidates = candidates(locale);
    }

    /**
     * Renders failures in the given locale.
     *
     * <p>{@link Locale#ROOT} and any locale without a language render the
     * English text, which is what the library is written in.</p>
     */
    public static Messages forLocale(Locale locale) {
        return new Messages(Objects.requireNonNull(locale, "locale"));
    }

    /** The locale this renders in. */
    public Locale locale() {
        return locale;
    }

    /**
     * The file names to try, least specific first.
     *
     * <p>Written out rather than taken from {@link java.util.ResourceBundle},
     * whose {@code Control} cannot be used at all from a named module and
     * whose default candidate list falls back to {@link Locale#getDefault()}
     * — which would be the library choosing the caller's language.</p>
     */
    private static List<String> candidates(Locale locale) {
        // the locale reaches here from a request header or a command line and
        // ends up in a resource path, so it is checked before it names one:
        // new Locale("cl/../../secret") is a legal Locale
        String language = locale.getLanguage();
        if (!isLetters(language, 2, 8)) {
            return List.of();
        }
        List<String> names = new ArrayList<>(2);
        names.add("messages_" + language + ".properties");
        String country = locale.getCountry();
        if (isLetters(country, 2, 2) || isDigits(country)) {
            names.add("messages_" + language + "_" + country + ".properties");
        }
        return List.copyOf(names);
    }

    /** An ISO 639 language, or an ISO 3166-1 alpha-2 country. */
    private static boolean isLetters(String value, int min, int max) {
        if (value.length() < min || value.length() > max) {
            return false;
        }
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if ((c < 'a' || c > 'z') && (c < 'A' || c > 'Z')) {
                return false;
            }
        }
        return true;
    }

    /** A UN M.49 area code, which is how a Locale spells a region like 419. */
    private static boolean isDigits(String value) {
        if (value.length() != 3) {
            return false;
        }
        for (int i = 0; i < 3; i++) {
            char c = value.charAt(i);
            if (c < '0' || c > '9') {
                return false;
            }
        }
        return true;
    }

    /** What this locale's translation says, in the package of one class. */
    private Map<String, String> bundle(Class<?> anchor) {
        if (candidates.isEmpty()) {
            return NONE;
        }
        return bundles.computeIfAbsent(anchor, key -> {
            Map<String, String> merged = new HashMap<>();
            for (String name : candidates) {
                read(key, name, merged);
            }
            return merged.isEmpty() ? NONE : Map.copyOf(merged);
        });
    }

    private static void read(Class<?> anchor, String name, Map<String, String> into) {
        try (InputStream in = anchor.getResourceAsStream(name)) {
            if (in == null) {
                return;
            }
            Properties properties = new Properties();
            // load(InputStream) would read the file as ISO-8859-1
            properties.load(new InputStreamReader(in, StandardCharsets.UTF_8));
            properties.forEach((k, v) -> into.put(String.valueOf(k), String.valueOf(v)));
        } catch (IOException | RuntimeException e) {
            // A malformed escape makes load() throw IllegalArgumentException,
            // an unchecked exception that would otherwise escape from inside
            // validate(). A translation nobody can read is a translation that
            // is not there — and load() throws before anything was merged, so
            // a broken country file still leaves the language file standing.
        }
    }

    /**
     * The failure said in this locale.
     *
     * @throws NullPointerException if either argument is {@code null}; nothing
     *                              else is ever thrown, whatever the state of
     *                              the translation files
     */
    public String render(ValidationError error, Message message) {
        Objects.requireNonNull(error, "error");
        Objects.requireNonNull(message, "message");
        try {
            String template = null;
            if (message.anchor() != null && message.code() != null) {
                template = usable(bundle(message.anchor()).get(message.code()));
            }
            if (template == null) {
                template = usable(bundle(ValidationError.class).get("error." + error.name()));
            }
            return template == null
                    ? message.text()
                    : Message.interpolate(template, message.args());
        } catch (RuntimeException e) {
            return message.text();
        }
    }

    /**
     * A sentence, or {@code null} when there is nothing to say. A key left
     * with nothing after the {@code =} is what a half-written translation
     * looks like, and it is the one kind of breakage that parses; showing
     * somebody an empty string is worse than showing them a vaguer one.
     */
    private static String usable(String template) {
        return template == null || template.isBlank() ? null : template;
    }

    /** The exception said in this locale. */
    public String render(ValidationException failure) {
        Objects.requireNonNull(failure, "failure");
        return render(failure.error(), failure.message());
    }

    /** The rejection said in this locale. */
    public String render(Check.Invalid invalid) {
        Objects.requireNonNull(invalid, "invalid");
        return render(invalid.error(), invalid.message());
    }

    @Override
    public String toString() {
        return "Messages[" + locale.toLanguageTag() + "]";
    }
}
