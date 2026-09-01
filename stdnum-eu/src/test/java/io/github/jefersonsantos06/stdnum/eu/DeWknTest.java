package io.github.jefersonsantos06.stdnum.eu;

import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.tck.StdNumContractTest;

class DeWknTest extends StdNumContractTest {

    @Override
    protected StdNum subject() {
        return DeWkn.INSTANCE;
    }
}
