package io.github.jefersonsantos06.stdnum.international;

import io.github.jefersonsantos06.stdnum.algo.Mod97;
import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.Message;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.util.Locale;

/**
 * ISO 11649 structured creditor reference, used on payment instructions:
 * the literal {@code RF}, two check digits and up to 21 further
 * characters. Validation moves {@code RF} and the check digits to the end
 * and applies the ISO 7064 MOD 97-10 check, exactly as an IBAN does.
 */
public final class Iso11649 implements StdNum {

    public static final Iso11649 INSTANCE = new Iso11649();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("iso11649", "Creditor Reference")
                    .title("ISO 11649 structured creditor reference")
                    .description("Payment reference: RF, two MOD 97-10 check digits and up"
                            + " to 21 characters.")
                    .tags(Tag.PAYMENT, Tag.BANK)
                    .references("https://en.wikipedia.org/wiki/Creditor_Reference")
                    .build();

    private Iso11649() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " -.,/:").toUpperCase(Locale.ROOT);
    }

    /** The two check digits that make {@code RF + digits + reference} valid. */
    public static String calcCheckDigits(String reference) {
        return Mod97.calcCheckDigits(INSTANCE.compact(reference) + "RF");
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (n.length() < 5 || n.length() > 25) {
            throw new InvalidLengthException();
        }
        if (!n.startsWith("RF")) {
            throw new InvalidFormatException(Message.of(Iso11649.class, "iso11649.prefix",
                    "A creditor reference starts with RF."));
        }
        Mod97.validate(n.substring(4) + n.substring(0, 4));
        return n;
    }

    @Override
    public String format(String number) {
        String n = validate(number);
        StringBuilder sb = new StringBuilder(n.length() + n.length() / 4);
        for (int i = 0; i < n.length(); i += 4) {
            if (i > 0) {
                sb.append(' ');
            }
            sb.append(n, i, Math.min(i + 4, n.length()));
        }
        return sb.toString();
    }
}
