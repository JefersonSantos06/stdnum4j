package io.github.jefersonsantos06.stdnum.br;

import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.tck.StdNumContractTest;

class BrCodeTest extends StdNumContractTest {

    @Override
    protected StdNum subject() {
        return BrCode.INSTANCE;
    }
}
