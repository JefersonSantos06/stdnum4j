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
                Bitcoin.INSTANCE,
                CasRn.INSTANCE,
                Cfi.INSTANCE,
                Cusip.INSTANCE,
                Ean.INSTANCE,
                EuExcise.INSTANCE,
                EuVat.INSTANCE,
                Figi.INSTANCE,
                Grid.INSTANCE,
                Gs1128.INSTANCE,
                Iban.INSTANCE,
                Imei.INSTANCE,
                Imo.INSTANCE,
                Imsi.INSTANCE,
                Isan.INSTANCE,
                Isbn.INSTANCE,
                Isin.INSTANCE,
                Ismn.INSTANCE,
                Isni.INSTANCE,
                Iso11649.INSTANCE,
                Iso6346.INSTANCE,
                Isrc.INSTANCE,
                Issn.INSTANCE,
                Lei.INSTANCE,
                Mac.INSTANCE,
                Meid.INSTANCE,
                Sedol.INSTANCE,
                Upi.INSTANCE,
                Vatin.INSTANCE);
    }
}
