package io.github.jefersonsantos06.stdnum.br;

import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.tck.StdNumContractTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TituloEleitorTest extends StdNumContractTest {

    @Override
    protected StdNum subject() {
        return TituloEleitor.INSTANCE;
    }

    @Test
    void formatsInGroupsOfFour() {
        assertEquals("1023 8501 0671", TituloEleitor.INSTANCE.format("102385010671"));
    }

    @Test
    void leadingZerosAreRestored() {
        // stored as a number, the leading zero is often dropped
        assertEquals("012345678901",
                TituloEleitor.INSTANCE.compact("12345678901"));
    }

    @Test
    void saoPauloAndMinasUseOneWhenRemainderIsZero() {
        // sequence 00000000 has weighted sum 0: dv1 must be 1 for MG (02)...
        assertTrue(TituloEleitor.INSTANCE.isValid("000000000213"));
        // ...and would be 0 elsewhere, so the same digits under RJ (03) fail
        assertFalse(TituloEleitor.INSTANCE.isValid("000000000313"));
    }
}
