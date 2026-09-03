package io.github.jefersonsantos06.stdnum.eu;

import io.github.jefersonsantos06.stdnum.algo.Luhn;
import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidComponentException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.Message;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;


/**
 * Partita IVA, the Italian VAT number: seven digits identifying the
 * company (not all zero), three digits for the province of residence
 * (001-100, 120, 121, 888 or 999) and a Luhn check digit. An optional
 * {@code IT} prefix is accepted.
 */
public final class ItIva implements StdNum {

    public static final ItIva INSTANCE = new ItIva();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("it.iva", "Partita IVA")
                    .country("IT")
                    .title("Partita IVA")
                    .description("Italian VAT number: 11 digits with a province code and a"
                            + " Luhn check digit.")
                    .tags(Tag.VAT, Tag.COMPANY)
                    .build();

    private ItIva() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " -:", "IT");
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (!Strings.isDigits(n)) {
            throw new InvalidFormatException();
        }
        if (n.length() != 11) {
            throw new InvalidLengthException();
        }
        if (Integer.parseInt(n.substring(0, 7)) == 0) {
            throw new InvalidFormatException();
        }
        int province = Integer.parseInt(n.substring(7, 10));
        boolean validProvince = (province >= 1 && province <= 100)
                || province == 120 || province == 121 || province == 888 || province == 999;
        if (!validProvince) {
            throw new InvalidComponentException(Message.of(ItIva.class, "iva.province",
                    "Unknown province of residence."));
        }
        Luhn.validate(n);
        return n;
    }
}
