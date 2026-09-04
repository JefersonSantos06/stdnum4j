package io.github.jefersonsantos06.stdnum.international;

import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.tck.StdNumContractTest;

class BitcoinTest extends StdNumContractTest {

    @Override
    protected StdNum subject() {
        return Bitcoin.INSTANCE;
    }
}
