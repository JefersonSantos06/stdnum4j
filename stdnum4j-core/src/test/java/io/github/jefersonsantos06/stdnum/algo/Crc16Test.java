package io.github.jefersonsantos06.stdnum.algo;

import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class Crc16Test {

    @Test
    void standardCheckVector() {
        // the CRC-16/CCITT-FALSE check value for "123456789" is 0x29B1
        assertEquals(0x29B1, Crc16.checksum("123456789"));
        assertEquals("29B1", Crc16.hex("123456789"));
    }

    @Test
    void emptyInputIsTheInitialValue() {
        assertEquals(0xFFFF, Crc16.checksum(""));
    }

    @Test
    void resultAlwaysFitsSixteenBits() {
        for (String text : new String[] {"A", "pix", "0002012658", "ção"}) {
            int crc = Crc16.checksum(text);
            assertEquals(crc, crc & 0xFFFF, text);
            assertEquals(4, Crc16.hex(text).length());
        }
    }

    @Test
    void nullIsRejected() {
        assertThrows(InvalidFormatException.class, () -> Crc16.checksum((String) null));
    }
}
