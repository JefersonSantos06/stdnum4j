package io.github.jefersonsantos06.stdnum.postal;

import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.tck.StdNumContractTest;

class IePostalCodeTest extends StdNumContractTest {

    @Override
    protected StdNum subject() {
        return PostalCode.of("IE").orElseThrow();
    }
}
