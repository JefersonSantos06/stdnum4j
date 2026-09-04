package io.github.jefersonsantos06.stdnum.tck;

import io.github.jefersonsantos06.stdnum.StdNums;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Proves the ServiceLoader wiring end to end: a provider declared in
 * {@code META-INF/services} is discovered by the registry and usable
 * through it.
 */
class RegistryIntegrationTest {

    @Test
    void dummyIsDiscoveredThroughServiceLoader() {
        StdNum dummy = StdNums.byId("zz.dummy").orElseThrow();
        assertEquals("ZZ", dummy.descriptor().countryCode());
        assertEquals("12345674", dummy.validate("1234-5674"));
    }

    @Test
    void dummyIsListedByCountry() {
        assertTrue(StdNums.byCountry("ZZ").stream()
                .anyMatch(n -> n.descriptor().id().equals("zz.dummy")));
    }
}
