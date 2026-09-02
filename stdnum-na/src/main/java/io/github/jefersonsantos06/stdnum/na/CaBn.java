package io.github.jefersonsantos06.stdnum.na;

import io.github.jefersonsantos06.stdnum.algo.Luhn;
import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidComponentException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.Message;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.util.Locale;

/**
 * BN (Canadian Business Number): nine Luhn-checked digits, optionally
 * followed by a two-letter program identifier ({@code RC}, {@code RM},
 * {@code RP} or {@code RT}) and a four-digit reference number, forming the
 * fifteen-character program account (BN15).
 */
public final class CaBn implements StdNum {

    public static final CaBn INSTANCE = new CaBn();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("ca.bn", "BN")
                    .country("CA")
                    .title("Business Number")
                    .description("Canadian business number: 9 Luhn-checked digits, or the"
                            + " 15-character program account.")
                    .tags(Tag.COMPANY, Tag.TAX)
                    .build();

    private CaBn() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, "- ").toUpperCase(Locale.ROOT);
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (n.length() != 9 && n.length() != 15) {
            throw new InvalidLengthException();
        }
        if (!Strings.isDigits(n.substring(0, 9))) {
            throw new InvalidFormatException();
        }
        Luhn.validate(n.substring(0, 9));
        if (n.length() == 15) {
            String program = n.substring(9, 11);
            if (!program.equals("RC") && !program.equals("RM")
                    && !program.equals("RP") && !program.equals("RT")) {
                throw new InvalidComponentException(Message.of(CaBn.class, "bn.program",
                        "Unknown program identifier."));
            }
            if (!Strings.isDigits(n.substring(11))) {
                throw new InvalidFormatException();
            }
        }
        return n;
    }
}
