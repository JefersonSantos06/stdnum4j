package io.github.jefersonsantos06.stdnum;

import io.github.jefersonsantos06.stdnum.spi.Check;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.spi.ValidationError;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StdNumsTest {

    @Test
    void discoversProvidersFromServiceLoader() {
        assertTrue(StdNums.all().size() >= 2);
        assertTrue(StdNums.byId("xx.alpha").isPresent());
        assertTrue(StdNums.byId("xx.beta").isPresent());
    }

    @Test
    void byIdIsCaseInsensitiveAndTrimmed() {
        assertTrue(StdNums.byId("  XX.Alpha  ").isPresent());
        assertTrue(StdNums.byId("unknown.number").isEmpty());
        assertTrue(StdNums.byId(null).isEmpty());
    }

    @Test
    void byCountryListsAllTypesOfACountry() {
        List<StdNum> xx = StdNums.byCountry("XX");
        assertEquals(2, xx.size());
        assertEquals(List.of(), StdNums.byCountry("YY"));
        assertEquals(List.of(), StdNums.byCountry(null));
    }

    @Test
    void byCountryAndNameResolvesTheId() {
        Optional<StdNum> alpha = StdNums.byCountry("XX", "alpha");
        assertTrue(alpha.isPresent());
        assertEquals("xx.alpha", alpha.get().descriptor().id());
        assertTrue(StdNums.byCountry("XX", "gamma").isEmpty());
        assertTrue(StdNums.byCountry(null, "alpha").isEmpty());
    }

    @Test
    void byTagFilters() {
        assertTrue(StdNums.byTag(Tag.TAX).stream()
                .anyMatch(n -> n.descriptor().id().equals("xx.alpha")));
        assertFalse(StdNums.byTag(Tag.COMPANY).stream()
                .anyMatch(n -> n.descriptor().id().equals("xx.alpha")));
        assertEquals(List.of(), StdNums.byTag(null));
    }

    @Test
    void registeredTypesBehaveThroughTheContract() {
        StdNum alpha = StdNums.byId("xx.alpha").orElseThrow();
        assertEquals("1234", alpha.validate(" 12 34 "));
        assertTrue(alpha.isValid("1234"));
        assertFalse(alpha.isValid("12345"));

        Check valid = alpha.check("1234");
        assertInstanceOf(Check.Valid.class, valid);
        assertEquals("1234", ((Check.Valid) valid).compact());

        Check invalid = alpha.check("123");
        assertInstanceOf(Check.Invalid.class, invalid);
        assertEquals(ValidationError.INVALID_LENGTH, ((Check.Invalid) invalid).error());

        StdNum beta = StdNums.byId("xx.beta").orElseThrow();
        Check component = beta.check("123456");
        assertInstanceOf(Check.Invalid.class, component);
        assertEquals(ValidationError.INVALID_COMPONENT, ((Check.Invalid) component).error());
    }
}
