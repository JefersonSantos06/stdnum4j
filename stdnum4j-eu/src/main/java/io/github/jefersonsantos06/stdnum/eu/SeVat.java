package io.github.jefersonsantos06.stdnum.eu;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.Message;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;


/**
 * Momsregistreringsnummer, the Swedish VAT number: the ten-digit
 * organisation number followed by {@code 01}.
 */
public final class SeVat implements StdNum {

    public static final SeVat INSTANCE = new SeVat();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("se.vat", "Moms")
                    .country("SE")
                    .title("Momsregistreringsnummer")
                    .description("Swedish VAT number: the organisation number followed by 01.")
                    .tags(Tag.VAT)
                    .build();

    private SeVat() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " -.", "SE");
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (!Strings.isDigits(n)) {
            throw new InvalidFormatException();
        }
        if (n.length() != 12) {
            throw new InvalidLengthException();
        }
        if (!n.endsWith("01")) {
            throw new InvalidFormatException(Message.of(SeVat.class, "vat.se.suffix",
                    "A Swedish VAT number ends with 01."));
        }
        SeOrgnr.INSTANCE.validate(n.substring(0, 10));
        return n;
    }
}
