package io.github.jefersonsantos06.stdnum.africa;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.util.Locale;

/**
 * Matricule fiscal, the Tunisian tax number: seven digits, a control letter,
 * and for a company three more letters and a three-digit establishment
 * number.
 *
 * <p>The establishment number is 000 for the head office, and only a branch
 * — category E — may carry another.</p>
 */
public final class TnMf implements StdNum {

    public static final TnMf INSTANCE = new TnMf();

    /** The control letters, which leave out the ones easily misread. */
    private static final String CONTROL_KEYS = "ABCDEFGHJKLMNPQRSTVWXYZ";
    private static final String TVA_CODES = "APBDN";
    private static final String CATEGORY_CODES = "MPCNE";

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("tn.mf", "Matricule fiscal")
                    .country("TN")
                    .title("Matricule fiscal tunisien")
                    .description("Tunisian tax number: 7 digits and a control letter, followed"
                            + " for a company by a VAT code, a category and an establishment.")
                    .tags(Tag.COMPANY, Tag.TAX)
                    .references("https://www.registre-entreprises.tn/")
                    .build();

    private TnMf() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        String n = Strings.compact(number, " /.-").toUpperCase(Locale.ROOT);
        // the serial is quoted without its leading zeros as often as with them
        int serial = 0;
        while (serial < n.length() && n.charAt(serial) >= '0' && n.charAt(serial) <= '9') {
            serial++;
        }
        return serial == 0 || serial >= 7 ? n
                : "0".repeat(7 - serial) + n;
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (n.length() != 8 && n.length() != 13) {
            throw new InvalidLengthException();
        }
        if (!Strings.isDigits(n.substring(0, 7))) {
            throw new InvalidFormatException();
        }
        if (CONTROL_KEYS.indexOf(n.charAt(7)) < 0) {
            throw new InvalidFormatException();
        }
        if (n.length() == 8) {
            return n;
        }
        if (TVA_CODES.indexOf(n.charAt(8)) < 0 || CATEGORY_CODES.indexOf(n.charAt(9)) < 0) {
            throw new InvalidFormatException();
        }
        if (!Strings.isDigits(n.substring(10))) {
            throw new InvalidFormatException();
        }
        if (!n.endsWith("000") && n.charAt(9) != 'E') {
            throw new InvalidFormatException();
        }
        return n;
    }

    @Override
    public String format(String number) {
        String n = validate(number);
        return n.length() == 8 ? n.substring(0, 7) + '/' + n.charAt(7)
                : n.substring(0, 7) + '/' + n.charAt(7) + '/' + n.charAt(8)
                        + '/' + n.charAt(9) + '/' + n.substring(10);
    }
}
