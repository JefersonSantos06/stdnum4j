package io.github.jefersonsantos06.stdnum.text;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MaskTest {

    @Test
    void countsSlotsAndIgnoresSeparators() {
        assertEquals(9, Mask.of("##.###.###-#").slots());
        assertEquals(6, Mask.of("A99 9AA").slots());
        assertEquals(0, Mask.of("").slots());
    }

    @Test
    void anySlotTakesAnything() {
        Mask mask = Mask.of("#-########.#/###");
        assertTrue(mask.matches("P011004243002"));
        assertEquals("P-01100424.3/002", mask.fill("P011004243002"));
    }

    @Test
    void typedSlotsTellShapesOfOneLengthApart() {
        Mask outward = Mask.of("A99 9AA");
        Mask forces = Mask.of("AAAA 99");
        assertTrue(outward.matches("M344AB"));
        assertFalse(outward.matches("BFPO61"));
        assertFalse(forces.matches("M344AB"));
        assertTrue(forces.matches("BFPO61"));
        assertEquals("BFPO 61", Mask.apply(List.of(outward, forces), "BFPO61"));
        assertEquals("M34 4AB", Mask.apply(List.of(outward, forces), "M344AB"));
    }

    @Test
    void lengthAloneDecidesForUntypedMasks() {
        List<Mask> masks = List.of(Mask.of("######-##"), Mask.of("#######-##"));
        assertEquals("123456-63", Mask.apply(masks, "12345663"));
        assertEquals("1000003-06", Mask.apply(masks, "100000306"));
    }

    @Test
    void lengthIsTheFallbackWhenNoShapeFits() {
        // one Eircode example stands for every routing key and identifier
        List<Mask> masks = List.of(Mask.of("A99 A9A9"));
        assertEquals("A65 F4E2", Mask.apply(masks, "A65F4E2"));
        assertEquals("D02 X285", Mask.apply(masks, "D02X285"));
    }

    @Test
    void aNumberNoMaskDescribesIsItsOwnPresentation() {
        assertEquals("NXR123", Mask.apply(List.of(Mask.of("AAA 99"), Mask.of("AAA 9999")), "NXR123"));
        assertEquals("240000048", Mask.apply(List.of(), "240000048"));
    }

    @Test
    void fillRefusesTheWrongLength() {
        assertThrows(IllegalArgumentException.class, () -> Mask.of("###-####").fill("12"));
    }

    @Test
    void aTemplateHoldsNoLiteralLettersOrDigits() {
        assertThrows(IllegalArgumentException.class, () -> Mask.of("BFPO ##"));
        assertThrows(IllegalArgumentException.class, () -> Mask.of("##-1"));
    }
}
