package io.github.jefersonsantos06.stdnum.na;

import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.tck.StdNumContractTest;

class UsAtinTest extends StdNumContractTest {

    @Override
    protected StdNum subject() {
        return UsAtin.INSTANCE;
    }
}
