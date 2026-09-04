package io.github.jefersonsantos06.stdnum.tck;

import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.StdNumProvider;

import java.util.Collection;
import java.util.List;

/** Registers {@link DummyNumber} so registry integration can be verified. */
public final class DummyProvider implements StdNumProvider {

    @Override
    public Collection<StdNum> numbers() {
        return List.of(DummyNumber.INSTANCE);
    }
}
