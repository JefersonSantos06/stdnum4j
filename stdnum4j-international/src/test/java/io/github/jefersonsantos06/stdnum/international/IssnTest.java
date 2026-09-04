package io.github.jefersonsantos06.stdnum.international;

import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.tck.StdNumContractTest;

class IssnTest extends StdNumContractTest {

    @Override
    protected StdNum subject() {
        return Issn.INSTANCE;
    }
}
