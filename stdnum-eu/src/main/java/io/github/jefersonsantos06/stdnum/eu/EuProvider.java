package io.github.jefersonsantos06.stdnum.eu;

import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.StdNumProvider;

import java.util.Collection;
import java.util.List;

/** Registers the European number types with the registry. */
public final class EuProvider implements StdNumProvider {

    @Override
    public Collection<StdNum> numbers() {
        return List.of(
                DeVat.INSTANCE,
                FrSiren.INSTANCE,
                FrSiret.INSTANCE,
                FrTva.INSTANCE,
                EsDni.INSTANCE,
                EsNie.INSTANCE,
                EsCif.INSTANCE,
                EsNif.INSTANCE,
                ItIva.INSTANCE,
                PtNif.INSTANCE);
    }
}
