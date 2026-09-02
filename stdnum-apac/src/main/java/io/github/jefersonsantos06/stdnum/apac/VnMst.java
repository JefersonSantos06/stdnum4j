package io.github.jefersonsantos06.stdnum.apac;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidComponentException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.Message;
import io.github.jefersonsantos06.stdnum.spi.Reasons;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

/**
 * MST (Mã số thuế), the Vietnamese tax number: ten digits, optionally
 * followed by a three-digit branch code, with a prime-weighted mod 11
 * check digit in the tenth position.
 */
public final class VnMst implements StdNum {

    public static final VnMst INSTANCE = new VnMst();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("vn.mst", "MST")
                    .country("VN")
                    .title("Mã số thuế")
                    .description("Vietnamese tax number: 10 digits, optionally with a 3-digit"
                            + " branch code, and a weighted mod 11 check digit.")
                    .tags(Tag.TAX, Tag.VAT)
                    .build();

    private static final int[] WEIGHTS = {31, 29, 23, 19, 17, 13, 7, 5, 3};

    private VnMst() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " -.");
    }

    /** The check digit, which sits in the tenth position. */
    public static char calcCheckDigit(String number) {
        String n = INSTANCE.compact(number);
        int total = 0;
        for (int i = 0; i < WEIGHTS.length && i < n.length(); i++) {
            total += WEIGHTS[i] * (n.charAt(i) - '0');
        }
        return (char) ('0' + 10 - total % 11);
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (n.length() != 10 && n.length() != 13) {
            throw new InvalidLengthException();
        }
        if (!Strings.isDigits(n)) {
            throw new InvalidFormatException();
        }
        if (n.startsWith("0000000", 2)) {
            throw new InvalidComponentException(Reasons.zeroSequence());
        }
        if (n.length() == 13 && n.endsWith("000")) {
            throw new InvalidComponentException(Message.of(VnMst.class, "mst.branch",
                    "The branch code must not be zero."));
        }
        if (n.charAt(9) != calcCheckDigit(n)) {
            throw new InvalidChecksumException();
        }
        return n;
    }

    @Override
    public String format(String number) {
        String n = validate(number);
        return n.length() == 10 ? n : n.substring(0, 10) + "-" + n.substring(10);
    }
}
