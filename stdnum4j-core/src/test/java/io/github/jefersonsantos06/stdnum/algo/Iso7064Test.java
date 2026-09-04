package io.github.jefersonsantos06.stdnum.algo;

import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Iso7064Test {

    @Test
    void mod11Dash2() {
        assertEquals('0', Iso7064.MOD_11_2.calcCheckDigit("0794"));
        assertEquals("07940", Iso7064.MOD_11_2.validate("07940"));
        // check character can be X
        assertEquals('X', Iso7064.MOD_11_2.calcCheckDigit("079"));
        assertEquals("079X", Iso7064.MOD_11_2.validate("079X"));
        assertEquals(1, Iso7064.MOD_11_2.checksum("079X"));
        assertThrows(InvalidChecksumException.class,
                () -> Iso7064.MOD_11_2.validate("07941"));
    }

    @Test
    void mod37Dash2() {
        assertEquals('Y', Iso7064.MOD_37_2.calcCheckDigit("G123489654321"));
        assertEquals("G123489654321Y", Iso7064.MOD_37_2.validate("G123489654321Y"));
        assertFalse(Iso7064.MOD_37_2.isValid("G123489654321Z"));
    }

    @Test
    void mod11Comma10() {
        assertEquals('3', Iso7064.MOD_11_10.calcCheckDigit("79462"));
        assertEquals("794623", Iso7064.MOD_11_10.validate("794623"));
        assertEquals('5', Iso7064.MOD_11_10.calcCheckDigit("00200667308"));
        assertEquals("002006673085", Iso7064.MOD_11_10.validate("002006673085"));
        assertFalse(Iso7064.MOD_11_10.isValid("794624"));
    }

    @Test
    void mod37Comma36() {
        assertEquals('M', Iso7064.MOD_37_36.calcCheckDigit("A12425GABC1234002"));
        assertEquals("A12425GABC1234002M",
                Iso7064.MOD_37_36.validate("A12425GABC1234002M"));
        assertFalse(Iso7064.MOD_37_36.isValid("A12425GABC1234002N"));
    }

    @Test
    void roundTripAcrossSystems() {
        for (String payload : new String[] {"0", "1", "123456", "999999999"}) {
            assertTrue(Iso7064.MOD_11_2.isValid(
                    payload + Iso7064.MOD_11_2.calcCheckDigit(payload)));
            assertTrue(Iso7064.MOD_11_10.isValid(
                    payload + Iso7064.MOD_11_10.calcCheckDigit(payload)));
            assertTrue(Iso7064.MOD_37_2.isValid(
                    payload + Iso7064.MOD_37_2.calcCheckDigit(payload)));
            assertTrue(Iso7064.MOD_37_36.isValid(
                    payload + Iso7064.MOD_37_36.calcCheckDigit(payload)));
        }
    }

    @Test
    void garbageRejectedAsFormat() {
        assertThrows(InvalidFormatException.class, () -> Iso7064.MOD_11_2.checksum(null));
        assertThrows(InvalidFormatException.class, () -> Iso7064.MOD_11_2.checksum(""));
        assertThrows(InvalidFormatException.class, () -> Iso7064.MOD_11_2.checksum("12a"));
        // lower case is not in the alphabets
        assertThrows(InvalidFormatException.class, () -> Iso7064.MOD_37_36.checksum("abc"));
    }

    @Test
    void constructorsValidateArguments() {
        assertThrows(IllegalArgumentException.class,
                () -> new Iso7064.PureSystem(11, "0123456789"));
        assertThrows(IllegalArgumentException.class,
                () -> new Iso7064.HybridSystem("012"));
    }
}
