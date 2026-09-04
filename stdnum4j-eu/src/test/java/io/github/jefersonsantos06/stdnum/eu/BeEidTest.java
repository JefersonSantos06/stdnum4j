package io.github.jefersonsantos06.stdnum.eu;

import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.tck.StdNumContractTest;

class BeEidTest extends StdNumContractTest {

    @Override
    protected StdNum subject() {
        return BeEid.INSTANCE;
    }
}
