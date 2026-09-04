package io.github.jefersonsantos06.stdnum.latam;

import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.tck.StdNumContractTest;

class PeCuiTest extends StdNumContractTest {

    @Override
    protected StdNum subject() {
        return PeCui.INSTANCE;
    }
}
