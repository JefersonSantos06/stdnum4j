package io.github.jefersonsantos06.stdnum.tck;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads sample numbers from classpath fixture files.
 *
 * <p>Files live in {@code src/test/resources/fixtures/<name>.txt}: one number
 * per line, kept exactly as found in the wild (masks and separators included).
 * Blank lines and lines starting with {@code '#'} are ignored.</p>
 */
public final class Fixtures {

    private Fixtures() {
    }

    /**
     * Loads {@code /fixtures/<name>.txt} using the class loader of
     * {@code anchor} (pass the test class so the consuming module's resources
     * are visible). Returns an empty list when the file does not exist.
     */
    public static List<String> load(Class<?> anchor, String name) {
        InputStream in = anchor.getResourceAsStream("/fixtures/" + name + ".txt");
        if (in == null) {
            return List.of();
        }
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader =
                     new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String sample = line.strip();
                if (!sample.isEmpty() && !sample.startsWith("#")) {
                    lines.add(sample);
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read fixture " + name, e);
        }
        return List.copyOf(lines);
    }
}
