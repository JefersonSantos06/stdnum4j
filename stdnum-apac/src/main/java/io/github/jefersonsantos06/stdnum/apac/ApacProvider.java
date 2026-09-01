package io.github.jefersonsantos06.stdnum.apac;

import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.StdNumProvider;

import java.util.Collection;
import java.util.List;

/** Registers the Asia-Pacific number types with the registry. */
public final class ApacProvider implements StdNumProvider {

    @Override
    public Collection<StdNum> numbers() {
        return List.of(
                AuAbn.INSTANCE,
                AuAcn.INSTANCE,
                AuTfn.INSTANCE,
                CnUscc.INSTANCE,
                IdNpwp.INSTANCE,
                IlHp.INSTANCE,
                IlIdnr.INSTANCE,
                InAadhaar.INSTANCE,
                InEpic.INSTANCE,
                InGstin.INSTANCE,
                InPan.INSTANCE,
                InVid.INSTANCE,
                JpCn.INSTANCE,
                JpIn.INSTANCE,
                KrBrn.INSTANCE,
                KrRrn.INSTANCE,
                MyNric.INSTANCE,
                NzIrd.INSTANCE,
                OmVat.INSTANCE,
                PkCnic.INSTANCE,
                RuInn.INSTANCE,
                RuOgrn.INSTANCE,
                SgUen.INSTANCE,
                ThMoa.INSTANCE,
                ThPin.INSTANCE,
                ThTin.INSTANCE,
                TrTckimlik.INSTANCE,
                TrVkn.INSTANCE,
                TwUbn.INSTANCE,
                VnMst.INSTANCE);
    }
}
