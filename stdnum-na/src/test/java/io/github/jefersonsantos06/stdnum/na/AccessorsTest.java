package io.github.jefersonsantos06.stdnum.na;

import io.github.jefersonsantos06.stdnum.spi.ValidationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Exercises the accessors and separator rules of the US number types. */
class AccessorsTest {

    @Test
    void einExposesTheIssuingCampus() {
        assertEquals("Philadelphia", UsEin.getCampus("911144442").orElseThrow());
        assertEquals("Brookhaven", UsEin.getCampus("042103594").orElseThrow());
    }

    @Test
    void unassignedEinPrefixesAreRejected() {
        // 07, 08, 09 and 89 are not among the prefixes the IRS assigns
        assertThrows(ValidationException.class, () -> UsEin.INSTANCE.validate("071144442"));
        assertThrows(ValidationException.class, () -> UsEin.INSTANCE.validate("891144442"));
    }

    @Test
    void separatorsMustSitInTheirDocumentedPosition() {
        // the canonical spellings are accepted
        assertEquals("911144442", UsEin.INSTANCE.validate("91-1144442"));
        assertEquals("536904399", UsSsn.INSTANCE.validate("536-90-4399"));
        assertEquals("912903456", UsItin.INSTANCE.validate("912-90-3456"));
        // either separator may be omitted, so this is still canonical
        assertEquals("912903456", UsItin.INSTANCE.validate("91290-3456"));
        // a hyphen anywhere else is not
        assertFalse(UsEin.INSTANCE.isValid("911-144442"));
        assertFalse(UsSsn.INSTANCE.isValid("53-690-4399"));
        assertFalse(UsItin.INSTANCE.isValid("9129-03456"));
    }
}
