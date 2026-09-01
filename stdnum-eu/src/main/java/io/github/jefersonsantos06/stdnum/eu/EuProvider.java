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
                BgEgn.INSTANCE,
                BgPnf.INSTANCE,
                BgVat.INSTANCE,
                ChUid.INSTANCE,
                ChVat.INSTANCE,
                CyVat.INSTANCE,
                CzDic.INSTANCE,
                CzRc.INSTANCE,
                DeVat.INSTANCE,
                DkCpr.INSTANCE,
                DkCvr.INSTANCE,
                EeKmkr.INSTANCE,
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
                GrVat.INSTANCE,
                HrOib.INSTANCE,
                HuAnum.INSTANCE,
                IeVat.INSTANCE,
                IsKennitala.INSTANCE,
                ItIva.INSTANCE,
                LtPvm.INSTANCE,
                LuTva.INSTANCE,
                LvPvn.INSTANCE,
                MtVat.INSTANCE,
                NlBsn.INSTANCE,
                NlBtw.INSTANCE,
                NoMva.INSTANCE,
                NoOrgnr.INSTANCE,
                PlNip.INSTANCE,
                PlPesel.INSTANCE,
                PlRegon.INSTANCE,
                PtNif.INSTANCE,
                RoCui.INSTANCE,
                SeOrgnr.INSTANCE,
                SeVat.INSTANCE,
                SiDdv.INSTANCE,
                SkDph.INSTANCE,
                SkRc.INSTANCE);
    }
}
