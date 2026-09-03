package io.github.jefersonsantos06.stdnum.eu;

import io.github.jefersonsantos06.stdnum.algo.Luhn;
import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Mask;
import io.github.jefersonsantos06.stdnum.text.Strings;

/**
 * Organisationsnummer, the Swedish company number: ten digits validated
 * with the Luhn checksum. It is the VAT number without the {@code SE}
 * prefix and the trailing {@code 01}.
 */
public final class SeOrgnr implements StdNum {

    public static final SeOrgnr INSTANCE = new SeOrgnr();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("se.orgnr", "Orgnr")
                    .country("SE")
                    .title("Organisationsnummer")
                    .description("Swedish company number: 10 digits with a Luhn checksum.")
                    .tags(Tag.COMPANY)
                    .build();

    private static final Mask MASK = Mask.of("######-####");

    private SeOrgnr() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " -.");
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (!Strings.isDigits(n)) {
            throw new InvalidFormatException();
        }
        if (n.length() != 10) {
            throw new InvalidLengthException();
        }
        Luhn.validate(n);
        return n;
    }

    @Override
    public String format(String number) {
        return MASK.fill(validate(number));
    }
}
