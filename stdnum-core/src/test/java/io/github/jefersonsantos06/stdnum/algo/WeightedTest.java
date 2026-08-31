package io.github.jefersonsantos06.stdnum.algo;

import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WeightedTest {

    @Test
    void descendingBuildsCpfStyleWeights() {
        assertArrayEquals(new int[]{10, 9, 8, 7, 6, 5, 4, 3, 2},
                Weighted.descending(10, 9));
        assertArrayEquals(new int[]{11, 10, 9, 8, 7, 6, 5, 4, 3, 2},
                Weighted.descending(11, 10));
    }

    @Test
    void cyclicFillsRightToLeft() {
        // CNPJ first check digit
        assertArrayEquals(new int[]{5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2}, Weighted.cyclic(12, 2, 3, 4, 5, 6, 7, 8, 9));
        // CNPJ second check digit
        assertArrayEquals(new int[]{6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2}, Weighted.cyclic(13, 2, 3, 4, 5, 6, 7, 8, 9));
        // PIS / RENAVAM
        assertArrayEquals(new int[]{3, 2, 9, 8, 7, 6, 5, 4, 3, 2}, Weighted.cyclic(10, 2, 3, 4, 5, 6, 7, 8, 9));
    }

    @Test
    void cpfCheckDigitsFromRealNumber() {
        // CPF 390.533.447-05
        assertEquals(221, Weighted.weightedSum("390533447", Weighted.descending(10, 9)));
        assertEquals(0, Weighted.mod11(221));
        assertEquals(5, Weighted.mod11CheckDigit("3905334470", Weighted.descending(11, 10)));
    }

    @Test
    void cnpjCheckDigitsFromRealNumber() {
        // CNPJ 16.727.230/0001-97
        int[] w1 = Weighted.cyclic(12, 2, 3, 4, 5, 6, 7, 8, 9);
        int[] w2 = Weighted.cyclic(13, 2, 3, 4, 5, 6, 7, 8, 9);
        assertEquals(9, Weighted.mod11CheckDigit("167272300001", w1));
        assertEquals(7, Weighted.mod11CheckDigit("1672723000019", w2));
    }

    @Test
    void alphanumericCnpjCheckDigits() {
        // Alphanumeric CNPJ 12.ABC.345/01DE-35 (format valid from July 2026):
        // letters take value ASCII - 48 (A=17 ... Z=42)
        int[] w1 = Weighted.cyclic(12, 2, 3, 4, 5, 6, 7, 8, 9);
        int[] w2 = Weighted.cyclic(13, 2, 3, 4, 5, 6, 7, 8, 9);
        assertEquals(3, Weighted.mod11CheckDigit("12ABC34501DE", w1));
        assertEquals(5, Weighted.mod11CheckDigit("12ABC34501DE3", w2));
    }

    @Test
    void mod11IsSafeForAnySum() {
        // the naive Java formula (11 - sum) % 11 goes negative for sum=221;
        // the safe formula must yield 0
        assertEquals(0, Weighted.mod11(221));
        // remainder 0 -> digit 0
        assertEquals(0, Weighted.mod11(110));
        // remainder 10 -> 11 - 10 = 1
        assertEquals(1, Weighted.mod11(109));
        // remainder 1 -> 11 - 1 = 10 -> maps to 0
        assertEquals(0, Weighted.mod11(100));
    }

    @Test
    void weightedSumRejectsBadInput() {
        int[] weights = Weighted.descending(10, 9);
        assertThrows(InvalidFormatException.class,
                () -> Weighted.weightedSum(null, weights));
        assertThrows(InvalidLengthException.class,
                () -> Weighted.weightedSum("123", weights));
        assertThrows(InvalidFormatException.class,
                () -> Weighted.weightedSum("39053344a", weights));
    }

    @Test
    void weightFactoriesRejectBadArguments() {
        assertThrows(IllegalArgumentException.class, () -> Weighted.descending(3, 5));
        assertThrows(IllegalArgumentException.class, () -> Weighted.descending(10, 0));
        assertThrows(IllegalArgumentException.class, () -> Weighted.cyclic(0, 2, 3));
        assertThrows(IllegalArgumentException.class, () -> Weighted.cyclic(5));
    }
}
