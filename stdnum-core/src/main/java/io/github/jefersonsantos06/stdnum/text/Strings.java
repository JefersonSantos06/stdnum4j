package io.github.jefersonsantos06.stdnum.text;

import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.Reasons;

import java.util.HashMap;
import java.util.Map;

/**
 * String helpers shared by all number types.
 *
 * <p>{@link #clean(String, String)} first replaces common Unicode look-alikes
 * of separators and digits with their ASCII counterpart (en/em dashes become
 * {@code '-'}, fullwidth and mathematical digits become {@code '0'-'9'},
 * ideographic spaces become {@code ' '}, ...), then removes the characters
 * the caller declared as valid separators. This makes validators robust
 * against numbers copy-pasted from formatted documents and web pages.</p>
 */
public final class Strings {

    private Strings() {
    }

    /** Unicode code point to ASCII replacement for common separator look-alikes. */
    private static final Map<Integer, Character> CHAR_MAP = buildCharMap();

    private static Map<Integer, Character> buildCharMap() {
        Map<Integer, Character> m = new HashMap<>(128);
        // hyphen/dash/minus variants
        put(m, '-',
                0x00AF, 0x02D7, 0x058A, 0x05BE, 0x180A, 0x2010, 0x2011, 0x2012,
                0x2013, 0x2014, 0x2015, 0x2043, 0x203E, 0x207B, 0x208B, 0x2212,
                0x23AF, 0x2E3A, 0x2E3B, 0xFE58, 0xFE63, 0xFF0D, 0xFFE3);
        // asterisk variants
        put(m, '*',
                0x066D, 0x204E, 0x2217, 0x22C6, 0x2731, 0xFE61, 0xFF0A);
        // comma variants
        put(m, ',',
                0x00B8, 0x060C, 0x066B, 0x066C, 0x201A, 0x3001, 0xFE50, 0xFE51, 0xFF0C);
        // full stop / middle dot variants
        put(m, '.',
                0x00B7, 0x02D9, 0x0387, 0x06D4, 0x2022, 0x2024, 0x2027, 0x2219,
                0x22C5, 0x3002, 0x30FB, 0xFE52, 0xFF0E, 0xFF65);
        // solidus variants
        put(m, '/',
                0x2044, 0x2215, 0x29F8, 0xFF0F);
        // colon variants
        put(m, ':',
                0x1361, 0xA789, 0xFE13, 0xFE55, 0xFF1A);
        // space variants
        put(m, ' ',
                0x00A0, 0x1680, 0x2000, 0x2001, 0x2002, 0x2003, 0x2004, 0x2005,
                0x2006, 0x2007, 0x2008, 0x2009, 0x200A, 0x202F, 0x205F, 0x3000);
        // apostrophe/quote/accent variants
        put(m, '\'',
                0x0060, 0x00B4, 0x02B9, 0x02BB, 0x02BC, 0x02C8, 0x0300, 0x0301,
                0x0313, 0x0314, 0x055A, 0x2018, 0x2019, 0x201B, 0x2032, 0xFF07);
        return m;
    }

    private static void put(Map<Integer, Character> map, char to, int... codePoints) {
        for (int cp : codePoints) {
            map.put(cp, to);
        }
    }

    /**
     * Maps one code point to its ASCII counterpart, or returns the code point
     * unchanged. Handles fullwidth digits (U+FF10..U+FF19), the Arabic-Indic
     * digits (U+0660.. and U+06F0..) and the mathematical digit blocks
     * (U+1D7CE..U+1D7FF) by range.
     */
    private static int mapCodePoint(int cp) {
        if (cp >= 0xFF10 && cp <= 0xFF19) {
            return '0' + (cp - 0xFF10);
        }
        if (cp >= 0x0660 && cp <= 0x0669) {
            return '0' + (cp - 0x0660);
        }
        if (cp >= 0x06F0 && cp <= 0x06F9) {
            return '0' + (cp - 0x06F0);
        }
        if (cp >= 0x1D7CE && cp <= 0x1D7FF) {
            return '0' + ((cp - 0x1D7CE) % 10);
        }
        Character mapped = CHAR_MAP.get(cp);
        return mapped != null ? mapped : cp;
    }

    /** Same as {@link #clean(String, String)} with no characters removed. */
    public static String clean(String number) {
        return clean(number, "");
    }

    /**
     * Replaces Unicode look-alike characters with their ASCII counterpart and
     * removes every character contained in {@code deleteChars}.
     *
     * <pre>{@code
     * clean("123-456:78 9", " -:")  -> "123456789"
     * clean("1–2—3")      -> "1-2-3"     (en dash, em dash)
     * }</pre>
     *
     * @throws InvalidFormatException if {@code number} is {@code null}
     */
    public static String clean(String number, String deleteChars) {
        if (number == null) {
            throw new InvalidFormatException(Reasons.nullNumber());
        }
        String delete = (deleteChars == null) ? "" : deleteChars;
        StringBuilder sb = new StringBuilder(number.length());
        int i = 0;
        int n = number.length();
        while (i < n) {
            int cp = number.codePointAt(i);
            i += Character.charCount(cp);
            int mapped = mapCodePoint(cp);
            if (delete.indexOf(mapped) < 0) {
                sb.appendCodePoint(mapped);
            }
        }
        return sb.toString();
    }

    /**
     * The usual first step of {@code StdNum.compact}: {@link #clean(String, String)}
     * followed by stripping surrounding whitespace.
     */
    public static String compact(String number, String deleteChars) {
        return clean(number, deleteChars).strip();
    }

    /**
     * Whether the string is non-empty and consists only of ASCII digits
     * {@code '0'-'9'}. Unlike {@link Character#isDigit(char)} this rejects
     * all other Unicode digit categories. Returns {@code false} for {@code null}.
     */
    public static boolean isDigits(String s) {
        if (s == null || s.isEmpty()) {
            return false;
        }
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c < '0' || c > '9') {
                return false;
            }
        }
        return true;
    }
}
