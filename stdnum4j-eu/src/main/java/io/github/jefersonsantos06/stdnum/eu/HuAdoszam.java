package io.github.jefersonsantos06.stdnum.eu;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidComponentException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.Message;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Mask;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.util.List;

/**
 * Adószám, the Hungarian domestic tax number of a business: eleven digits in
 * three groups — the eight-digit törzsszám, which is the {@link HuAnum} and
 * carries the check digit, a VAT status code, and the code of the tax office.
 *
 * <p>The VAT status is 1 (exempt), 2 (general), 3 (simplified business tax),
 * 4 (member of a VAT group) or 5 (the group itself). The tax office code is
 * 02 to 20 or 22 to 40 for the county directorates, 41 to 43 for Budapest,
 * 44 for the large taxpayers' office and 51 for special cases. Both codes can
 * change over the life of a taxpayer; the törzsszám does not.</p>
 */
public final class HuAdoszam implements StdNum {

    public static final HuAdoszam INSTANCE = new HuAdoszam();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("hu.adoszam", "Adószám")
                    .country("HU")
                    .title("Hungarian tax number")
                    .description("Hungarian domestic tax number of a business: 11 digits as"
                            + " the 8-digit ANUM, a VAT status code and a tax office code.")
                    .tags(Tag.COMPANY, Tag.TAX)
                    .references(
                            "https://www.oecd.org/tax/automatic-exchange/crs-implementation-and-assistance/tax-identification-numbers/Hungary-TIN.pdf",
                            "https://hu.wikipedia.org/wiki/Ad%C3%B3sz%C3%A1m")
                    .build();

    private static final Mask MASK = Mask.of("########-#-##");

    private HuAdoszam() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " -");
    }

    /** The törzsszám, which is also the number of the Community VAT number. */
    public static String toAnum(String number) {
        return INSTANCE.validate(number).substring(0, 8);
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
        HuAnum.INSTANCE.validate(n.substring(0, 8));
        if (n.charAt(8) < '1' || n.charAt(8) > '5') {
            throw new InvalidComponentException(Message.of(HuAdoszam.class, "adoszam.vat-status",
                    "Not a VAT status code."));
        }
        int office = Integer.parseInt(n.substring(9));
        if (office < 2 || office == 21 || office > 44 && office != 51) {
            throw new InvalidComponentException(Message.of(HuAdoszam.class, "adoszam.tax-office",
                    "Not the code of a tax office."));
        }
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
