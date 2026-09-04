package io.github.jefersonsantos06.stdnum.tck;

import io.github.jefersonsantos06.stdnum.spi.StdNum;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Runs the full contract against {@link DummyNumber}, proving the TCK works
 * end to end (fixture loading included) before any real module exists.
 */
class DummyNumberContractTest extends StdNumContractTest {

    @Override
    protected StdNum subject() {
        return DummyNumber.INSTANCE;
    }

    @Test
    void formatsWithHyphen() {
        assertEquals("1234-5674", DummyNumber.INSTANCE.format(" 12345674 "));
    }
}
