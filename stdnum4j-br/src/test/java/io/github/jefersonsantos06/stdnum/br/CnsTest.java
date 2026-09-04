package io.github.jefersonsantos06.stdnum.br;

import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.tck.StdNumContractTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CnsTest extends StdNumContractTest {

    @Override
    protected StdNum subject() {
        return Cns.INSTANCE;
    }

    @Test
    void formatsInGroups() {
        assertEquals("123 4567 8901 0000", Cns.INSTANCE.format("123456789010000"));
    }

    @Test
    void definitiveSuffixCoversBothFillers() {
        assertEquals("0000", Cns.definitiveSuffix("12345678901"));
        // remainder 1 forces the +2 correction and the 001 filler
        assertEquals("0018", Cns.definitiveSuffix("10000000006"));
    }
}
