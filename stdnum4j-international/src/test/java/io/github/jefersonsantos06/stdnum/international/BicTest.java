package io.github.jefersonsantos06.stdnum.international;

import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.tck.StdNumContractTest;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BicTest extends StdNumContractTest {

    @Override
    protected StdNum subject() {
        return Bic.INSTANCE;
    }

    @Test
    void exposesTheEmbeddedFields() {
        assertEquals("DE", Bic.countryCode("DEUTDEFF"));
        assertEquals(Optional.of("500"), Bic.branchCode("DEUTDEFF500"));
        assertEquals(Optional.empty(), Bic.branchCode("DEUTDEFF"));
    }
}
