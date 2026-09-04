package io.github.jefersonsantos06.stdnum.eu;

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
 * AHV-Nummer (numero AVS), the Swiss social security number: thirteen digits
 * opening with the 756 country prefix and closing with an EAN-13 check digit.
 * Since 2008 the digits in between are drawn at random, so the number carries
 * no personal information.
 */
public final class ChSsn implements StdNum {

    public static final ChSsn INSTANCE = new ChSsn();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("ch.ssn", "AHV-Nr.")
                    .country("CH")
                    .title("Schweizer Sozialversicherungsnummer")
                    .description("Swiss social security number: the 756 prefix and 10 further"
                            + " digits, the last an EAN-13 check digit.")
                    .tags(Tag.PERSON, Tag.HEALTH)
                    .references("https://en.wikipedia.org/wiki/National_identification_number#Switzerland")
                    .build();

    private static final Mask MASK = Mask.of("###.####.####.##");

    private ChSsn() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " .");
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (n.length() != 13) {
            throw new InvalidLengthException();
        }
        if (!Strings.isDigits(n)) {
            throw new InvalidFormatException();
        }
        if (!n.startsWith("756")) {
            throw new InvalidComponentException(Message.of(ChSsn.class, "ssn.prefix",
                    "Swiss social security numbers start with 756."));
        }
        if (n.charAt(12) != eanCheckDigit(n)) {
            throw new InvalidChecksumException();
        }
        return n;
    }

    /**
     * The EAN-13 check digit, from the first twelve digits. Spelled out here
     * because this module deliberately does not depend on the international
     * one, where the EAN implementation lives.
     */
    private static char eanCheckDigit(String n) {
        int sum = 0;
        for (int i = 0; i < 12; i++) {
            sum += (i % 2 == 0 ? 1 : 3) * (n.charAt(i) - '0');
        }
        return (char) ('0' + (10 - sum % 10) % 10);
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
