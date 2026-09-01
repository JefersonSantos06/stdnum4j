package io.github.jefersonsantos06.stdnum.eu;

import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.tck.StdNumContractTest;

class SkRcTest extends StdNumContractTest {

    @Override
    protected StdNum subject() {
        return SkRc.INSTANCE;
    }
}
