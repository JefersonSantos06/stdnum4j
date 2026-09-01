package io.github.jefersonsantos06.stdnum.latam;

import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.tck.StdNumContractTest;

class CrCpfTest extends StdNumContractTest {

    @Override
    protected StdNum subject() {
        return CrCpf.INSTANCE;
    }
}
