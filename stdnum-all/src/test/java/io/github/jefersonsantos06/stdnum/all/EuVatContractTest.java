package io.github.jefersonsantos06.stdnum.all;

import io.github.jefersonsantos06.stdnum.international.EuVat;
import io.github.jefersonsantos06.stdnum.spi.InvalidComponentException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.tck.StdNumContractTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Exercises the EU VAT dispatcher with every country module on the
 * classpath — which is why this test lives in the aggregator module.
 */
class EuVatContractTest extends StdNumContractTest {

    @Override
    protected StdNum subject() {
        return EuVat.INSTANCE;
    }

    @Test
    void acceptsTheTwoPrefixesThatAreNotIsoCountryCodes() {
        // Greece writes EL, and Northern Ireland kept XI after Brexit
        assertEquals("EL094259216", EuVat.INSTANCE.validate("EL 094259216"));
        assertEquals("XI980780684", EuVat.INSTANCE.validate("XI980780684"));
    }

    @Test
    void oneStopShopNumbersCarryTheirOwnPrefix() {
        assertEquals("EU372022452", EuVat.INSTANCE.validate("EU 372022452"));
        assertEquals("IM3720224521", EuVat.INSTANCE.validate("IM3720224521"));
    }

    @Test
    void countriesOutsideTheUnionAreComponentErrors() {
        // Great Britain left; only Northern Ireland stayed in for goods
        assertThrows(InvalidComponentException.class,
                () -> EuVat.INSTANCE.validate("GB980780684"));
        assertThrows(InvalidComponentException.class,
                () -> EuVat.INSTANCE.validate("CHE-107.787.577 IVA"));
        assertThrows(InvalidComponentException.class,
                () -> EuVat.INSTANCE.validate("XX123456789"));
    }
}
