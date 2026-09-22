package io.github.jefersonsantos06.stdnum.br;

import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.ValidationException;
import io.github.jefersonsantos06.stdnum.tck.StdNumContractTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

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
    void safeFormatHandsTheRefusalBackInsteadOfThrowing() {
        assertEquals("390.533.447-05", Cpf.INSTANCE.safeFormat("39053344705"));
        // what it refuses comes back undressed, never wearing the mask
        assertEquals("11111111111", Cpf.INSTANCE.safeFormat("11111111111"));
        assertEquals("123", Cpf.INSTANCE.safeFormat("123"));
        assertEquals("abacaxi", Cpf.INSTANCE.safeFormat("abacaxi"));
        assertNull(Cpf.INSTANCE.safeFormat(null));
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
