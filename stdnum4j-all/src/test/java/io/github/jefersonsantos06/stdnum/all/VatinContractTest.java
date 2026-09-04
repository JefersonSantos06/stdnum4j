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
        assertEquals("BR16727230000197", Vatin.INSTANCE.validate("BR16.727.230/0001-97"));
        assertEquals("GB980780684", Vatin.INSTANCE.validate("GB 980 7806 84"));
    }

    @Test
    void resolvesVatNumbersThatCarryALocalName() {
        // no <cc>.vat id exists for these: they are found by their VAT tag
        assertEquals("FR40303265045", Vatin.INSTANCE.validate("Fr 40 303 265 045"));
        assertEquals("ESB58378431", Vatin.INSTANCE.validate("ES B-58378431"));
        assertEquals("IT00743110157", Vatin.INSTANCE.validate("IT 00743110157"));
        assertEquals("PT501964843", Vatin.INSTANCE.validate("PT 501 964 843"));
    }

    @Test
    void prefixesThatDoNotMatchTheCountryCodeAreRemapped() {
        // Northern Ireland (XI) is validated as Great Britain
        assertEquals("XI980780684", Vatin.INSTANCE.validate("XI980780684"));
        // Greece uses EL instead of GR
        assertEquals("EL094259216", Vatin.INSTANCE.validate("EL 094259216"));
    }

    @Test
    void unsupportedCountriesAreComponentErrors() {
        assertThrows(InvalidComponentException.class,
                () -> Vatin.INSTANCE.validate("JP123456789"));
        assertThrows(InvalidComponentException.class,
                () -> Vatin.INSTANCE.validate("XX123456789"));
    }
}
