package io.github.jefersonsantos06.stdnum.africa;

import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.StdNumProvider;

import java.util.Collection;
import java.util.List;

/** Registers the African number types with the registry. */
public final class AfricaProvider implements StdNumProvider {

    @Override
    public Collection<StdNum> numbers() {
        return List.of(ZaIdnr.INSTANCE, ZaTin.INSTANCE);
    }
}
