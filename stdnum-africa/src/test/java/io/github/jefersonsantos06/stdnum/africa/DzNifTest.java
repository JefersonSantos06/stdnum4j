package io.github.jefersonsantos06.stdnum.africa;

import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.tck.StdNumContractTest;

class DzNifTest extends StdNumContractTest {

    @Override
    protected StdNum subject() {
        return DzNif.INSTANCE;
    }
}
