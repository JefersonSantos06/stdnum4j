package io.github.jefersonsantos06.stdnum.algo;

import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VerhoeffTest {

    @Test
    void checksumAndCheckDigit() {
        assertEquals(1, Verhoeff.checksum("1234"));
        assertEquals('0', Verhoeff.calcCheckDigit("1234"));
        assertEquals("12340", Verhoeff.validate("12340"));
        assertTrue(Verhoeff.isValid("12340"));
    }

    @Test
    void invalidChecksumRejected() {
        assertThrows(InvalidChecksumException.class, () -> Verhoeff.validate("1234"));
        assertFalse(Verhoeff.isValid("12341"));
    }

    @Test
    void roundTripForSeveralPayloads() {
        for (String payload : new String[]{"0", "42", "236", "1234567", "999999999"}) {
            char check = Verhoeff.calcCheckDigit(payload);
            assertTrue(Verhoeff.isValid(payload + check), "payload " + payload + " with check digit " + check);
        }
    }

    @Test
    void detectsAdjacentTransposition() {
        char check = Verhoeff.calcCheckDigit("1234");
        assertTrue(Verhoeff.isValid("1234" + check));
        assertFalse(Verhoeff.isValid("2134" + check));
    }

    @Test
    void garbageRejectedAsFormat() {
        assertThrows(InvalidFormatException.class, () -> Verhoeff.checksum(null));
        assertThrows(InvalidFormatException.class, () -> Verhoeff.checksum(""));
        assertThrows(InvalidFormatException.class, () -> Verhoeff.checksum("12x0"));
    }
}
