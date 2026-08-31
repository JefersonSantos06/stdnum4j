package io.github.jefersonsantos06.stdnum.br;

import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.tck.StdNumContractTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CnpjTest extends StdNumContractTest {

    @Override
    protected StdNum subject() {
        return Cnpj.INSTANCE;
    }

    @Test
    void formatsWithTheStandardMask() {
        assertEquals("16.727.230/0001-97", Cnpj.INSTANCE.format("16727230000197"));
        assertEquals("12.ABC.345/01DE-35", Cnpj.INSTANCE.format("12abc34501de35"));
    }

    @Test
    void calculatesCheckDigitsForBothFormats() {
        assertEquals("97", Cnpj.calcCheckDigits("167272300001"));
        assertEquals("35", Cnpj.calcCheckDigits("12ABC34501DE"));
    }

    @Test
    void lowerCaseAlphanumericIsNormalised() {
        assertEquals("12ABC34501DE35", Cnpj.INSTANCE.validate("12abc34501de35"));
    }
}
