package io.github.jefersonsantos06.stdnum.algo;

import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Mod97Test {

    @Test
    void calcAndValidate() {
        assertEquals("90", Mod97.calcCheckDigits("99991234567890121414"));
        assertEquals("9999123456789012141490",
                Mod97.validate("9999123456789012141490"));
        assertEquals("31", Mod97.calcCheckDigits("4354111611551114"));
        assertEquals("08686001256515001121751",
                Mod97.validate("08686001256515001121751"));
    }

    @Test
    void lettersExpandBase36CaseInsensitive() {
        // IBAN-style rearranged strings contain letters
        String payload = "WESTDEUTSCHE123";
        String digits = Mod97.calcCheckDigits(payload);
        assertTrue(Mod97.isValid(payload + digits));
        assertEquals(Mod97.checksum("ab12"), Mod97.checksum("AB12"));
    }

    @Test
    void checkDigitsAlwaysTwoCharacters() {
        for (String payload : new String[] {"1", "12", "9999", "COFFEE", "0"}) {
            String digits = Mod97.calcCheckDigits(payload);
            assertEquals(2, digits.length(), "payload " + payload);
            assertTrue(Mod97.isValid(payload + digits));
        }
    }

    @Test
    void invalidChecksumRejected() {
        assertThrows(InvalidChecksumException.class,
                () -> Mod97.validate("9999123456789012141491"));
        assertFalse(Mod97.isValid("00"));
    }

    @Test
    void garbageRejectedAsFormat() {
        assertThrows(InvalidFormatException.class, () -> Mod97.checksum(null));
        assertThrows(InvalidFormatException.class, () -> Mod97.checksum(""));
        assertThrows(InvalidFormatException.class, () -> Mod97.checksum("12-34"));
    }
}
