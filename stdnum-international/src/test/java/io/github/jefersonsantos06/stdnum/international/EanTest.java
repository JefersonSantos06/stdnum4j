package io.github.jefersonsantos06.stdnum.international;

import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.tck.StdNumContractTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EanTest extends StdNumContractTest {

    @Override
    protected StdNum subject() {
        return Ean.INSTANCE;
    }

    @Test
    void calculatesCheckDigits() {
        assertEquals('1', Ean.calcCheckDigit("400638133393"));
        assertEquals('4', Ean.calcCheckDigit("9638507"));
    }

    @Test
    void gtin14RoundTrip() {
        String base = "0400638133393";
        String gtin14 = base + Ean.calcCheckDigit(base);
        assertTrue(Ean.INSTANCE.isValid(gtin14));
        assertEquals(14, gtin14.length());
    }
}
