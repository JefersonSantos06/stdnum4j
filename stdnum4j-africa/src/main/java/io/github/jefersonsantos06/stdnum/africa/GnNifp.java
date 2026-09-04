package io.github.jefersonsantos06.stdnum.africa;

import io.github.jefersonsantos06.stdnum.algo.Luhn;
import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Mask;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.util.List;

/**
 * NIFp, the Guinean taxpayer number: nine digits closing with a Luhn check
 * digit.
 */
public final class GnNifp implements StdNum {

    public static final GnNifp INSTANCE = new GnNifp();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("gn.nifp", "NIFp")
                    .country("GN")
                    .title("Numero d'Identification Fiscale permanent")
                    .description("Guinean taxpayer number: 9 digits with a Luhn check digit.")
                    .tags(Tag.COMPANY, Tag.TAX)
                    .references("https://www.dgi.gov.gn/")
                    .build();

    private static final Mask MASK = Mask.of("###-###-###");

    private GnNifp() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " -");
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (n.length() != 9) {
            throw new InvalidLengthException();
        }
        if (!Strings.isDigits(n)) {
            throw new InvalidFormatException();
        }
        Luhn.validate(n);
        return n;
    }

    @Override
    public String format(String number) {
        return MASK.fill(validate(number));
    }

    @Override
    public List<Mask> masks() {
        return List.of(MASK);
    }
}
