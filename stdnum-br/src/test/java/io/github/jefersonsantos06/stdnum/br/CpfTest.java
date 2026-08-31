package io.github.jefersonsantos06.stdnum.br;

import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.ValidationException;
import io.github.jefersonsantos06.stdnum.tck.StdNumContractTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CpfTest extends StdNumContractTest {

    @Override
    protected StdNum subject() {
        return Cpf.INSTANCE;
    }

    @Test
    void formatsWithTheStandardMask() {
        assertEquals("390.533.447-05", Cpf.INSTANCE.format("39053344705"));
    }

    @Test
    void formatOfInvalidNumberThrows() {
        assertThrows(ValidationException.class, () -> Cpf.INSTANCE.format("123"));
        assertThrows(ValidationException.class, () -> Cpf.INSTANCE.format("11111111111"));
    }

    @Test
    void calculatesCheckDigits() {
        assertEquals("05", Cpf.calcCheckDigits("390533447"));
        assertEquals("09", Cpf.calcCheckDigits("123.456.789"));
    }

    @Test
    void unicodeSeparatorsAreCleaned() {
        // en dash instead of hyphen, as pasted from formatted documents
        assertEquals("39053344705", Cpf.INSTANCE.validate("390.533.447–05"));
    }
}
