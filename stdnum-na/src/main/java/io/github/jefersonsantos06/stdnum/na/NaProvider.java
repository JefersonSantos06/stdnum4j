package io.github.jefersonsantos06.stdnum.na;

import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.StdNumProvider;

import java.util.Collection;
import java.util.List;

/** Registers the North American number types with the registry. */
public final class NaProvider implements StdNumProvider {

    @Override
    public Collection<StdNum> numbers() {
        return List.of(
                CaBn.INSTANCE,
                CaSin.INSTANCE,
                UsAtin.INSTANCE,
                UsEin.INSTANCE,
                UsItin.INSTANCE,
                UsPtin.INSTANCE,
                UsRtn.INSTANCE,
                UsSsn.INSTANCE,
                UsTin.INSTANCE);
    }
}
