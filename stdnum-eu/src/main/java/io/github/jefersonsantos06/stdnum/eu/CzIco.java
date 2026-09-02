package io.github.jefersonsantos06.stdnum.eu;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

/**
 * ICO, the Czech organisation identification number: eight digits with a
 * weighted mod 11 check digit. It is what the Czech VAT number ({@link CzDic})
 * of a legal entity is built on.
 */
public final class CzIco implements StdNum {

    public static final CzIco INSTANCE = new CzIco();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("cz.ico", "ICO")
                    .country("CZ")
                    .title("Identifikacni cislo osoby")
                    .description("Czech organisation identification number: 8 digits with a"
                            + " weighted mod 11 check digit.")
                    .tags(Tag.COMPANY, Tag.TAX)
                    .references("https://cs.wikipedia.org/wiki/"
                            + "Identifika%C4%8Dn%C3%AD_%C4%8D%C3%ADslo_osoby")
                    .build();

    private CzIco() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " /");
    }

    /** The check digit of an 8-digit number, from its first seven digits. */
    public static char calcCheckDigit(String base) {
        return CzDic.calcCheckDigitLegal(base);
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (!Strings.isDigits(n)) {
            throw new InvalidFormatException();
        }
        if (n.length() != 8) {
            throw new InvalidLengthException();
        }
        if (n.charAt(7) != calcCheckDigit(n.substring(0, 7))) {
            throw new InvalidChecksumException();
        }
        return n;
    }
}
