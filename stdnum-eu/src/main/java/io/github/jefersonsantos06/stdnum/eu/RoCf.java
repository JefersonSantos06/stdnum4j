package io.github.jefersonsantos06.stdnum.eu;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.util.Locale;

/**
 * CF (Cod de înregistrare în scopuri de TVA), the Romanian VAT number.
 *
 * <p>It dispatches by length: two to ten digits are a company identifier
 * ({@link RoCui}), while thirteen digits are a personal numeric code
 * ({@link RoCnp}) — sources disagree on whether the latter is really
 * usable as a VAT number, but the registry accepts it.</p>
 */
public final class RoCf implements StdNum {

    public static final RoCf INSTANCE = new RoCf();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("ro.cf", "CF")
                    .country("RO")
                    .title("Cod de înregistrare în scopuri de TVA")
                    .description("Romanian VAT number: a 2-10 digit company identifier or a"
                            + " 13-digit personal numeric code.")
                    .tags(Tag.VAT)
                    .build();

    private RoCf() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        String n = Strings.compact(number, " -").toUpperCase(Locale.ROOT);
        return n.startsWith("RO") ? n.substring(2) : n;
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (n.length() == 13) {
            RoCnp.INSTANCE.validate(n);
        } else if (n.length() >= 2 && n.length() <= 10) {
            RoCui.INSTANCE.validate(n);
        } else {
            throw new InvalidLengthException();
        }
        return n;
    }
}
