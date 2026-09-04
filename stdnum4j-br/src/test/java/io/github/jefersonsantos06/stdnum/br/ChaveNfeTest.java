package io.github.jefersonsantos06.stdnum.br;

import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.tck.StdNumContractTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ChaveNfeTest extends StdNumContractTest {

    private static final String KEY = "35240116727230000197550010000000011123456784";

    @Override
    protected StdNum subject() {
        return ChaveNfe.INSTANCE;
    }

    @Test
    void parseExposesTheParts() {
        ChaveNfe.Partes partes = ChaveNfe.parse(KEY);
        assertEquals(Uf.SP, partes.uf());
        assertEquals("2401", partes.anoMes());
        assertEquals("16727230000197", partes.documentoEmitente());
        assertEquals("55", partes.modelo());
        assertEquals("001", partes.serie());
        assertEquals("000000001", partes.numero());
        assertEquals("1", partes.formaEmissao());
        assertEquals("12345678", partes.codigoNumerico());
        assertEquals('4', partes.digito());
    }

    @Test
    void formatsInGroupsOfFour() {
        assertEquals("3524 0116 7272 3000 0197 5500 1000 0000 0111 2345 6784",
                ChaveNfe.INSTANCE.format(KEY));
    }

    @Test
    void calculatesTheCheckDigit() {
        assertEquals(4, ChaveNfe.calcCheckDigit(KEY.substring(0, 43)));
    }
}
