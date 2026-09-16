package io.github.jefersonsantos06.stdnum.latam;

import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.ValidationException;
import io.github.jefersonsantos06.stdnum.tck.StdNumContractTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NiCedulaTest extends StdNumContractTest {

    @Override
    protected StdNum subject() {
        return NiCedula.INSTANCE;
    }

    @Test
    void theCheckLetterIsTheThirteenDigitsModuloTwentyThree() {
        // the two examples the rule was confirmed against, whose letters fall
        // at either end of the alphabet
        assertEquals('W', NiCedula.calcCheckLetter("6011506790002"));
        assertEquals('F', NiCedula.calcCheckLetter("6071904680001"));
        // thirteen digits overflow an int, so a remainder taken in one go
        // would be wrong here rather than merely imprecise
        assertEquals('M', NiCedula.calcCheckLetter("6010101000001"));
    }

    @Test
    void theCalculatorRefusesWhatIsNotThirteenDigits() {
        for (String garbage : new String[] {"AAAAAAAAAAAAA", ".............", "",
                                            "601150679000", "60115067900021"}) {
            assertThrows(ValidationException.class,
                    () -> NiCedula.calcCheckLetter(garbage), garbage);
        }
        assertThrows(ValidationException.class, () -> NiCedula.calcCheckLetter(null));
    }
}
