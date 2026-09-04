package io.github.jefersonsantos06.stdnum.international;

import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.tck.StdNumContractTest;

class UpiTest extends StdNumContractTest {

    @Override
    protected StdNum subject() {
        return Upi.INSTANCE;
    }
}
