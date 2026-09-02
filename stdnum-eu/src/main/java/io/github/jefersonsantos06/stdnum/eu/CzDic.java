package io.github.jefersonsantos06.stdnum.eu;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidComponentException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.Message;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.util.Locale;

/**
 * DIČ (Daňové identifikační číslo), the Czech VAT number. Three shapes:
 * eight digits for legal entities, nine digits starting with {@code 6} for
 * individuals without a birth number, and the nine or ten digit birth
 * number ({@link CzRc}) itself.
 */
public final class CzDic implements StdNum {

    public static final CzDic INSTANCE = new CzDic();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("cz.dic", "DIČ")
                    .country("CZ")
                    .title("Daňové identifikační číslo")
                    .description("Czech VAT number: 8 digits for legal entities, or a 9/10"
                            + " digit individual number.")
                    .tags(Tag.VAT)
                    .build();

    private CzDic() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        String n = Strings.compact(number, " /").toUpperCase(Locale.ROOT);
        return n.startsWith("CZ") ? n.substring(2) : n;
    }

    /** The check digit of an 8-digit legal entity number, from its first seven digits. */
    public static char calcCheckDigitLegal(String base) {
        int sum = 0;
        for (int i = 0; i < base.length(); i++) {
            sum += (8 - i) * (base.charAt(i) - '0');
        }
        int check = Math.floorMod(11 - sum, 11);
        return (char) ('0' + (check == 0 ? 1 : check) % 10);
    }

    /** The check digit of a 9-digit special number, from its middle seven digits. */
    public static char calcCheckDigitSpecial(String base) {
        int sum = 0;
        for (int i = 0; i < base.length(); i++) {
            sum += (8 - i) * (base.charAt(i) - '0');
        }
        return (char) ('0' + Math.floorMod(8 - (10 - sum % 11) % 11, 10));
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (!Strings.isDigits(n)) {
            throw new InvalidFormatException();
        }
        if (n.length() == 8) {
            if (n.charAt(0) == '9') {
                throw new InvalidComponentException(Message.of(CzDic.class, "dic.legal-entity-prefix",
                        "A legal entity DIČ does not start with 9."));
            }
            if (n.charAt(7) != calcCheckDigitLegal(n.substring(0, 7))) {
                throw new InvalidChecksumException();
            }
        } else if (n.length() == 9 && n.charAt(0) == '6') {
            if (n.charAt(8) != calcCheckDigitSpecial(n.substring(1, 8))) {
                throw new InvalidChecksumException();
            }
        } else if (n.length() == 9 || n.length() == 10) {
            CzRc.INSTANCE.validate(n);
        } else {
            throw new InvalidLengthException();
        }
        return n;
    }
}
