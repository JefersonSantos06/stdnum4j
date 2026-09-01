package io.github.jefersonsantos06.stdnum.all;

import io.github.jefersonsantos06.stdnum.international.Iban;
import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidComponentException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.tck.StdNumContractTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Exercises the generic IBAN validator with the country types on the
 * classpath — which is why this test lives in the aggregator module. Several
 * countries give the account part a check of their own that the registered
 * BBAN structure cannot express, and the generic validator hands the number
 * on to them.
 */
class IbanContractTest extends StdNumContractTest {

    @Override
    protected StdNum subject() {
        return Iban.INSTANCE;
    }

    @Test
    void theSpanishAccountCheckIsAppliedThroughTheGenericValidator() {
        // the two CCC check digits are wrong; nothing but the Spanish rule
        // can see that
        assertThrows(InvalidChecksumException.class,
                () -> Iban.INSTANCE.validate("ES2121000418450200051331"));
        // a Spanish IBAN whose account part does pass its own rule
        assertTrue(Iban.INSTANCE.isValid("ES7712341234161234567890"));
    }

    @Test
    void theCountryRuleCanBeAskedFor() {
        // false skips the country's own type, not the registry or the BBAN
        assertEquals("ES2121000418450200051331",
                Iban.INSTANCE.validate("ES2121000418450200051331", false));
        assertThrows(InvalidChecksumException.class,
                () -> Iban.INSTANCE.validate("ES2121000418450200051331", true));
    }

    @Test
    void aBankCodeNoInstitutionHoldsIsRejected() {
        // 138 is one of the ranges the National Bank of Belgium lists as
        // unavailable, so no account can be held under it
        assertThrows(InvalidComponentException.class,
                () -> Iban.INSTANCE.validate("BE83138811735115"));
    }
}
