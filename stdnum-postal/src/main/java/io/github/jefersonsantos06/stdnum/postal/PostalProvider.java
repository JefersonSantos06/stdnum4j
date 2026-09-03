package io.github.jefersonsantos06.stdnum.postal;

import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.StdNumProvider;

import java.util.Collection;
import java.util.List;

/** Registers one postal code type per country the data file describes. */
public final class PostalProvider implements StdNumProvider {

    @Override
    public Collection<StdNum> numbers() {
        return List.copyOf(PostalCode.all());
    }
}
