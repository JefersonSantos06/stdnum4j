package io.github.jefersonsantos06.stdnum.spi;

import java.util.List;
import java.util.Objects;

/**
 * What a validator has to say about a failure, before anyone decides what
 * language to say it in.
 *
 * <p>A message is the English sentence the validator was written with, plus
 * — optionally — a {@code code} naming it and the {@code anchor} class whose
 * package holds the translations of that code. {@link Messages} turns the
 * three into a sentence in a chosen language; nothing here knows about
 * {@link java.util.Locale}, so a validator can be shared between callers who
 * want different languages, and a caller who wants none pays nothing.</p>
 *
 * <p>The English text lives here, in the source, next to the code that
 * throws — so the validator still reads as prose — and a translation is an
 * overlay keyed by {@code code}. A message with no code is not untranslatable;
 * it falls back to the sentence for its {@link ValidationError}, which every
 * translation carries.</p>
 *
 * <p>Arguments are converted to text when the message is built, never when it
 * is rendered: a number embedded in a message is part of the number being
 * validated, so it must read the same in every language.</p>
 *
 * @param anchor      the class whose package holds the translations, or
 *                    {@code null} when the message has no code
 * @param code        the key naming this message within that package, or
 *                    {@code null}
 * @param defaultText the English sentence, with {@code {0}}, {@code {1}}, ...
 *                    where the arguments go
 * @param args        the arguments, already text
 */
public record Message(Class<?> anchor, String code, String defaultText, List<String> args) {

    public Message {
        Objects.requireNonNull(defaultText, "defaultText");
        args = args == null ? List.of() : List.copyOf(args);
    }

    /** A sentence with nothing behind it: no code, no arguments, no translation. */
    public static Message plain(String text) {
        return new Message(null, null, text == null ? "" : text, List.of());
    }

    /**
     * A named message, translatable under {@code code} in the package of
     * {@code anchor}.
     *
     * <p>The anchor is normally the class that throws, which puts the
     * translations in the same jar as the validator that needs them: a module
     * carries its own sentences, and there is no central file for every module
     * to edit.</p>
     *
     * <p>The lookup is {@link Class#getResourceAsStream}, so on the module
     * path the anchor's package must be open to this one. Nothing here
     * declares a {@code module-info}, and an automatic module is open, so
     * that is free today; a jar that later declares one has to say
     * {@code opens <package> to io.github.jefersonsantos06.stdnum;} or its
     * translations will be silently ignored.</p>
     */
    public static Message of(Class<?> anchor, String code, String defaultText, Object... args) {
        return new Message(Objects.requireNonNull(anchor, "anchor"),
                Objects.requireNonNull(code, "code"), defaultText, text(args));
    }

    private static List<String> text(Object... args) {
        if (args == null || args.length == 0) {
            return List.of();
        }
        String[] values = new String[args.length];
        for (int i = 0; i < args.length; i++) {
            values[i] = String.valueOf(args[i]);
        }
        return List.of(values);
    }

    /** The English sentence with its arguments filled in. */
    public String text() {
        return interpolate(defaultText, args);
    }

    /**
     * A template with {@code {0}}, {@code {1}}, ... replaced by the arguments.
     *
     * <p>Deliberately not {@link java.text.MessageFormat}: that reads a
     * single quote as a quoting character, so it silently eats the apostrophe
     * in a sentence like "The number's checksum is invalid", and formats a
     * number against whatever locale the JVM happens to have. Here a brace
     * that does not open a valid index is simply a brace.</p>
     */
    static String interpolate(String template, List<String> args) {
        if (args.isEmpty() || template.indexOf('{') < 0) {
            return template;
        }
        StringBuilder sb = new StringBuilder(template.length() + 16);
        int i = 0;
        while (i < template.length()) {
            char c = template.charAt(i);
            if (c != '{') {
                sb.append(c);
                i++;
                continue;
            }
            int close = template.indexOf('}', i + 1);
            int index = close < 0 ? -1 : indexOf(template, i + 1, close);
            if (index < 0 || index >= args.size()) {
                sb.append(c);
                i++;
            } else {
                sb.append(args.get(index));
                i = close + 1;
            }
        }
        return sb.toString();
    }

    /** The digits between two positions as an index, or -1 if they are not digits. */
    private static int indexOf(String template, int from, int to) {
        if (from == to || to - from > 9) {
            return -1;
        }
        int index = 0;
        for (int i = from; i < to; i++) {
            char c = template.charAt(i);
            if (c < '0' || c > '9') {
                return -1;
            }
            index = index * 10 + (c - '0');
        }
        return index;
    }
}
