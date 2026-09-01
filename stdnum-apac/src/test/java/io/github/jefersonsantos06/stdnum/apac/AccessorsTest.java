package io.github.jefersonsantos06.stdnum.apac;

import io.github.jefersonsantos06.stdnum.spi.ValidationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Exercises the derived accessors of the Asia-Pacific number types. */
class AccessorsTest {

    @Test
    void companyNumberConvertsToBusinessNumber() {
        String abn = AuAcn.toAbn("004085616");
        assertEquals("53004085616", abn);
        assertTrue(AuAbn.INSTANCE.isValid(abn));
    }

    @Test
    void panIsMaskedToTheCbdtStandard() {
        assertEquals("ACUPAXXXXR", InPan.mask("ACUPA7085R"));
        assertEquals('A', InPan.initial("ACUPA7085R"));
    }

    @Test
    void gstinExposesItsEmbeddedFields() {
        InGstin.Info info = InGstin.info("27ACUPA7085R1ZL");
        assertEquals("27", info.stateCode());
        assertEquals("ACUPA7085R", info.pan());
        assertEquals("A", info.initial());
        assertEquals(1, info.registrationCount());
        assertEquals("ACUPA7085R", InGstin.toPan("27ACUPA7085R1ZL"));
    }

    @Test
    void koreanNumberCanRejectFutureBirthDates() {
        // 971013 is in the past under either switch
        assertEquals("9710139019902", KrRrn.validate("9710139019902", false));
        assertEquals("9710139019902", KrRrn.validate("9710139019902", true));
    }

    @Test
    void israeliNumberIsFormatted() {
        assertEquals("03933742-3", IlIdnr.INSTANCE.format("039337423"));
    }

    @Test
    void thaiPersonalNumberRejectsCompanyPrefixes() {
        // 0 marks a number issued by the Department of Business Development
        assertThrows(ValidationException.class, () -> ThPin.INSTANCE.validate("0105536112014"));
        assertTrue(ThMoa.INSTANCE.isValid("0105536112014"));
    }

    @Test
    void theKindOfThaiNumberIsTheTypeThatAcceptedIt() {
        assertEquals("th.moa", ThTin.kindOf("0-99-4-000-61772-1").descriptor().id());
        assertEquals("th.moa", ThTin.kindOf("0234545678783").descriptor().id());
        assertEquals("th.pin", ThTin.kindOf("1-2345-45678-78-1").descriptor().id());
    }

    @Test
    void aVirtualIdIsOnlyMaskedWhenItIsOne() {
        assertEquals("XXXX XXXX XXXX 2341", InVid.mask("2341234123412341"));
        // python-stdnum masks anything; a number whose check digit is wrong is
        // not a virtual ID, and dressing it up as one helps nobody
        assertThrows(ValidationException.class, () -> InVid.mask("2341234123412342"));
    }
}
