package io.github.jefersonsantos06.stdnum.africa;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
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
 * NIF (Número de Identificação Fiscal), the Cape Verdean tax number: nine
 * digits, the first giving the kind of taxpayer — 1 a person, 2 a company,
 * 3 a national entity, 4 an international one, 5 any other — and the last a
 * check digit.
 *
 * <p>The official descriptions call the last two digits control digits
 * without saying how they are formed. The last one is the check digit of
 * Portugal's NIF: the first eight digits weighted 9 down to 2, subtracted
 * from 11 modulo 11, with 10 and 11 written as 0. It holds for 212 of the 214
 * NIFs gathered from public documents for python-stdnum pull request #391.</p>
 */
public final class CvNif implements StdNum {

    public static final CvNif INSTANCE = new CvNif();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("cv.nif", "NIF")
                    .country("CV")
                    .title("Número de Identificação Fiscal")
                    .description("Cape Verdean tax number: 9 digits, the first the kind of taxpayer"
                            + " and the last a weighted mod 11 check digit.")
                    .tags(Tag.TAX, Tag.VAT)
                    .references("https://github.com/arthurdejong/python-stdnum/pull/391")
                    .build();

    private static final Mask MASK = Mask.of("999 999 999");

    private CvNif() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " -.");
    }

    /** The check digit for the eight digits that come before it. */
    public static char calcCheckDigit(String base) {
        Strings.requireDigits(base, 8);
        int sum = 0;
        for (int i = 0; i < 8; i++) {
            sum += (9 - i) * (base.charAt(i) - '0');
        }
        int check = 11 - sum % 11;
        return (char) ('0' + (check >= 10 ? 0 : check));
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (!Strings.isDigits(n)) {
            throw new InvalidFormatException();
        }
        if (n.length() != 9) {
            throw new InvalidLengthException();
        }
        if (n.charAt(0) < '1' || n.charAt(0) > '5') {
            throw new InvalidComponentException(Message.of(CvNif.class, "nif.cv-kind",
                    "The first digit gives no kind of taxpayer."));
        }
        if (n.charAt(8) != calcCheckDigit(n.substring(0, 8))) {
            throw new InvalidChecksumException();
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
