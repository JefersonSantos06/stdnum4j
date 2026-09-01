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
                AtUid.INSTANCE,
                BeVat.INSTANCE,
                DeVat.INSTANCE,
                DkCpr.INSTANCE,
                DkCvr.INSTANCE,
                EsCif.INSTANCE,
                EsDni.INSTANCE,
                EsNie.INSTANCE,
                EsNif.INSTANCE,
                FiAlv.INSTANCE,
                FiYtunnus.INSTANCE,
                FrSiren.INSTANCE,
                FrSiret.INSTANCE,
                FrTva.INSTANCE,
                GbVat.INSTANCE,
                IeVat.INSTANCE,
                ItIva.INSTANCE,
                LuTva.INSTANCE,
                NlBsn.INSTANCE,
                NlBtw.INSTANCE,
                NoMva.INSTANCE,
                NoOrgnr.INSTANCE,
                PtNif.INSTANCE,
                SeOrgnr.INSTANCE,
                SeVat.INSTANCE);
    }
}
