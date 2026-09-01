package io.github.jefersonsantos06.stdnum.international;

import io.github.jefersonsantos06.stdnum.algo.Iso7064;
import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.util.Locale;

/**
 * ISAN, the International Standard Audiovisual Number: a root that names the
 * work, an episode, and optionally a version — each of the last two followed
 * by its own check character when the number is written in full.
 *
 * <p>Both check characters are optional, which is why an ISAN comes in
 * lengths from 16 to 26. Whichever are present are checked.</p>
 */
public final class Isan implements StdNum {

    public static final Isan INSTANCE = new Isan();

    private static final String HEX = "0123456789ABCDEF";

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("isan", "ISAN")
                    .title("International Standard Audiovisual Number")
                    .description("Identifier of an audiovisual work: a root, an episode and an"
                            + " optional version, with optional check characters.")
                    .tags(Tag.MEDIA, Tag.PRODUCT)
                    .references("https://www.isan.org/")
                    .build();

    private Isan() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    /** The root, episode, first check, version and second check of a number. */
    public static String[] split(String number) {
        String n = Strings.compact(number, " -").toUpperCase(Locale.ROOT);
        if (n.length() == 17 || n.length() == 26) {
            return new String[] {n.substring(0, 12), n.substring(12, 16),
                    n.substring(16, 17), slice(n, 17, 25), slice(n, 25, n.length())};
        }
        if (n.length() > 16) {
            return new String[] {n.substring(0, 12), n.substring(12, 16), "",
                    slice(n, 16, 24), slice(n, 24, n.length())};
        }
        return new String[] {slice(n, 0, 12), slice(n, 12, 16), slice(n, 16, n.length()), "", ""};
    }

    /** The characters between two positions, clamped to what is there. */
    private static String slice(String n, int from, int to) {
        int start = Math.min(from, n.length());
        return n.substring(start, Math.max(start, Math.min(to, n.length())));
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " -").toUpperCase(Locale.ROOT);
    }

    /** The number with both check characters removed. */
    public static String stripCheckDigits(String number) {
        String[] parts = split(number);
        return parts[0] + parts[1] + parts[3];
    }

    /** The number with both check characters present, computing any that are missing. */
    public static String addCheckDigits(String number) {
        String[] parts = split(number);
        String check1 = parts[2].isEmpty()
                ? String.valueOf(Iso7064.MOD_37_36.calcCheckDigit(parts[0] + parts[1]))
                : parts[2];
        String check2 = parts[4];
        if (check2.isEmpty() && !parts[3].isEmpty()) {
            check2 = String.valueOf(
                    Iso7064.MOD_37_36.calcCheckDigit(parts[0] + parts[1] + parts[3]));
        }
        return parts[0] + parts[1] + check1 + parts[3] + check2;
    }

    /**
     * The URN form of the number, as ISO 15706 registers it. Both check
     * characters are part of the URN, so any that the number does not carry
     * are computed.
     */
    public static String toUrn(String number) {
        return "URN:ISAN:" + INSTANCE.format(addCheckDigits(number));
    }

    /**
     * The XML form of the number: the root, the episode and the version as
     * three attributes. The check characters are not part of it, and the
     * version is empty for a number that has none.
     */
    public static String toXml(String number) {
        String[] parts = split(INSTANCE.validate(number));
        return "<ISAN root=\"" + hyphenate(parts[0]) + "\" episode=\"" + parts[1]
                + "\" version=\"" + hyphenate(parts[3]) + "\" />";
    }

    /** A run of hexadecimal characters broken into groups of four. */
    private static String hyphenate(String part) {
        StringBuilder sb = new StringBuilder(part.length() + part.length() / 4);
        for (int i = 0; i < part.length(); i += 4) {
            if (i > 0) {
                sb.append('-');
            }
            sb.append(part, i, Math.min(i + 4, part.length()));
        }
        return sb.toString();
    }

    @Override
    public String validate(String number) {
        String[] parts = split(number);
        String root = parts[0];
        String episode = parts[1];
        String check1 = parts[2];
        String version = parts[3];
        String check2 = parts[4];
        String body = root + episode + version;
        for (int i = 0; i < body.length(); i++) {
            if (HEX.indexOf(body.charAt(i)) < 0) {
                throw new InvalidFormatException();
            }
        }
        if (root.length() != 12 || episode.length() != 4 || check1.length() > 1
                || (version.length() != 0 && version.length() != 8)) {
            throw new InvalidLengthException();
        }
        if (!check1.isEmpty()) {
            Iso7064.MOD_37_36.validate(root + episode + check1);
        }
        if (!check2.isEmpty()) {
            Iso7064.MOD_37_36.validate(root + episode + version + check2);
        }
        return root + episode + check1 + version + check2;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Groups the number as it stands. The reference also fills in any
     * missing check characters here; that would make the formatted number a
     * different number, so it lives in {@link #addCheckDigits(String)}.</p>
     */
    @Override
    public String format(String number) {
        String[] parts = split(validate(number));
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 12; i += 4) {
            sb.append(parts[0], i, i + 4).append('-');
        }
        sb.append(parts[1]);
        if (!parts[2].isEmpty()) {
            sb.append('-').append(parts[2]);
        }
        if (!parts[3].isEmpty()) {
            for (int i = 0; i < 8; i += 4) {
                sb.append('-').append(parts[3], i, i + 4);
            }
        }
        if (!parts[4].isEmpty()) {
            sb.append('-').append(parts[4]);
        }
        return sb.toString();
    }
}
