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
 * COE, the number San Marino gives an economic operator: up to five digits
 * with no check digit.
 *
 * <p>As with the Finnish {@link FiAssociationid}, the numbers below 100 were
 * handed out sparsely, so only the ones actually issued are accepted.</p>
 */
public final class SmCoe implements StdNum {

    public static final SmCoe INSTANCE = new SmCoe();

    /** The numbers below 100 that were issued. */
    private static final Set<Integer> LOW_NUMBERS = Set.of(
            2, 4, 6, 7, 8, 9, 10, 11, 13, 16, 18, 19, 20, 21, 25, 26, 30, 32, 33, 35,
            36, 37, 38, 39, 40, 42, 45, 47, 49, 51, 52, 55, 56, 57, 58, 59, 61, 62,
            64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 79, 80, 81, 84, 85,
            87, 88, 91, 92, 94, 95, 96, 97, 99);

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("sm.coe", "COE")
                    .country("SM")
                    .title("Codice Operatore Economico")
                    .description("San Marino economic operator code: up to 5 digits, those"
                            + " below 100 drawn from the ones actually issued.")
                    .tags(Tag.COMPANY, Tag.TAX)
                    .references("https://www.camcom.sm/registri-e-servizi-della-camera-di-commercio/"
                            + "registri/autorizzazioni-ad-operare/")
                    .build();

    private SmCoe() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        String n = Strings.compact(number, ".");
        int start = 0;
        while (start < n.length() && n.charAt(start) == '0') {
            start++;
        }
        return n.substring(start);
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (n.isEmpty() || n.length() > 5) {
            throw new InvalidLengthException();
        }
        if (!Strings.isDigits(n)) {
            throw new InvalidFormatException();
        }
        if (n.length() < 3 && !LOW_NUMBERS.contains(Integer.parseInt(n))) {
            throw new InvalidComponentException(Message.of(SmCoe.class, "register.low-number",
                    "This number below 100 was never issued."));
        }
        return n;
    }
}
