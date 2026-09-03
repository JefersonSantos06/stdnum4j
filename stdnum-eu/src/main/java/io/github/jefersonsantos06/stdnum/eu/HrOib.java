package io.github.jefersonsantos06.stdnum.eu;

import io.github.jefersonsantos06.stdnum.algo.Iso7064;
import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;


/**
 * OIB (Osobni identifikacijski broj), the Croatian identification number
 * for persons and legal entities: eleven digits carrying no personal
 * information, closed by an ISO 7064 MOD 11,10 check digit. It doubles as
 * the Croatian VAT number.
 */
public final class HrOib implements StdNum {

    public static final HrOib INSTANCE = new HrOib();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("hr.oib", "OIB")
                    .country("HR")
                    .title("Osobni identifikacijski broj")
                    .description("Croatian identification and VAT number: 11 digits with an"
                            + " ISO 7064 MOD 11,10 check digit.")
                    .tags(Tag.VAT, Tag.PERSON, Tag.COMPANY)
                    .build();

    private HrOib() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " -", "HR");
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
        Iso7064.MOD_11_10.validate(n);
        return n;
    }
}
