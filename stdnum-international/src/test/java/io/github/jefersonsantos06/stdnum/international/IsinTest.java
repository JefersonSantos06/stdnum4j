package io.github.jefersonsantos06.stdnum.international;

import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.tck.StdNumContractTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IsinTest extends StdNumContractTest {

    @Override
    protected StdNum subject() {
        return Isin.INSTANCE;
    }

    @Test
    void calculatesTheCheckDigit() {
        assertEquals('5', Isin.calcCheckDigit("US037833100"));
        assertEquals('7', Isin.calcCheckDigit("DE000BAY001"));
    }

    @Test
    void lowerCaseIsNormalised() {
        assertEquals("US0378331005", Isin.INSTANCE.validate("us0378331005"));
    }
}
