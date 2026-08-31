package io.github.jefersonsantos06.stdnum.br;

import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.tck.StdNumContractTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RenavamTest extends StdNumContractTest {

    @Override
    protected StdNum subject() {
        return Renavam.INSTANCE;
    }

    @Test
    void legacyNineDigitNumbersArePadded() {
        assertEquals("00123456789", Renavam.INSTANCE.compact("123456789"));
        assertEquals("00123456789", Renavam.INSTANCE.validate("123456789"));
    }

    @Test
    void calculatesTheCheckDigit() {
        assertEquals(7, Renavam.calcCheckDigit("0123456789"));
    }
}
