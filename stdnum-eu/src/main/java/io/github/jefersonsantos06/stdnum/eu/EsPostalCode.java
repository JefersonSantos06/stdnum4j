package io.github.jefersonsantos06.stdnum.eu;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidComponentException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.Reasons;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

/**
 * The Spanish postal code: five digits whose first two name one of the fifty
 * provinces, or Ceuta and Melilla.
 */
public final class EsPostalCode implements StdNum {

    public static final EsPostalCode INSTANCE = new EsPostalCode();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("es.postal_code", "Codigo postal")
                    .country("ES")
                    .title("Codigo postal espanol")
                    .description("Spanish postal code: 5 digits opening with a province"
                            + " between 01 and 52.")
                    .tags(Tag.POSTAL)
                    .references("https://es.wikipedia.org/wiki/C%C3%B3digo_postal_de_Espa%C3%B1a")
                    .build();

    private EsPostalCode() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " ");
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (n.length() != 5) {
            throw new InvalidLengthException();
        }
        if (!Strings.isDigits(n)) {
            throw new InvalidFormatException();
        }
        String province = n.substring(0, 2);
        if (province.compareTo("01") < 0 || province.compareTo("52") > 0) {
            throw new InvalidComponentException(Reasons.provinceCode());
        }
        return n;
    }
}
