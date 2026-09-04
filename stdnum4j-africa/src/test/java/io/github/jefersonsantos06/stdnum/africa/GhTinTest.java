package io.github.jefersonsantos06.stdnum.africa;

import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.tck.StdNumContractTest;

class GhTinTest extends StdNumContractTest {

    @Override
    protected StdNum subject() {
        return GhTin.INSTANCE;
    }
}
