package io.github.jefersonsantos06.stdnum.latam;

import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.tck.StdNumContractTest;

class DoRncTest extends StdNumContractTest {

    @Override
    protected StdNum subject() {
        return DoRnc.INSTANCE;
    }
}
