package io.github.jefersonsantos06.stdnum.eu;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.Message;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;


/**
 * MVA (Merverdiavgift), the Norwegian VAT number: the organisation number
 * with the suffix {@code MVA}.
 */
public final class NoMva implements StdNum {

    public static final NoMva INSTANCE = new NoMva();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("no.mva", "MVA")
                    .country("NO")
                    .title("Merverdiavgift")
                    .description("Norwegian VAT number: the organisation number suffixed"
                            + " with MVA.")
                    .tags(Tag.VAT)
                    .build();

    private NoMva() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " ", "NO");
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (!n.endsWith("MVA")) {
            throw new InvalidFormatException(Message.of(NoMva.class, "mva.suffix",
                    "A Norwegian VAT number ends with MVA."));
        }
        NoOrgnr.INSTANCE.validate(n.substring(0, n.length() - 3));
        return n;
    }

    @Override
    public String format(String number) {
        String n = validate(number);
        return "NO " + NoOrgnr.INSTANCE.format(n.substring(0, 9)) + " MVA";
    }
}
