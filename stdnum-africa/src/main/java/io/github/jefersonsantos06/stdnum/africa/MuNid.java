package io.github.jefersonsantos06.stdnum.africa;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidComponentException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * NID, the Mauritian national identity number: the first letter of the
 * holder's surname at birth, a six-digit birth date, a six-digit serial
 * and a check character taken modulo 17.
 */
public final class MuNid implements StdNum {

    public static final MuNid INSTANCE = new MuNid();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("mu.nid", "NID")
                    .country("MU")
                    .title("Mauritian National Identity number")
                    .description("Mauritian identity number: a surname initial, a birth date,"
                            + " a serial and a mod 17 check character.")
                    .tags(Tag.PERSON)
                    .build();

    private static final Pattern STRUCTURE = Pattern.compile("[A-Z][0-9]{12}[0-9A-Z]");
    private static final String ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private MuNid() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " -").toUpperCase(Locale.ROOT);
    }

    /** The check character, computed over the first thirteen characters. */
    public static char calcCheckDigit(String number) {
        String n = INSTANCE.compact(number);
        int sum = 0;
        for (int i = 0; i < 13 && i < n.length(); i++) {
            int value = ALPHABET.indexOf(n.charAt(i));
            if (value < 0) {
                throw new InvalidFormatException();
            }
            sum += (14 - i) * value;
        }
        return ALPHABET.charAt(Math.floorMod(17 - sum, 17));
    }

    /** The birth date encoded in the number; the century may be wrong. */
    public static LocalDate getBirthDate(String number) {
        String n = INSTANCE.compact(number);
        if (n.length() != 14) {
            throw new InvalidLengthException();
        }
        try {
            return LocalDate.of(
                    Integer.parseInt(n.substring(5, 7)) + 2000,
                    Integer.parseInt(n.substring(3, 5)),
                    Integer.parseInt(n.substring(1, 3)));
        } catch (DateTimeException | NumberFormatException e) {
            throw new InvalidComponentException(
                    "The number does not contain a valid birth date.");
        }
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (n.length() != 14) {
            throw new InvalidLengthException();
        }
        if (!STRUCTURE.matcher(n).matches()) {
            throw new InvalidFormatException();
        }
        if (n.charAt(13) != calcCheckDigit(n)) {
            throw new InvalidChecksumException();
        }
        getBirthDate(n);
        return n;
    }
}
