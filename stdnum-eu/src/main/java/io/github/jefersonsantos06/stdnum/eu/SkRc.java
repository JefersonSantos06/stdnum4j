package io.github.jefersonsantos06.stdnum.eu;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;

/**
 * RČ (Rodné číslo), the Slovak birth number. Czechoslovakia issued a single
 * scheme until 1993, so the rules are exactly those of {@link CzRc}.
 */
public final class SkRc implements StdNum {

    public static final SkRc INSTANCE = new SkRc();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("sk.rc", "RČ")
                    .country("SK")
                    .title("Rodné číslo")
                    .description("Slovak birth number: identical in structure and checks to"
                            + " the Czech RČ.")
                    .tags(Tag.PERSON)
                    .build();

    private SkRc() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return CzRc.INSTANCE.compact(number);
    }

    @Override
    public String validate(String number) {
        return CzRc.INSTANCE.validate(number);
    }

    @Override
    public String format(String number) {
        return CzRc.INSTANCE.format(number);
    }
}
