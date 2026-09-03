package io.github.jefersonsantos06.stdnum.latam;

import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.ValidationException;
import io.github.jefersonsantos06.stdnum.tck.StdNumContractTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class MxCurpTest extends StdNumContractTest {

    @Override
    protected StdNum subject() {
        return MxCurp.INSTANCE;
    }

    @Test
    void accessorsRefuseGarbageOfTheRightLength() {
        // the contract only reaches validate(); an accessor called directly on
        // eighteen characters that are not a CURP used to throw
        // NumberFormatException out of getBirthDate
        for (String garbage : new String[] {"AAAAAAAAAAAAAAAAAA", "..................",
                                            "AAAA......AAAAAAAA"}) {
            assertThrows(ValidationException.class, () -> MxCurp.getBirthDate(garbage), garbage);
            assertThrows(ValidationException.class, () -> MxCurp.getGender(garbage), garbage);
        }
        // calcCheckDigit is a calculator, and A is a character a CURP may hold
        assertThrows(ValidationException.class, () -> MxCurp.calcCheckDigit(".................."));
    }
}
