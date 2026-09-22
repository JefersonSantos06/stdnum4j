package io.github.jefersonsantos06.stdnum.na;

import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.tck.StdNumContractTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UsTinTest extends StdNumContractTest {

    @Override
    protected StdNum subject() {
        return UsTin.INSTANCE;
    }

    /**
     * The inherited safeFormat has to cross the dispatch: this type's format
     * asks the kind that accepted the number to write it.
     */
    @Test
    void safeFormatCrossesTheDispatchToTheKindThatAccepts() {
        assertEquals("042-10-3594", UsTin.INSTANCE.safeFormat("042103594"));
        assertEquals("12345", UsTin.INSTANCE.safeFormat("12345"));
    }
}
