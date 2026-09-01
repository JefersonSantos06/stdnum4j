package io.github.jefersonsantos06.stdnum.br;

import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.tck.StdNumContractTest;

class LinhaDigitavelTest extends StdNumContractTest {

    @Override
    protected StdNum subject() {
        return LinhaDigitavel.INSTANCE;
    }
}
