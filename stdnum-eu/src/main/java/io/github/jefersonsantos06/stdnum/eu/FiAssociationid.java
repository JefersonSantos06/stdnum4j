package io.github.jefersonsantos06.stdnum.eu;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidComponentException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.Message;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.util.Set;

/**
 * The registration number of a Finnish association: one to six digits, with
 * no check digit.
 *
 * <p>Numbers below 100 were handed out sparsely when the register opened, so
 * a one- or two-digit number is only valid if it is one that was actually
 * issued.</p>
 */
public final class FiAssociationid implements StdNum {

    public static final FiAssociationid INSTANCE = new FiAssociationid();

    /** The numbers below 100 that were issued. */
    private static final Set<Integer> LOW_NUMBERS = Set.of(
            1, 6, 7, 9, 12, 14, 15, 16, 18, 22, 23, 24, 27, 28, 29, 35, 36, 38, 40,
            41, 42, 43, 45, 46, 50, 52, 55, 58, 60, 64, 65, 68, 72, 75, 76, 77, 78,
            83, 84, 85, 89, 92);

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("fi.associationid", "Rekisterinumero")
                    .country("FI")
                    .title("Suomalainen yhdistysrekisterinumero")
                    .description("Registration number of a Finnish association: 1 to 6 digits,"
                            + " those below 100 drawn from the ones actually issued.")
                    .tags(Tag.COMPANY)
                    .references("https://www.prh.fi/en/yhdistysrekisteri.html")
                    .build();

    private FiAssociationid() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " -._+");
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (!Strings.isDigits(n)) {
            throw new InvalidFormatException();
        }
        if (n.length() > 6) {
            throw new InvalidLengthException();
        }
        if (n.length() < 3 && !LOW_NUMBERS.contains(Integer.parseInt(n))) {
            throw new InvalidComponentException(Message.of(FiAssociationid.class, "register.low-number",
                    "This number below 100 was never issued."));
        }
        return n;
    }

    @Override
    public String format(String number) {
        String n = validate(number);
        return n.length() <= 3 ? n : n.substring(0, n.length() - 3) + '.' + n.substring(n.length() - 3);
    }
}
