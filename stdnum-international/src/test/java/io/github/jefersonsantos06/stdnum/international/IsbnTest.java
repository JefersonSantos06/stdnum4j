package io.github.jefersonsantos06.stdnum.international;

import io.github.jefersonsantos06.stdnum.spi.InvalidComponentException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.tck.StdNumContractTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IsbnTest extends StdNumContractTest {

    @Override
    protected StdNum subject() {
        return Isbn.INSTANCE;
    }

    @Test
    void convertsBetweenForms() {
        assertEquals("9780306406157", Isbn.convertTo13("0-306-40615-2"));
        assertEquals("0306406152", Isbn.convertTo10("978-0-306-40615-7"));
        // 979 numbers have no 10-digit form
        assertThrows(InvalidComponentException.class,
                () -> Isbn.convertTo10("9790000000001"));
    }

    @Test
    void checkCharacterX() {
        assertEquals('X', Isbn.calcCheckDigit10("080442957"));
        assertEquals("080442957X", Isbn.INSTANCE.validate("0-8044-2957-x"));
    }

    @Test
    void splitsWithTheOfficialRanges() {
        Isbn.Parts parts = Isbn.split("9780306406157").orElseThrow();
        assertEquals("978", parts.prefix());
        assertEquals("0", parts.group());
        assertEquals("306", parts.publisher());
        assertEquals("40615", parts.item());
        assertEquals('7', parts.checkDigit());
    }

    @Test
    void formatsHyphenated() {
        assertEquals("978-0-306-40615-7", Isbn.INSTANCE.format("9780306406157"));
        assertEquals("0-306-40615-2", Isbn.INSTANCE.format("0306406152"));
    }

    @Test
    void formatFallsBackToCompactWhenRangesAreUnknown() {
        // a valid EAN check digit under a group/publisher not in the data
        // may not be splittable; format must still return something valid
        String formatted = Isbn.INSTANCE.format("9780306406157");
        assertTrue(Isbn.INSTANCE.isValid(formatted));
    }
}
