package io.github.jefersonsantos06.stdnum.eu;

import io.github.jefersonsantos06.stdnum.spi.InvalidComponentException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.tck.StdNumContractTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FrTvaTest extends StdNumContractTest {

    @Override
    protected StdNum subject() {
        return FrTva.INSTANCE;
    }

    @Test
    void extractsTheSiren() {
        assertEquals("303265045", FrTva.toSiren("Fr 40 303 265 045"));
    }

    @Test
    void monacoNumbersCarryNoSiren() {
        // craft a Monaco-style number (body starting 000) with its numeric key
        String body = "000123456";
        long key = Long.parseLong(body + "12") % 97;
        String number = (key < 10 ? "0" : "") + key + body;
        assertEquals(number, FrTva.INSTANCE.validate(number));
        assertThrows(InvalidComponentException.class, () -> FrTva.toSiren(number));
    }
}
