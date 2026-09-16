package io.github.jefersonsantos06.stdnum.text;

import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StringsTest {

    @Test
    void cleanRemovesDeclaredSeparators() {
        assertEquals("123456789", Strings.clean("123-456:78 9", " -:"));
    }

    @Test
    void cleanMapsUnicodeDashesToAsciiHyphen() {
        // hyphen, en dash, em dash, horizontal bar, minus sign
        assertEquals("1-2-3-4-5-6",
                Strings.clean("1‐2–3—4―5−6"));
    }

    @Test
    void cleanMapsFullwidthAndMathematicalDigits() {
        assertEquals("123", Strings.clean("１２３"));
        // U+1D7CE MATHEMATICAL BOLD DIGIT ZERO, U+1D7D8 DOUBLE-STRUCK ZERO
        assertEquals("00", Strings.clean("𝟎𝟘"));
    }

    @Test
    void cleanMapsArabicIndicDigits() {
        // Arabic-Indic and extended Arabic-Indic digits, as used in Egypt
        assertEquals("123", Strings.clean("١٢٣"));
        assertEquals("456", Strings.clean("۴۵۶"));
    }

    @Test
    void cleanMapsUnicodeSpaces() {
        // no-break space, ideographic space removed as declared separators
        assertEquals("12", Strings.clean("1 　2", " "));
    }

    @Test
    void cleanNullThrowsInvalidFormat() {
        assertThrows(InvalidFormatException.class, () -> Strings.clean(null, " "));
    }

    @Test
    void compactCleansAndStrips() {
        assertEquals("39053344705", Strings.compact("  390.533.447-05  ", " -."));
    }

    @Test
    void isDigitsAcceptsOnlyAsciiDigits() {
        assertTrue(Strings.isDigits("0123456789"));
        assertFalse(Strings.isDigits(""));
        assertFalse(Strings.isDigits(null));
        assertFalse(Strings.isDigits("12a4"));
        assertFalse(Strings.isDigits("12 4"));
        // ARABIC-INDIC digits must be rejected
        assertFalse(Strings.isDigits("١٢٣"));
    }

    @Test
    void padStartFillsToTheLeftAndNeverTruncates() {
        assertEquals("00515", Strings.padStart("515", 5, '0'));
        assertEquals("515", Strings.padStart("515", 3, '0'));
        assertEquals("515", Strings.padStart("515", 2, '0'));
        assertEquals("0000", Strings.padStart("", 4, '0'));
        assertEquals("  AB", Strings.padStart("AB", 4, ' '));
    }

    @Test
    void padStartNullThrowsInvalidFormat() {
        assertThrows(InvalidFormatException.class, () -> Strings.padStart(null, 4, '0'));
    }

    @Test
    void firstTakesTheHeadAndNeverOverruns() {
        assertEquals("12345", Strings.first("123456789", 5));
        assertEquals("123", Strings.first("123", 5));
        assertEquals("", Strings.first("123", 0));
    }

    @Test
    void firstNullThrowsInvalidFormat() {
        assertThrows(InvalidFormatException.class, () -> Strings.first(null, 5));
    }
}
