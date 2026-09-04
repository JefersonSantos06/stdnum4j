package io.github.jefersonsantos06.stdnum.apac;

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
 * BRN, the South Korean business registration number: ten digits in three
 * groups — the tax office that issued it, the kind of business, and a serial
 * number with a final digit.
 *
 * <p>The number carries no check digit, so validation is limited to the
 * ranges the three groups are drawn from: the tax office code starts at 101,
 * the business type is never 00 and the serial is never 0000.</p>
 */
public final class KrBrn implements StdNum {

    public static final KrBrn INSTANCE = new KrBrn();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("kr.brn", "BRN")
                    .country("KR")
                    .title("Korean Business Registration Number")
                    .description("South Korean business registration number: 10 digits as a tax"
                            + " office code, a business type code and a serial number.")
                    .tags(Tag.COMPANY, Tag.TAX)
                    .references("https://call.nts.go.kr/call/qna/selectQnaInfo.do?mi=1329&ctgId=CTG11944")
                    .build();

    private static final Mask MASK = Mask.of("###-##-#####");

    private KrBrn() {
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
        if (n.length() != 10) {
            throw new InvalidLengthException();
        }
        if (!Strings.isDigits(n)) {
            throw new InvalidFormatException();
        }
        if (n.substring(0, 3).compareTo("101") < 0) {
            throw new InvalidComponentException(Message.of(KrBrn.class, "brn.tax-office",
                    "Tax office codes start at 101."));
        }
        if (n.startsWith("00", 3)) {
            throw new InvalidComponentException(Message.of(KrBrn.class, "brn.business-type",
                    "00 is not a business type code."));
        }
        if (n.startsWith("0000", 5)) {
            throw new InvalidComponentException(Message.of(KrBrn.class, "brn.serial",
                    "0000 is not a serial number."));
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
