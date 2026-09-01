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
                Bic.INSTANCE,
                CasRn.INSTANCE,
                Cusip.INSTANCE,
                Ean.INSTANCE,
                Iban.INSTANCE,
                Imei.INSTANCE,
                Imo.INSTANCE,
                Isbn.INSTANCE,
                Isin.INSTANCE,
                Ismn.INSTANCE,
                Issn.INSTANCE,
                Lei.INSTANCE,
                Vatin.INSTANCE);
    }
}
