package io.github.jefersonsantos06.stdnum.eu;

import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.tck.StdNumContractTest;

class FiYtunnusTest extends StdNumContractTest {

    @Override
    protected StdNum subject() {
        return FiYtunnus.INSTANCE;
    }
}
