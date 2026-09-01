package io.github.jefersonsantos06.stdnum.all;

import io.github.jefersonsantos06.stdnum.international.EuExcise;
import io.github.jefersonsantos06.stdnum.spi.InvalidComponentException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.tck.StdNumContractTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Exercises the excise number with every country module on the classpath —
 * which is why this test lives in the aggregator module.
 */
class EuExciseContractTest extends StdNumContractTest {

    @Override
    protected StdNum subject() {
        return EuExcise.INSTANCE;
    }

    @Test
    void aShortSerialIsPaddedRatherThanRejected() {
        assertEquals("LU00000987ABC", EuExcise.INSTANCE.validate("LU 987ABC"));
    }

    @Test
    void countriesOutsideTheUnionAreComponentErrors() {
        assertThrows(InvalidComponentException.class,
                () -> EuExcise.INSTANCE.validate("GB12345678901"));
        assertThrows(InvalidComponentException.class,
                () -> EuExcise.INSTANCE.validate("XX12345678901"));
    }
}
