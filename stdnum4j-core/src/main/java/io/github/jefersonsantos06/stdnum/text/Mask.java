package io.github.jefersonsantos06.stdnum.text;

import java.util.List;
import java.util.Objects;

/**
 * How a number is written: a template in which each slot takes the next
 * character of the compact form and everything else is copied through.
 *
 * <p>Three kinds of slot: {@code '#'} takes any character, {@code '9'} a
 * digit and {@code 'A'} a letter. {@code "##.###.###-#"} turns
 * {@code 200895141} into {@code 20.089.514-1}; {@code "A99 9AA"} fits
 * {@code M344AB} and refuses {@code BFPO61}, which {@code "AAAA 99"} then
 * takes — so a number type with several shapes of the same length keeps one
 * mask per shape and {@link #apply(List, String)} picks the one that fits.</p>
 *
 * <p>A template carries no literal digit or letter, so that a slot and a
 * literal can never be confused; the literals are separators, and a
 * {@code compact} that strips those separators makes the formatted number
 * round-trip.</p>
 */
public final class Mask {

    private final String template;
    private final int slots;

    private Mask(String template) {
        this.template = template;
        int count = 0;
        for (int i = 0; i < template.length(); i++) {
            char c = template.charAt(i);
            if (isSlot(c)) {
                count++;
            } else if (Character.isLetterOrDigit(c)) {
                throw new IllegalArgumentException(
                        "a mask holds slots and separators, not '" + c + "': " + template);
            }
        }
        this.slots = count;
    }

    /** A mask from its template, such as {@code "#####-###"} or {@code "A9A 9AA"}. */
    public static Mask of(String template) {
        return new Mask(Objects.requireNonNull(template, "template"));
    }

    /** The template this mask was made from. */
    public String template() {
        return template;
    }

    /** How many characters of a compact number this mask takes. */
    public int slots() {
        return slots;
    }

    /** Whether the compact number has the length and the shape of this mask. */
    public boolean matches(String compact) {
        if (compact.length() != slots) {
            return false;
        }
        int at = 0;
        for (int i = 0; i < template.length(); i++) {
            char t = template.charAt(i);
            if (isSlot(t) && !fits(t, compact.charAt(at++))) {
                return false;
            }
        }
        return true;
    }

    /**
     * The compact number written through this mask. The caller has checked
     * {@link #matches(String)}; a number of another length is a programming
     * error, not a validation failure.
     */
    public String fill(String compact) {
        if (compact.length() != slots) {
            throw new IllegalArgumentException(
                    "mask " + template + " takes " + slots + " characters, not " + compact);
        }
        StringBuilder out = new StringBuilder(template.length());
        int at = 0;
        for (int i = 0; i < template.length(); i++) {
            char t = template.charAt(i);
            out.append(isSlot(t) ? compact.charAt(at++) : t);
        }
        return out.toString();
    }

    /**
     * The compact number written through the first mask that fits it — by
     * shape, or failing that by length alone — or unchanged when none does:
     * a number no mask describes is its own presentation.
     *
     * <p>The second pass is what lets one example stand for a family: the
     * Eircode {@code A65 F4E2} yields {@code "A99 A9A9"}, and {@code D02X285}
     * fits it by length though not by shape, so it is written {@code D02
     * X285} all the same. The first pass still tells {@code BFPO61} from
     * {@code M344AB}.</p>
     */
    public static String apply(List<Mask> masks, String compact) {
        for (Mask mask : masks) {
            if (mask.matches(compact)) {
                return mask.fill(compact);
            }
        }
        for (Mask mask : masks) {
            if (mask.slots() == compact.length()) {
                return mask.fill(compact);
            }
        }
        return compact;
    }

    private static boolean isSlot(char c) {
        return c == '#' || c == '9' || c == 'A';
    }

    private static boolean fits(char slot, char c) {
        switch (slot) {
            case '9':
                return c >= '0' && c <= '9';
            case 'A':
                return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z');
            default:
                return true;
        }
    }

    @Override
    public String toString() {
        return template;
    }
}
