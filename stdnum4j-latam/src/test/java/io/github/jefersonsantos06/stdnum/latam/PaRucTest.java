package io.github.jefersonsantos06.stdnum.latam;

import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.ValidationException;
import io.github.jefersonsantos06.stdnum.tck.StdNumContractTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaRucTest extends StdNumContractTest {

    @Override
    protected StdNum subject() {
        return PaRuc.INSTANCE;
    }

    @Test
    void aFirstFieldNamingAProvinceIsReadBothWays() {
        // the same shape, and each of the two registers claims one of them:
        // a cédula of Panamá province, and tomo 1 of the Public Registry
        assertTrue(PaRuc.INSTANCE.isValid("8-473-515-70"));
        assertTrue(PaRuc.INSTANCE.isValid("1-513-153-21"));
        // which costs something: the other register's digits close too, and
        // nothing in the number says they should not
        assertTrue(PaRuc.INSTANCE.isValid("8-473-515-56"));
        assertTrue(PaRuc.INSTANCE.isValid("1-513-153-63"));
        // a third pair belongs to neither
        assertThrows(ValidationException.class, () -> PaRuc.INSTANCE.validate("8-473-515-71"));
        // above 13 there is no province, so only the tomo is left, and then
        // the cédula's digits are refused like any others
        assertTrue(PaRuc.INSTANCE.isValid("49-572-8631-19"));
        assertThrows(ValidationException.class, () -> PaRuc.INSTANCE.validate("49-572-8631-97"));
    }

    @Test
    void anNtNumberIsReadBothWays() {
        // 8-NT-2-25024 is a person's, 8-NT-1-22684 a company's, and nothing in
        // either number says so
        assertTrue(PaRuc.INSTANCE.isValid("8-NT-2-25024-36"));
        assertTrue(PaRuc.INSTANCE.isValid("8-NT-1-22684-98"));
    }

    @Test
    void checkDigitsAreRefusedWhereTheNumberReadsAsTwoRegisters() {
        assertThrows(ValidationException.class, () -> PaRuc.calcCheckDigits("8-473-515"));
        assertThrows(ValidationException.class, () -> PaRuc.calcCheckDigits("8-NT-1-22684"));
        // but the number that carries its own digits hands them back
        assertEquals("98", PaRuc.calcCheckDigits("8-NT-1-22684-98"));
    }

    @Test
    void theCalculatorRefusesWhatIsNotARuc() {
        for (String garbage : new String[] {"", "----", "8-473", "8-473-515-7", "abc😀def",
                                            "9".repeat(1024)}) {
            assertThrows(ValidationException.class,
                    () -> PaRuc.calcCheckDigits(garbage), garbage);
        }
        assertThrows(ValidationException.class, () -> PaRuc.calcCheckDigits(null));
    }

    @Test
    void aRucWithoutItsDvIsTakenOnItsShapeAlone() {
        // there is no check digit in it to verify, so a wrong field passes
        assertTrue(PaRuc.INSTANCE.isValid("1870951-1-1751"));
        assertTrue(PaRuc.INSTANCE.isValid("1870951-1-1752"));
        // and the presentation is the number as written
        assertEquals("1870951-1-1751-18", PaRuc.INSTANCE.format(" 1870951-1-1751-18 "));
    }
}
