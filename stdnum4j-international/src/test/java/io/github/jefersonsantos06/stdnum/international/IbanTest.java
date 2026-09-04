package io.github.jefersonsantos06.stdnum.international;

import io.github.jefersonsantos06.stdnum.algo.Mod97;
import io.github.jefersonsantos06.stdnum.spi.InvalidComponentException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.tck.StdNumContractTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class IbanTest extends StdNumContractTest {

    @Override
    protected StdNum subject() {
        return Iban.INSTANCE;
    }

    @Test
    void formatsInGroupsOfFour() {
        assertEquals("GB29 NWBK 6016 1331 9268 19",
                Iban.INSTANCE.format("GB29NWBK60161331926819"));
    }

    @Test
    void calculatesCheckDigits() {
        // check digit positions of the input are ignored
        assertEquals("31", Iban.calcCheckDigits("BExx435411161155"));
        assertEquals("29", Iban.calcCheckDigits("GB00NWBK60161331926819"));
    }

    @Test
    void unknownCountryWithValidChecksumIsAComponentError() {
        // craft a mod-97-valid IBAN for a country that is not in the registry
        String bban = "12345678";
        String digits = Mod97.calcCheckDigits(bban + "ZZ");
        String iban = "ZZ" + digits + bban;
        assertThrows(InvalidComponentException.class, () -> Iban.INSTANCE.validate(iban));
    }

    @Test
    void bbanStructureIsEnforcedPerCountry() {
        // Belgium requires 12 digits; craft a mod-97-valid Belgian IBAN with letters
        String bban = "ABC411161155";
        String digits = Mod97.calcCheckDigits(bban + "BE");
        String iban = "BE" + digits + bban;
        assertThrows(InvalidFormatException.class, () -> Iban.INSTANCE.validate(iban));
    }
}
