package io.github.jefersonsantos06.stdnum.all;

import io.github.jefersonsantos06.stdnum.StdNums;
import io.github.jefersonsantos06.stdnum.spi.Check;
import io.github.jefersonsantos06.stdnum.spi.ValidationException;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Sweeps every number type discovered through the registry with the generic
 * robustness checks, without knowing which types exist. A module that
 * registers a fragile validator fails here even if it ships no tests.
 */
class AllRegisteredContractTest {

    private static final List<String> GARBAGE = List.of(
            "", "   ", "\t \n", "!!!", "%%%", "----", "abc😀def",
            "9".repeat(1024));

    @Test
    void registryIsPopulated() {
        // 10 national numbers, 27 state registrations and the postal code
        assertEquals(38, StdNums.byCountry("BR").size());
        assertTrue(StdNums.byId("br.cpf").isPresent());
        assertTrue(StdNums.byId("br.ie.sp").isPresent());
        // international types carry no country and are found by id
        for (String id : new String[] {"iban", "isbn", "ean", "isin", "bic", "vatin",
                "imei", "lei", "issn", "ismn", "imo", "cusip", "casrn"}) {
            assertTrue(StdNums.byId(id).isPresent(), id);
            assertTrue(StdNums.byId(id).orElseThrow().descriptor().country().isEmpty(), id);
        }
        assertEquals(10, StdNums.byCountry("ES").size());
        assertEquals(8, StdNums.byCountry("FR").size());
        // 289 hand-written types and 178 generic postal codes
        assertEquals(467, StdNums.all().size());
    }

    @TestFactory
    Stream<DynamicTest> descriptorsAreWellFormed() {
        return StdNums.all().stream().map(number ->
                DynamicTest.dynamicTest(number.descriptor().id(), () -> {
                    assertFalse(number.descriptor().id().isBlank());
                    assertFalse(number.descriptor().shortName().isBlank());
                    assertFalse(number.descriptor().title().isBlank());
                }));
    }

    @TestFactory
    Stream<DynamicTest> nullAndGarbageNeverEscapeValidationException() {
        return StdNums.all().stream().map(number ->
                DynamicTest.dynamicTest(number.descriptor().id(), () -> {
                    assertThrows(ValidationException.class, () -> number.validate(null));
                    assertFalse(number.isValid(null));
                    assertInstanceOf(Check.Invalid.class, number.check(null));
                    assertThrows(ValidationException.class, () -> number.format(null));
                    assertNull(number.safeFormat(null),
                            () -> "safeFormat(null) escaped in " + number.descriptor().id());
                    for (String garbage : GARBAGE) {
                        assertThrows(ValidationException.class,
                                () -> number.validate(garbage),
                                () -> "garbage accepted by " + number.descriptor().id()
                                        + ": " + garbage);
                        assertFalse(number.isValid(garbage));
                        // safeFormat is the only call that may be aimed at
                        // arbitrary input without a try, so it is the only way
                        // to sweep every type down the formatting path at all
                        assertEquals(garbage, number.safeFormat(garbage),
                                () -> "garbage dressed up by " + number.descriptor().id()
                                        + ": " + garbage);
                    }
                }));
    }
}
