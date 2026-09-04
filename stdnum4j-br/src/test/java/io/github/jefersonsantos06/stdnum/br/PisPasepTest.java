package io.github.jefersonsantos06.stdnum.br;

import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.tck.StdNumContractTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PisPasepTest extends StdNumContractTest {

    @Override
    protected StdNum subject() {
        return PisPasep.INSTANCE;
    }

    @Test
    void formatsWithTheStandardMask() {
        assertEquals("120.12345.67-2", PisPasep.INSTANCE.format("12012345672"));
    }

    @Test
    void calculatesTheCheckDigit() {
        assertEquals(2, PisPasep.calcCheckDigit("1201234567"));
        assertEquals(0, PisPasep.calcCheckDigit("1234567890"));
    }
}
