package io.github.jefersonsantos06.stdnum.latam;

import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.StdNumProvider;

import java.util.Collection;
import java.util.List;

/** Registers the Latin American number types with the registry. */
public final class LatamProvider implements StdNumProvider {

    @Override
    public Collection<StdNum> numbers() {
        return List.of(
                ArCuit.INSTANCE,
                ArDni.INSTANCE,
                ClRut.INSTANCE,
                CoNit.INSTANCE,
                CuNi.INSTANCE,
                EcCi.INSTANCE,
                GtNit.INSTANCE,
                PeRuc.INSTANCE,
                PyRuc.INSTANCE,
                UyRut.INSTANCE,
                VeRif.INSTANCE);
    }
}
