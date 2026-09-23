package io.github.jefersonsantos06.stdnum.apac;

import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.tck.StdNumContractTest;

class AeTrnTest extends StdNumContractTest {

    @Override
    protected StdNum subject() {
        return AeTrn.INSTANCE;
    }
}
