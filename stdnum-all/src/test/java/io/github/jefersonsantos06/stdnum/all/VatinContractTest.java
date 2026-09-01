package io.github.jefersonsantos06.stdnum.all;

import io.github.jefersonsantos06.stdnum.international.Vatin;
import io.github.jefersonsantos06.stdnum.spi.InvalidComponentException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.tck.StdNumContractTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Exercises the VATIN dispatcher with every country module on the
 * classpath — which is why this test lives in the aggregator module.
 */
class VatinContractTest extends StdNumContractTest {

    @Override
    protected StdNum subject() {
        return Vatin.INSTANCE;
    }

    @Test
    void delegatesToTheCountryModules() {
        assertEquals("DE136695976", Vatin.INSTANCE.validate("DE 136,695 976"));
        assertEquals("FR40303265045", Vatin.INSTANCE.validate("Fr 40 303 265 045"));
        assertEquals("BR16727230000197", Vatin.INSTANCE.validate("BR16.727.230/0001-97"));
        assertEquals("ESB58378431", Vatin.INSTANCE.validate("ES B-58378431"));
    }

    @Test
    void unsupportedCountriesAreComponentErrors() {
        assertThrows(InvalidComponentException.class,
                () -> Vatin.INSTANCE.validate("GB123456789"));
        // Greece's EL prefix maps to GR, which has no module yet
        assertThrows(InvalidComponentException.class,
                () -> Vatin.INSTANCE.validate("EL123456789"));
    }
}
