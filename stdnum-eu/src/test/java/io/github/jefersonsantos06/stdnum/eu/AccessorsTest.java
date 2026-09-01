package io.github.jefersonsantos06.stdnum.eu;

import io.github.jefersonsantos06.stdnum.spi.ValidationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Exercises the derived accessors of the European number types. */
class AccessorsTest {

    @Test
    void cifSplitsIntoItsFourComponents() {
        EsCif.Parts p = EsCif.split("A13585625");
        assertEquals("A", p.organisationType());
        assertEquals("13", p.province());
        assertEquals("58562", p.sequence());
        assertEquals("5", p.checkDigit());
    }

    @Test
    void spanishCheckLettersAreCalculable() {
        assertEquals('K', EsDni.calcCheckDigit("54362315"));
        assertEquals('W', EsNie.calcCheckDigit("X2482300"));
        assertThrows(ValidationException.class, () -> EsDni.calcCheckDigit("5436231"));
        assertThrows(ValidationException.class, () -> EsNie.calcCheckDigit("A2482300"));
    }

    @Test
    void irishVatConvertsOldStyleToNew() {
        // the second character is dropped, the leading digit moves to the
        // seventh position and a zero takes the front
        assertEquals("0797398I", IeVat.convert("8D79739I"));
        assertTrue(IeVat.INSTANCE.isValid("8D79739I"));
        // a new-style number is returned untouched
        assertEquals("6433435F", IeVat.convert("6433435F"));
    }

    @Test
    void danishLegacyChecksumIsStillAvailable() {
        // abandoned in 2007, so validate() does not apply it, but numbers
        // issued before then still fold to zero
        assertEquals(0, DkCpr.checksum("2110625629"));
        assertThrows(ValidationException.class, () -> DkCpr.checksum("211062562"));
    }

    @Test
    void checkDigitsAreCalculable() {
        assertEquals('9', PlPesel.calcCheckDigit("4405140135"));
        assertEquals('3', PtNif.calcCheckDigit("50196484"));
        assertEquals('7', RoCnp.calcCheckDigit("163061512345"));
    }

    @Test
    void britishVatIsGrouped() {
        assertEquals("980 7806 84", GbVat.INSTANCE.format("980780684"));
        assertEquals("980 7806 84 001", GbVat.INSTANCE.format("980780684001"));
        // institutional numbers are not grouped
        assertEquals("GD001", GbVat.INSTANCE.format("GD001"));
    }

    @Test
    void romanianPersonalCodeExposesItsFields() {
        assertEquals("Cluj", RoCnp.getCounty("1630615123457").orElseThrow());
        assertEquals(1963, RoCnp.getBirthDate("1630615123457").getYear());
        assertEquals(6, RoCnp.getBirthDate("1630615123457").getMonthValue());
        assertEquals(15, RoCnp.getBirthDate("1630615123457").getDayOfMonth());
    }
}
