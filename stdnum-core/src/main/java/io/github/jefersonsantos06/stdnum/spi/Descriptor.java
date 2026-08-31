package io.github.jefersonsantos06.stdnum.spi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Typed metadata about a number type: identity, naming and categorisation.
 *
 * <p>The {@code id} is the canonical registry key. By convention it is the
 * lower-case country code followed by the local short name ({@code "br.cpf"},
 * {@code "de.vat"}), or just the short name for international numbers
 * ({@code "iban"}, {@code "isbn"}).</p>
 *
 * @param id          canonical registry id, lower case ({@code "br.cpf"})
 * @param countryCode ISO 3166-1 alpha-2 country code, or {@code null} for
 *                    international numbers
 * @param shortName   the everyday name of the number ({@code "CPF"})
 * @param title       one-line human readable title; defaults to {@code shortName}
 * @param description longer free-form description; defaults to empty
 * @param tags        categories for discovery; defaults to empty
 * @param references  URLs documenting the number; defaults to empty
 */
public record Descriptor(
        String id,
        String countryCode,
        String shortName,
        String title,
        String description,
        Set<Tag> tags,
        List<String> references) {

    private static final Pattern ID_PATTERN = Pattern.compile("[a-z0-9]+(?:[._-][a-z0-9]+)*");
    private static final Pattern COUNTRY_PATTERN = Pattern.compile("[A-Z]{2}");

    public Descriptor {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(shortName, "shortName");
        if (!ID_PATTERN.matcher(id).matches()) {
            throw new IllegalArgumentException("id must match " + ID_PATTERN.pattern() + " but was: " + id);
        }
        if (shortName.isBlank()) {
            throw new IllegalArgumentException("shortName must not be blank");
        }
        if (countryCode != null) {
            countryCode = countryCode.toUpperCase(Locale.ROOT);
            if (!COUNTRY_PATTERN.matcher(countryCode).matches()) {
                throw new IllegalArgumentException(
                        "countryCode must be two letters but was: " + countryCode);
            }
        }
        if (title == null || title.isBlank()) {
            title = shortName;
        }
        if (description == null) {
            description = "";
        }
        tags = (tags == null || tags.isEmpty()) ? Set.of() : Collections.unmodifiableSet(EnumSet.copyOf(tags));
        references = (references == null) ? List.of() : List.copyOf(references);
    }

    /** The country code as an {@link Optional} (empty for international numbers). */
    public Optional<String> country() {
        return Optional.ofNullable(countryCode);
    }

    /** Starts a builder with the two mandatory fields. */
    public static Builder of(String id, String shortName) {
        return new Builder(id, shortName);
    }

    /** Fluent builder; all fields except {@code id} and {@code shortName} are optional. */
    public static final class Builder {

        private final String id;
        private final String shortName;
        private String countryCode;
        private String title;
        private String description;
        private final EnumSet<Tag> tags = EnumSet.noneOf(Tag.class);
        private final List<String> references = new ArrayList<>();

        private Builder(String id, String shortName) {
            this.id = id;
            this.shortName = shortName;
        }

        public Builder country(String countryCode) {
            this.countryCode = countryCode;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder tags(Tag... tags) {
            Collections.addAll(this.tags, tags);
            return this;
        }

        public Builder references(String... references) {
            Collections.addAll(this.references, references);
            return this;
        }

        public Descriptor build() {
            return new Descriptor(id, countryCode, shortName, title, description, tags, references);
        }
    }
}
