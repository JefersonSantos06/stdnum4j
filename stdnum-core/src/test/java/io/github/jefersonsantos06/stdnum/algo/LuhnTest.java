package io.github.jefersonsantos06.stdnum.algo;

import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LuhnTest {

    @Test
    void checksumAndCheckDigit() {
        assertEquals(6, Luhn.checksum("7894"));
        assertEquals('9', Luhn.calcCheckDigit("7894"));
        assertEquals("78949", Luhn.validate("78949"));
        assertTrue(Luhn.isValid("78949"));
    }

    @Test
    void invalidChecksumRejected() {
        assertThrows(InvalidChecksumException.class, () -> Luhn.validate("7894"));
        assertFalse(Luhn.isValid("7894"));
    }

    @Test
    void modNWithHexAlphabet() {
        String hex = "0123456789abcdef";
        assertEquals(14, Luhn.checksum("1234", hex));
        assertFalse(Luhn.isValid("1234", hex));
        // appending the computed check digit must validate
        char check = Luhn.calcCheckDigit("1234", hex);
        assertTrue(Luhn.isValid("1234" + check, hex));
    }

    @Test
    void garbageRejectedAsFormat() {
        assertThrows(InvalidFormatException.class, () -> Luhn.checksum(null));
        assertThrows(InvalidFormatException.class, () -> Luhn.checksum(""));
        assertThrows(InvalidFormatException.class, () -> Luhn.checksum("12a4"));
        assertFalse(Luhn.isValid(null));
        assertFalse(Luhn.isValid(""));
    }
}
