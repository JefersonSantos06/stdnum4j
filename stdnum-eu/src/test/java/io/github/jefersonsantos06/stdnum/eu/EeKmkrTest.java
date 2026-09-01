package io.github.jefersonsantos06.stdnum.eu;

import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.tck.StdNumContractTest;

class EeKmkrTest extends StdNumContractTest {

    @Override
    protected StdNum subject() {
        return EeKmkr.INSTANCE;
    }
}
