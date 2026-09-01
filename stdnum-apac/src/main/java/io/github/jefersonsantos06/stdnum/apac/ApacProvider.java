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
                InAadhaar.INSTANCE,
                InPan.INSTANCE,
                NzIrd.INSTANCE);
    }
}
