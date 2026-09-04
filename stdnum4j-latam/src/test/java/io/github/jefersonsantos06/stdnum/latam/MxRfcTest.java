package io.github.jefersonsantos06.stdnum.latam;

import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidComponentException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.tck.StdNumContractTest;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MxRfcTest extends StdNumContractTest {

    @Override
    protected StdNum subject() {
        return MxRfc.INSTANCE;
    }

    @Test
    void theCheckCharacterIsOnlyVerifiedOnRequest() {
        // numbers the SAT's own lookup service recognises, whose check
        // character is nonetheless wrong
        for (String issued : new String[] {"AAC0903183F6", "AMC020204AB7", "BER060923LW4"}) {
            assertTrue(MxRfc.INSTANCE.isValid(issued), issued);
            assertFalse(MxRfc.INSTANCE.isValid(issued, true), issued);
        }
    }

    @Test
    void strictValidationNamesTheCheckCharacterAsTheFault() {
        assertEquals("GODE561231GR9", MxRfc.INSTANCE.validate("GODE561231GR9"));
        assertThrows(InvalidChecksumException.class,
                () -> MxRfc.INSTANCE.validate("GODE561231GR9", true));
        assertEquals("GODE561231GR8", MxRfc.INSTANCE.validate("GODE561231GR8", true));
    }

    @Test
    void strictValidationAlsoRejectsAMalformedHomoclave() {
        // 0 opens no homoclave, and the last character is not in its alphabet
        assertEquals("CCM650122I06", MxRfc.INSTANCE.validate("CCM650122I06"));
        assertThrows(InvalidComponentException.class,
                () -> MxRfc.INSTANCE.validate("CCM650122I06", true));
    }

    @Test
    void theDateDigitsMustNameADay() {
        assertThrows(InvalidComponentException.class,
                () -> MxRfc.INSTANCE.validate("ABCD 123456"));
        assertThrows(InvalidComponentException.class,
                () -> MxRfc.INSTANCE.validate("XAXX 010231 000"));
        // the century is not recorded, so 29 February is taken as a real day
        assertEquals("CGB000229SW0", MxRfc.INSTANCE.validate("CGB000229SW0"));
    }

    @Test
    void aPersonalNumberMayNotOpenWithAWordTheSatStrikesOut() {
        assertThrows(InvalidComponentException.class,
                () -> MxRfc.INSTANCE.validate("CACA 580710 NF7"));
        // the rule is for people; a company keeps its three letters
        assertEquals("CAC580710NF7", MxRfc.INSTANCE.validate("CAC 580710 NF7"));
    }

    @Test
    void theDateIsReadAsTheMostRecentYearEndingInThoseDigits() {
        assertEquals(LocalDate.of(1956, 12, 31), MxRfc.getDate("GODE561231GR8"));
        // a personal number without the homoclave keeps the four name letters
        assertEquals(LocalDate.of(1960, 7, 3), MxRfc.getDate("COMG600703"));
        // a company number has three, so its date starts one place earlier
        assertEquals(LocalDate.of(1993, 7, 14), MxRfc.getDate("MAB9307148T4"));
        assertEquals(LocalDate.of(2000, 2, 29), MxRfc.getDate("CGB000229SW0"));
    }

    @Test
    void aPersonalNumberWithoutTheHomoclaveKeepsItsFourNameLetters() {
        assertEquals("GODE 561231", MxRfc.INSTANCE.format("GODE561231"));
        assertEquals("GODE 561231 GR8", MxRfc.INSTANCE.format("GODE561231GR8"));
        assertEquals("MAB 930714 8T4", MxRfc.INSTANCE.format("MAB9307148T4"));
    }
}
