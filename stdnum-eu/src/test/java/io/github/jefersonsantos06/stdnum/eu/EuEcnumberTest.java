package io.github.jefersonsantos06.stdnum.eu;

import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.tck.StdNumContractTest;

class EuEcnumberTest extends StdNumContractTest {

    @Override
    protected StdNum subject() {
        return EuEcnumber.INSTANCE;
    }
}
