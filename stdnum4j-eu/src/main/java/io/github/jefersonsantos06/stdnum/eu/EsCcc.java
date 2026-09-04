package io.github.jefersonsantos06.stdnum.eu;

import io.github.jefersonsantos06.stdnum.algo.Mod97;
import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.util.Locale;

/**
 * CCC, the Spanish bank account code that the IBAN replaced: the bank, the
 * branch, two check digits and the account number.
 *
 * <p>The check digits sit in the middle rather than at the end, one covering
 * the bank and branch and one the account, each weighted by ascending powers
 * of two modulo 11.</p>
 */
public final class EsCcc implements StdNum {

    public static final EsCcc INSTANCE = new EsCcc();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("es.ccc", "CCC")
                    .country("ES")
                    .title("Codigo Cuenta Cliente")
                    .description("Spanish bank account code: 20 digits with two check digits"
                            + " between the branch and the account number.")
                    .tags(Tag.BANK)
                    .references("https://es.wikipedia.org/wiki/C%C3%B3digo_cuenta_cliente")
                    .build();

    private EsCcc() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " -").toUpperCase(Locale.ROOT);
    }

    /** One check digit, over digits weighted by ascending powers of two. */
    private static char checkDigit(String part) {
        int check = 0;
        for (int i = 0; i < part.length(); i++) {
            check += (part.charAt(i) - '0') << i;
        }
        check %= 11;
        return (char) ('0' + (check < 2 ? check : 11 - check));
    }

    /** The two check digits of an account, from the parts on either side of them. */
    public static String calcCheckDigits(String number) {
        String n = INSTANCE.compact(number);
        return "" + checkDigit("00" + n.substring(0, 8)) + checkDigit(n.substring(10));
    }

    /** The IBAN this account is held under, in its compact form. */
    public static String toIban(String number) {
        String n = INSTANCE.validate(number);
        return "ES" + Mod97.calcCheckDigits(n + "ES") + n;
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (n.length() != 20) {
            throw new InvalidLengthException();
        }
        if (!Strings.isDigits(n)) {
            throw new InvalidFormatException();
        }
        if (!n.substring(8, 10).equals(calcCheckDigits(n))) {
            throw new InvalidChecksumException();
        }
        return n;
    }

    @Override
    public String format(String number) {
        String n = validate(number);
        return String.join(" ", n.substring(0, 4), n.substring(4, 8), n.substring(8, 10),
                n.substring(10, 15), n.substring(15));
    }
}
