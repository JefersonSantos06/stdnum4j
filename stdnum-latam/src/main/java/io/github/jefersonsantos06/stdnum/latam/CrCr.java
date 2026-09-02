package io.github.jefersonsantos06.stdnum.latam;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidComponentException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.Message;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.util.Locale;

/**
 * CR, the number Costa Rica gives a foreign resident: eleven or twelve digits
 * opening with a 1, with no check digit. Residents hold a {@link CrCpf}
 * instead.
 */
public final class CrCr implements StdNum {

    public static final CrCr INSTANCE = new CrCr();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("cr.cr", "CR")
                    .country("CR")
                    .title("Cedula de Residencia")
                    .description("Costa Rican foreign resident number: 11 or 12 digits"
                            + " starting with 1.")
                    .tags(Tag.PERSON)
                    .references("https://www.migracion.go.cr/")
                    .build();

    private CrCr() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " -").toUpperCase(Locale.ROOT);
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (n.length() != 11 && n.length() != 12) {
            throw new InvalidLengthException();
        }
        if (!Strings.isDigits(n)) {
            throw new InvalidFormatException();
        }
        if (n.charAt(0) != '1') {
            throw new InvalidComponentException(Message.of(CrCr.class, "cr.prefix",
                    "A residence number starts with 1."));
        }
        return n;
    }
}
