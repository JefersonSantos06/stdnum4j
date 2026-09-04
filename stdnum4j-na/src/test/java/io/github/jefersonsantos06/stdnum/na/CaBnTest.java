package io.github.jefersonsantos06.stdnum.na;

import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.tck.StdNumContractTest;

class CaBnTest extends StdNumContractTest {

    @Override
    protected StdNum subject() {
        return CaBn.INSTANCE;
    }
}
