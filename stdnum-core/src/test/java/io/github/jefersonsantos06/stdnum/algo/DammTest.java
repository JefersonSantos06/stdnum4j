package io.github.jefersonsantos06.stdnum.algo;

import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DammTest {

    @Test
    void checksumAndCheckDigit() {
        assertEquals(4, Damm.checksum("572"));
        assertEquals('4', Damm.calcCheckDigit("572"));
        assertEquals("5724", Damm.validate("5724"));
        assertTrue(Damm.isValid("5724"));
    }

    @Test
    void invalidChecksumRejected() {
        assertThrows(InvalidChecksumException.class, () -> Damm.validate("572"));
        assertFalse(Damm.isValid("5720"));
    }

    @Test
    void detectsSingleDigitErrorsAndAdjacentTranspositions() {
        String valid = "5724";
        // single digit error
        assertFalse(Damm.isValid("5734"));
        // adjacent transposition
        assertFalse(Damm.isValid("7524"));
        assertTrue(Damm.isValid(valid));
    }

    @Test
    void garbageRejectedAsFormat() {
        assertThrows(InvalidFormatException.class, () -> Damm.checksum(null));
        assertThrows(InvalidFormatException.class, () -> Damm.checksum(""));
        assertThrows(InvalidFormatException.class, () -> Damm.checksum("57a2"));
    }
}
