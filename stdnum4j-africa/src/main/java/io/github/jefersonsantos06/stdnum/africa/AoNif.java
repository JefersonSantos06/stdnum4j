package io.github.jefersonsantos06.stdnum.africa;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidComponentException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.Message;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * NIF (Número de Identificação Fiscal), the Angolan tax number, in the form
 * Decree 366/17 gave it. A company's is a sequential number of ten digits
 * that opens with 5; the 7 of institutional bodies was folded into it, but
 * older documents still carry it. A national's NIF is the number of their
 * identity card (bilhete de identidade), fourteen characters — nine digits,
 * the two letters of the province and three digits, {@code 004797863LA048}.
 *
 * <p>The NIF of a foreign resident is the number of their residence card,
 * whose form is not published, and is not covered. Neither form carries a
 * check digit, so only the shape is checked.</p>
 */
public final class AoNif implements StdNum {

    public static final AoNif INSTANCE = new AoNif();

    private static final Pattern IDENTITY_CARD = Pattern.compile("[0-9]{9}[A-Z]{2}[0-9]{3}");

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("ao.nif", "NIF")
                    .country("AO")
                    .title("Número de Identificação Fiscal")
                    .description("Angolan tax number: 10 digits opening with 5 for a company, or a"
                            + " national's 14-character identity card number.")
                    .tags(Tag.TAX, Tag.VAT)
                    .references(
                            "https://github.com/arthurdejong/python-stdnum/issues/367",
                            "https://github.com/koblas/stdnum-js/blob/main/src/ao/nif.ts")
                    .build();

    private AoNif() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " -.").toUpperCase(Locale.ROOT);
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (n.length() == 14) {
            if (!IDENTITY_CARD.matcher(n).matches()) {
                throw new InvalidFormatException();
            }
            return n;
        }
        if (n.length() != 10) {
            throw new InvalidLengthException();
        }
        if (!Strings.isDigits(n)) {
            throw new InvalidFormatException();
        }
        if (n.charAt(0) != '5' && n.charAt(0) != '7') {
            throw new InvalidComponentException(Message.of(AoNif.class, "nif.ao-company",
                    "A company's NIF opens with 5, or 7 in older documents."));
        }
        return n;
    }
}
