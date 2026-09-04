package io.github.jefersonsantos06.stdnum.eu;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.util.Locale;

/**
 * DNI (Documento Nacional de Identidad), the Spanish personal identity
 * number: eight digits followed by a check letter
 * ({@code "TRWAGMYFPDXBNJZSQVHLCKE"[number mod 23]}).
 */
public final class EsDni implements StdNum {

    public static final EsDni INSTANCE = new EsDni();

    static final String CHECK_LETTERS = "TRWAGMYFPDXBNJZSQVHLCKE";

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("es.dni", "DNI")
                    .country("ES")
                    .title("Documento Nacional de Identidad")
                    .description("Spanish personal identity number: 8 digits and a mod 23"
                            + " check letter.")
                    .tags(Tag.PERSON)
                    .build();

    private EsDni() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " -").toUpperCase(Locale.ROOT);
    }

    /** The check letter for the given digits. */
    static char checkLetter(String digits) {
        return CHECK_LETTERS.charAt(Integer.parseInt(digits) % 23);
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (n.isEmpty() || !Strings.isDigits(n.substring(0, n.length() - 1))) {
            throw new InvalidFormatException();
        }
        if (n.length() != 9) {
            throw new InvalidLengthException();
        }
        if (n.charAt(8) != checkLetter(n.substring(0, 8))) {
            throw new InvalidChecksumException();
        }
        return n;
    }

    /** The check letter for the eight-digit base. */
    public static char calcCheckDigit(String base) {
        String b = INSTANCE.compact(base);
        if (!Strings.isDigits(b)) {
            throw new InvalidFormatException();
        }
        if (b.length() != 8) {
            throw new InvalidLengthException();
        }
        return checkLetter(b);
    }

}
