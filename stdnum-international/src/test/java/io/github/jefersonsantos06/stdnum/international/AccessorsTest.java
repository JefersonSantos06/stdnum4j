package io.github.jefersonsantos06.stdnum.international;

import io.github.jefersonsantos06.stdnum.spi.ValidationException;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Exercises the derived accessors of the international number types. */
class AccessorsTest {

    @Test
    void imeiSplitsIntoItsThreeParts() {
        Imei.Parts p = Imei.split("354178036859789");
        assertEquals("35417803", p.typeAllocationCode());
        assertEquals("685978", p.serialNumber());
        assertEquals("9", p.tail());
        // the IMEISV form carries a software version where the check digit sits
        assertEquals("20", Imei.split("3568680000414120").tail());
        // the 14-digit form has no tail at all
        assertEquals("", Imei.split("35686800004141").tail());
    }

    @Test
    void isbnTypeDistinguishesTheTwoForms() {
        assertEquals(Optional.of(Isbn.Type.ISBN10), Isbn.isbnType("0306406152"));
        assertEquals(Optional.of(Isbn.Type.ISBN13), Isbn.isbnType("9780306406157"));
        assertEquals(Optional.empty(), Isbn.isbnType("nonsense"));
    }

    @Test
    void isinIsBuiltFromANationalIdentifier() {
        // a SEDOL padded to the nine characters an ISIN reserves
        String isin = Isin.fromNationalId("GB", "B15KXQ8");
        assertEquals("GB00B15KXQ89", isin);
        assertTrue(Isin.INSTANCE.isValid(isin));
        // and the CUSIP conversion agrees with the generic builder
        assertEquals(Cusip.toIsin("91324PAE2", "US"),
                Isin.fromNationalId("US", "91324PAE2"));
    }

    @Test
    void issnConvertsToEanForAGivenIssue() {
        // 977 + the seven significant digits + issue code 00 + the GS1 check
        assertEquals("9770024931000", Issn.toEan("00249319"));
        assertTrue(Ean.INSTANCE.isValid(Issn.toEan("00249319")));
        // the issue code occupies the two digits before the check digit
        assertTrue(Issn.toEan("00249319", "17").startsWith("977002493117"));
        assertTrue(Ean.INSTANCE.isValid(Issn.toEan("00249319", "17")));
        assertThrows(ValidationException.class, () -> Issn.toEan("00249319", "7"));
    }

    @Test
    void ismnSplitsAndKeepsTheFormItWasGiven() {
        Ismn.Parts p = Ismn.split("9790345246805");
        assertEquals("979", p.bookland());
        assertEquals("0", p.prefix());
        assertEquals("3452", p.publisher());
        assertEquals("4680", p.item());
        assertEquals("5", p.checkDigit());
        assertEquals("979-0-3452-4680-5", Ismn.INSTANCE.format("9790345246805"));
        // the legacy form keeps its M and stays ten characters long
        assertEquals("M-3452-4680-5", Ismn.INSTANCE.format("M345246805"));
    }

    @Test
    void macExposesItsSpellingsAndBits() {
        assertEquals("D0-50-99-84-A2-A0", Mac.toEui48("d0:50:99:84:a2:a0"));
        assertEquals("D05099", Mac.oui("d0:50:99:84:a2:a0"));
        assertTrue(Mac.isBroadcast("ff:ff:ff:ff:ff:ff"));
        assertTrue(Mac.isMulticast("ff:ff:ff:ff:ff:ff"));
        assertFalse(Mac.isBroadcast("d0:50:99:84:a2:a0"));
        assertTrue(Mac.isUnicast("d0:50:99:84:a2:a0"));
        // the second-least-significant bit of the first octet marks local scope
        assertTrue(Mac.isLocallyAdministered("02:00:00:00:00:01"));
        assertFalse(Mac.isLocallyAdministered("d0:50:99:84:a2:a0"));
    }
}
