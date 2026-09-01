package io.github.jefersonsantos06.stdnum.eu;

import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.tck.StdNumContractTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FrSirenTest extends StdNumContractTest {

    @Override
    protected StdNum subject() {
        return FrSiren.INSTANCE;
    }

    @Test
    void formatsInGroupsOfThree() {
        assertEquals("404 833 048", FrSiren.INSTANCE.format("404833048"));
    }

    @Test
    void derivesTheTvaNumber() {
        assertEquals("46443121975", FrSiren.toTva("443 121 975"));
    }
}
