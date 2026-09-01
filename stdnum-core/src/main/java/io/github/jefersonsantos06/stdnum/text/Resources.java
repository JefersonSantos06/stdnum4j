package io.github.jefersonsantos06.stdnum.text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Loads small line-oriented data files that ship alongside a number type.
 *
 * <p>Some registries issue numbers that do not satisfy their own check
 * digit. Those exceptions cannot be derived, only listed, so the affected
 * types carry a companion file of known-valid numbers. Blank lines and
 * lines starting with {@code '#'} are ignored, so each file can document
 * its own provenance.</p>
 *
 * <p>Files are read once and cached for the lifetime of the JVM.</p>
 */
public final class Resources {

    private Resources() {
    }

    private static final Map<String, Set<String>> CACHE = new ConcurrentHashMap<>();

    /**
     * The set of entries in the classpath resource, resolved against
     * {@code anchor} — pass the class that ships with the file.
     *
     * @throws IllegalArgumentException if the resource does not exist
     */
    public static Set<String> lines(Class<?> anchor, String resource) {
        return CACHE.computeIfAbsent(anchor.getName() + "|" + resource, key -> {
            InputStream in = anchor.getResourceAsStream(resource);
            if (in == null) {
                throw new IllegalArgumentException(
                        "Resource not found: " + resource + " (anchor " + anchor.getName() + ")");
            }
            Set<String> entries = new HashSet<>();
            try (BufferedReader reader =
                         new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String entry = line.strip();
                    if (!entry.isEmpty() && !entry.startsWith("#")) {
                        entries.add(entry);
                    }
                }
            } catch (IOException e) {
                throw new UncheckedIOException("Failed to read " + resource, e);
            }
            return Collections.unmodifiableSet(entries);
        });
    }
}
