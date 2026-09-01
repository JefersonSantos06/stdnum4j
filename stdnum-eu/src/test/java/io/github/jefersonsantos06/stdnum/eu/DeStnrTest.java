package io.github.jefersonsantos06.stdnum.eu;

import io.github.jefersonsantos06.stdnum.spi.InvalidComponentException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.tck.StdNumContractTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DeStnrTest extends StdNumContractTest {

    @Override
    protected StdNum subject() {
        return DeStnr.INSTANCE;
    }

    @Test
    void namingTheLandDecidesHowTheNumberIsGrouped() {
        assertEquals("156/141/14808",
                DeStnr.INSTANCE.format("156 / 141 / 14808", "Thuringen"));
        // eleven digits fit two layouts, and the Land says which
        assertEquals("181/815/08155", DeStnr.INSTANCE.format("18181508155", "Bayern"));
        assertEquals("181/8150/8155",
                DeStnr.INSTANCE.format("18181508155", "Nordrhein-Westfalen"));
    }

    @Test
    void withoutTheLandTheGroupingIsTheFirstLayoutThatFits() {
        assertEquals("21/815/08150", DeStnr.INSTANCE.format("2181508150"));
        assertEquals("181/815/08155", DeStnr.INSTANCE.format("18181508155"));
        assertEquals("101/576/11744", DeStnr.INSTANCE.format("101/5761/1744"));
        // the country-wide form is written unbroken
        assertEquals("2893081508152", DeStnr.INSTANCE.format("2893081508152"));
    }

    @Test
    void aLandThatDoesNotExistIsAComponentError() {
        assertThrows(InvalidComponentException.class,
                () -> DeStnr.INSTANCE.format("18181508155", "Elsass"));
    }
}
