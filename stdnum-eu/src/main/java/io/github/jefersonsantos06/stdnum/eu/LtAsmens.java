package io.github.jefersonsantos06.stdnum.eu;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.time.LocalDate;

/**
 * Asmens kodas, the Lithuanian personal number: eleven digits laid out and
 * checked exactly as the Estonian {@link EeIk}, with the first digit giving
 * the sex and the century of birth.
 *
 * <p>A number opening with 9 is issued without a date of birth, so the date
 * is checked for every other number only.</p>
 */
public final class LtAsmens implements StdNum {

    public static final LtAsmens INSTANCE = new LtAsmens();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("lt.asmens", "Asmens kodas")
                    .country("LT")
                    .title("Lietuvos asmens kodas")
                    .description("Lithuanian personal number: 11 digits giving the sex, the"
                            + " date of birth and a weighted mod 11 check digit.")
                    .tags(Tag.PERSON)
                    .references("https://lt.wikipedia.org/wiki/Asmens_kodas")
                    .build();

    private LtAsmens() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " ");
    }

    /** The birth date encoded in the number, which a number starting with 9 does not carry. */
    public static LocalDate getBirthDate(String number) {
        return EeIk.getBirthDate(number);
    }

    /** The check digit of a number, from every digit but its last. */
    public static char calcCheckDigit(String number) {
        return EeIk.calcCheckDigit(number);
    }

    @Override
    public String validate(String number) {
        return validate(number, true);
    }

    /**
     * Validates the number, optionally without requiring the date of birth to
     * be a real date. Numbers issued to people whose birth date was unknown
     * carry one that is not.
     */
    public String validate(String number, boolean validateBirthDate) {
        String n = compact(number);
        if (!Strings.isDigits(n)) {
            throw new InvalidFormatException();
        }
        if (n.length() != 11) {
            throw new InvalidLengthException();
        }
        if (validateBirthDate && n.charAt(0) != '9') {
            getBirthDate(n);
        }
        if (n.charAt(10) != calcCheckDigit(n)) {
            throw new InvalidChecksumException();
        }
        return n;
    }
}
