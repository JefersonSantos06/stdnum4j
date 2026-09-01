package io.github.jefersonsantos06.stdnum.eu;

import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.tck.StdNumContractTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FrSiretTest extends StdNumContractTest {

    @Override
    protected StdNum subject() {
        return FrSiret.INSTANCE;
    }

    @Test
    void derivesSirenAndTva() {
        assertEquals("732829320", FrSiret.toSiren("732 829 320 00074"));
        assertEquals("44732829320", FrSiret.toTva("73282932000074"));
    }

    @Test
    void formatsInStandardGroups() {
        assertEquals("732 829 320 00074", FrSiret.INSTANCE.format("73282932000074"));
    }
}
