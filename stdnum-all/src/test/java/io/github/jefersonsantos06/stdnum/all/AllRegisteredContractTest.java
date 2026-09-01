package io.github.jefersonsantos06.stdnum.all;

import io.github.jefersonsantos06.stdnum.StdNums;
import io.github.jefersonsantos06.stdnum.spi.Check;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.ValidationException;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        // 7 national numbers + 27 state registrations
        assertEquals(34, StdNums.byCountry("BR").size());
        assertTrue(StdNums.byId("br.cpf").isPresent());
        assertTrue(StdNums.byId("br.ie.sp").isPresent());
        // international types carry no country and are found by id
        for (String id : new String[] {"iban", "isbn", "ean", "isin", "bic", "vatin"}) {
            assertTrue(StdNums.byId(id).isPresent(), id);
            assertTrue(StdNums.byId(id).orElseThrow().descriptor().country().isEmpty(), id);
        }
        assertEquals(4, StdNums.byCountry("ES").size());
        assertEquals(3, StdNums.byCountry("FR").size());
        assertEquals(112, StdNums.all().size());
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
                    for (String garbage : GARBAGE) {
                        assertThrows(ValidationException.class,
                                () -> number.validate(garbage),
                                () -> "garbage accepted by " + number.descriptor().id()
                                        + ": " + garbage);
                        assertFalse(number.isValid(garbage));
                    }
                }));
    }
}
