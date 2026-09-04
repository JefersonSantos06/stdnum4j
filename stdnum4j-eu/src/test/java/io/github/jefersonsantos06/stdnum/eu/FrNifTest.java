package io.github.jefersonsantos06.stdnum.eu;

import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.tck.StdNumContractTest;

class FrNifTest extends StdNumContractTest {

    @Override
    protected StdNum subject() {
        return FrNif.INSTANCE;
    }
}
