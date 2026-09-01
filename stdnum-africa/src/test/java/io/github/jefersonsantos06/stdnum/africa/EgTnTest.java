package io.github.jefersonsantos06.stdnum.africa;

import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.tck.StdNumContractTest;

class EgTnTest extends StdNumContractTest {

    @Override
    protected StdNum subject() {
        return EgTn.INSTANCE;
    }
}
