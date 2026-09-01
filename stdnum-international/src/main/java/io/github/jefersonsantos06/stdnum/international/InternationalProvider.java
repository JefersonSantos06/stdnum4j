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
                EuVat.INSTANCE,
                Figi.INSTANCE,
                Iban.INSTANCE,
                Imei.INSTANCE,
                Imo.INSTANCE,
                Isbn.INSTANCE,
                Isin.INSTANCE,
                Ismn.INSTANCE,
                Isni.INSTANCE,
                Iso11649.INSTANCE,
                Iso6346.INSTANCE,
                Issn.INSTANCE,
                Lei.INSTANCE,
                Mac.INSTANCE,
                Sedol.INSTANCE,
                Vatin.INSTANCE);
    }
}
