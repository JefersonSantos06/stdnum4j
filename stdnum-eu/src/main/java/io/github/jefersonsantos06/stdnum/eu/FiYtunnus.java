package io.github.jefersonsantos06.stdnum.eu;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;

/**
 * Y-tunnus, the Finnish business identifier: the same eight digits as the
 * VAT number ({@link FiAlv}), conventionally printed with a hyphen before
 * the check digit.
 */
public final class FiYtunnus implements StdNum {

    public static final FiYtunnus INSTANCE = new FiYtunnus();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("fi.ytunnus", "Y-tunnus")
                    .country("FI")
                    .title("Y-tunnus")
                    .description("Finnish business identifier: the VAT number printed with a"
                            + " hyphen before the check digit.")
                    .tags(Tag.COMPANY)
                    .build();

    private FiYtunnus() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return FiAlv.INSTANCE.compact(number);
    }

    @Override
    public String validate(String number) {
        return FiAlv.INSTANCE.validate(number);
    }

    @Override
    public String format(String number) {
        String n = validate(number);
        return n.substring(0, 7) + "-" + n.substring(7);
    }
}
