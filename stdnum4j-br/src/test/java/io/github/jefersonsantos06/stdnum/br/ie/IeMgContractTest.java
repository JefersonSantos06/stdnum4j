package io.github.jefersonsantos06.stdnum.br.ie;

import io.github.jefersonsantos06.stdnum.br.Uf;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.tck.StdNumContractTest;

class IeMgContractTest extends StdNumContractTest {

    @Override
    protected StdNum subject() {
        return InscricaoEstadual.of(Uf.MG);
    }
}
