package io.github.jefersonsantos06.stdnum.international;

import io.github.jefersonsantos06.stdnum.spi.InvalidComponentException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.tck.StdNumContractTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MacTest extends StdNumContractTest {

    @Override
    protected StdNum subject() {
        return Mac.INSTANCE;
    }

    @Test
    void anAddressClaimingAManufacturerMustNameOneTheIeeeAssigned() {
        assertThrows(InvalidComponentException.class,
                () -> Mac.INSTANCE.validate("d0:5:9:04:a2:a0"));
        // the same address is well formed, so the check can be waived
        assertEquals("d0:05:09:04:a2:a0",
                Mac.INSTANCE.validate("d0:5:9:04:a2:a0", false));
        assertFalse(Mac.INSTANCE.isValid("fd:ff:ff:84:a2:a0"));
        assertTrue(Mac.INSTANCE.isValid("fd:ff:ff:84:a2:a0", false));
    }

    @Test
    void aLocallyAdministeredAddressClaimsNoManufacturer() {
        // fe has the local bit set, so no block is looked for
        assertEquals("fe:54:00:76:07:0a", Mac.INSTANCE.validate("fe:54:00:76:07:0a"));
        // unless the check is asked for, and then there is none
        assertThrows(InvalidComponentException.class,
                () -> Mac.INSTANCE.validate("fe:54:00:76:07:0a", true));
    }

    @Test
    void theBlockIsAsWideAsTheManufacturerBoughtIt() {
        // a 24-bit block: the whole of D05099 is one company's
        assertEquals("D05099", Mac.oui("d0:50:99:84:a2:a0"));
        assertEquals("84A2A0", Mac.iab("d0:50:99:84:a2:a0"));
        // a 36-bit block: 70B3D5 is subdivided, and the address falls in one
        // of the slices
        assertEquals("70B3D5001", Mac.oui("70:b3:d5:00:1a:bc"));
        assertEquals("ABC", Mac.iab("70:b3:d5:00:1a:bc"));
        assertEquals("SOREDI touch systems GmbH", Mac.manufacturer("70:b3:d5:00:1a:bc"));
    }

    @Test
    void aManufacturerNameCanCarryTheQuoteThatWouldCloseIt() {
        assertEquals("UAB \"Teltonika Telematics\"", Mac.manufacturer("38:8a:21:00:00:01"));
    }

    @Test
    void aSubdividedBlockOnlyCoversTheSlicesItSoldOff() {
        // 70B3D5 belongs to the registry itself; only its slices are assigned
        assertThrows(InvalidComponentException.class,
                () -> Mac.INSTANCE.validate("70:b3:d5:ff:ff:ff"));
    }
}
