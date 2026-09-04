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
 * The serial number printed on a euro banknote: a letter for the printer, a
 * second character, and ten digits. The whole of it, with letters counted by
 * their character code, is a multiple of nine.
 */
public final class EuBanknote implements StdNum {

    public static final EuBanknote INSTANCE = new EuBanknote();

    /** The printer codes in use; I, K, O and Q are never printed. */
    private static final String PRINTERS = "BCDEFGHJLMNPRSTUVWXYZ";

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("eu.banknote", "Banknote serial")
                    .title("Euro banknote serial number")
                    .description("Serial number of a euro banknote: a printer letter, one more"
                            + " character and 10 digits, summing to a multiple of nine.")
                    .tags(Tag.OTHER)
                    .references("https://en.wikipedia.org/wiki/Euro_banknotes#Serial_number")
                    .build();

    private EuBanknote() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " ").toUpperCase(Locale.ROOT);
    }

    /** The sum modulo nine; a valid serial yields 0. */
    public static int checksum(String number) {
        String n = INSTANCE.compact(number);
        int sum = 0;
        for (int i = 0; i < n.length(); i++) {
            char c = n.charAt(i);
            sum += c >= '0' && c <= '9' ? c - '0' : c;
        }
        return sum % 9;
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (n.length() < 2 || !Character.isLetterOrDigit(n.charAt(0))
                || !Character.isLetterOrDigit(n.charAt(1))
                || !Strings.isDigits(n.substring(2))) {
            throw new InvalidFormatException();
        }
        if (n.length() != 12) {
            throw new InvalidLengthException();
        }
        if (PRINTERS.indexOf(n.charAt(0)) < 0) {
            throw new InvalidComponentException(Message.of(EuBanknote.class, "banknote.printer",
                    "Not the code of a printer."));
        }
        if (checksum(n) != 0) {
            throw new InvalidChecksumException();
        }
        return n;
    }
}
