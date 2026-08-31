package io.github.jefersonsantos06.stdnum.international;

import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.StdNumProvider;

import java.util.Collection;
import java.util.List;

/** Registers the international number types with the registry. */
public final class InternationalProvider implements StdNumProvider {

    @Override
    public Collection<StdNum> numbers() {
        return List.of(
                Iban.INSTANCE,
                Isbn.INSTANCE,
                Ean.INSTANCE,
                Isin.INSTANCE,
                Bic.INSTANCE);
    }
}
