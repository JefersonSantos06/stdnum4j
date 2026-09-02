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
 * OGRN, the Russian primary state registration number: thirteen digits for a
 * company, or fifteen for a sole trader, whose last digit is the number
 * before it taken modulo 11 or 13.
 */
public final class RuOgrn implements StdNum {

    public static final RuOgrn INSTANCE = new RuOgrn();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("ru.ogrn", "OGRN")
                    .country("RU")
                    .title("Osnovnoy gosudarstvennyy registratsionnyy nomer")
                    .description("Russian state registration number: 13 digits for a company"
                            + " or 15 for a sole trader, with a modulo check digit.")
                    .tags(Tag.COMPANY)
                    .references("https://en.wikipedia.org/wiki/"
                            + "Primary_State_Registration_Number")
                    .build();

    private RuOgrn() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " ");
    }

    /**
     * The check digit of a number, from every digit but its last. A
     * thirteen-digit number folds modulo 11 and a fifteen-digit one modulo 13.
     *
     * @throws InvalidChecksumException when the fifteen-digit fold leaves a
     *                                  remainder above 9, for which no check
     *                                  digit exists
     */
    public static char calcCheckDigit(String number) {
        String n = INSTANCE.compact(number);
        long head = Long.parseLong(n.substring(0, n.length() - 1));
        long check = n.length() == 13 ? head % 11 % 10 : head % 13;
        if (check > 9) {
            throw new InvalidChecksumException(Reasons.noCheckDigit());
        }
        return (char) ('0' + check);
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (!Strings.isDigits(n)) {
            throw new InvalidFormatException();
        }
        if (n.length() == 13) {
            if (n.charAt(0) == '0') {
                throw new InvalidComponentException(Message.of(RuOgrn.class, "ogrn.record-kind",
                        "The first digit names a kind of record."));
            }
        } else if (n.length() == 15) {
            if (n.charAt(0) != '3' && n.charAt(0) != '4') {
                throw new InvalidComponentException(Message.of(RuOgrn.class, "ogrn.sole-trader-prefix",
                        "A sole trader's number starts with 3 or 4."));
            }
        } else {
            throw new InvalidLengthException();
        }
        if (n.charAt(n.length() - 1) != calcCheckDigit(n)) {
            throw new InvalidChecksumException();
        }
        return n;
    }
}
