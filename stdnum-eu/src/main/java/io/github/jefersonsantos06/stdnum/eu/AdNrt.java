package io.github.jefersonsantos06.stdnum.eu;

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
 * NRT (Número de Registre Tributari), the Andorran tax number: a
 * type letter, six digits and a control letter. The number ranges are
 * partitioned by type — {@code F} below 700000, {@code A} and {@code L}
 * between 700000 and 800000.
 */
public final class AdNrt implements StdNum {

    public static final AdNrt INSTANCE = new AdNrt();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("ad.nrt", "NRT")
                    .country("AD")
                    .title("Número de Registre Tributari")
                    .description("Andorran tax number: a type letter, 6 digits and a control"
                            + " letter, with type-dependent ranges.")
                    .tags(Tag.TAX, Tag.VAT)
                    .build();

    private static final String TYPES = "ACDEFGLOPU";

    private AdNrt() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " -.").toUpperCase(Locale.ROOT);
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (n.length() != 8) {
            throw new InvalidLengthException();
        }
        if (!Character.isLetter(n.charAt(0)) || !Character.isLetter(n.charAt(7))
                || !Strings.isDigits(n.substring(1, 7))) {
            throw new InvalidFormatException();
        }
        if (TYPES.indexOf(n.charAt(0)) < 0) {
            throw new InvalidComponentException(Message.of(AdNrt.class, "nrt.type",
                    "Unknown NRT type letter."));
        }
        String digits = n.substring(1, 7);
        if (n.charAt(0) == 'F' && digits.compareTo("699999") > 0) {
            throw new InvalidComponentException(Message.of(AdNrt.class, "nrt.range-f",
                    "An F number is below 700000."));
        }
        if ((n.charAt(0) == 'A' || n.charAt(0) == 'L')
                && !(digits.compareTo("699999") > 0 && digits.compareTo("800000") < 0)) {
            throw new InvalidComponentException(Message.of(AdNrt.class, "nrt.range-al",
                    "An A or L number is between 700000 and 800000."));
        }
        return n;
    }

    @Override
    public String format(String number) {
        String n = validate(number);
        return n.charAt(0) + "-" + n.substring(1, 7) + "-" + n.charAt(7);
    }
}
