package io.github.jefersonsantos06.stdnum.eu;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

/**
 * ICO, the Slovak organisation identification number. Slovakia kept the
 * Czechoslovak scheme, so the format and the check digit are those of the
 * Czech {@link CzIco}.
 */
public final class SkIco implements StdNum {

    public static final SkIco INSTANCE = new SkIco();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("sk.ico", "ICO")
                    .country("SK")
                    .title("Identifikacne cislo organizacie")
                    .description("Slovak organisation identification number: 8 digits with a"
                            + " weighted mod 11 check digit.")
                    .tags(Tag.COMPANY, Tag.TAX)
                    .references("https://sk.wikipedia.org/wiki/"
                            + "Identifika%C4%8Dn%C3%A9_%C4%8D%C3%ADslo_organiz%C3%A1cie")
                    .build();

    private SkIco() {
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
