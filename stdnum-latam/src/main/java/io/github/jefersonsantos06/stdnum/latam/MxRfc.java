package io.github.jefersonsantos06.stdnum.latam;

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
 * RFC (Registro Federal de Contribuyentes), the Mexican tax number: four
 * name letters, a six-digit date and a three-character "homoclave" for
 * individuals, or three name letters for companies. The number may also
 * appear as ten or twelve characters without the homoclave.
 *
 * <p>The check character closes the homoclave and is verified whenever it
 * is present. The dictionary of forbidden name prefixes that Mexican law
 * excludes is not applied.</p>
 */
public final class MxRfc implements StdNum {

    public static final MxRfc INSTANCE = new MxRfc();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("mx.rfc", "RFC")
                    .country("MX")
                    .title("Registro Federal de Contribuyentes")
                    .description("Mexican tax number: 12 characters for companies or 13 for"
                            + " individuals, with a mod 11 check character.")
                    .tags(Tag.TAX, Tag.VAT)
                    .build();

    private static final Pattern PERSONAL = Pattern.compile("[A-Z&Ñ]{4}[0-9]{6}[0-9A-Z]{0,3}");
    private static final Pattern COMPANY = Pattern.compile("[A-Z&Ñ]{3}[0-9]{6}[0-9A-Z]{3}");
    private static final Pattern HOMOCLAVE = Pattern.compile("[1-9A-V][1-9A-Z][0-9A]");

    private static final String ALPHABET = "0123456789ABCDEFGHIJKLMN&OPQRSTUVWXYZ Ñ";

    private MxRfc() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, "-_ ").toUpperCase(Locale.ROOT);
    }

    /** The check character, computed over everything before it. */
    public static char calcCheckDigit(String base) {
        String b = INSTANCE.compact(base);
        // the calculation is right-aligned on a 12-character field
        String padded = b.length() < 12 ? " ".repeat(12 - b.length()) + b : b;
        int check = 0;
        for (int i = 0; i < padded.length(); i++) {
            int value = ALPHABET.indexOf(padded.charAt(i));
            if (value < 0) {
                throw new InvalidFormatException();
            }
            check += value * (13 - i);
        }
        check = 11 - check % 11;
        return check == 11 ? '0' : check == 10 ? 'A' : (char) ('0' + check);
    }

    /** The birth or incorporation date encoded in the number. */
    public static LocalDate getDate(String number) {
        String n = INSTANCE.validate(number);
        int offset = n.length() == 12 || n.length() == 10 ? 3 : 4;
        int year = Integer.parseInt(n.substring(offset, offset + 2));
        int month = Integer.parseInt(n.substring(offset + 2, offset + 4));
        int day = Integer.parseInt(n.substring(offset + 4, offset + 6));
        try {
            // two-digit years are ambiguous; assume the 1900s as the tax
            // authority's own tooling does for legacy numbers
            return LocalDate.of(year + 1900, month, day);
        } catch (DateTimeException e) {
            throw new InvalidComponentException("The number does not contain a valid date.");
        }
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (n.length() == 10 || n.length() == 13) {
            if (!PERSONAL.matcher(n).matches()) {
                throw new InvalidFormatException();
            }
        } else if (n.length() == 12) {
            if (!COMPANY.matcher(n).matches()) {
                throw new InvalidFormatException();
            }
        } else {
            throw new InvalidLengthException();
        }
        if (n.length() >= 12) {
            String homoclave = n.substring(n.length() - 3);
            if (!HOMOCLAVE.matcher(homoclave).matches()) {
                throw new InvalidComponentException("Malformed homoclave.");
            }
            if (n.charAt(n.length() - 1) != calcCheckDigit(n.substring(0, n.length() - 1))) {
                throw new InvalidChecksumException();
            }
        }
        return n;
    }

    @Override
    public String format(String number) {
        String n = validate(number);
        int split = n.length() == 12 || n.length() == 10 ? 3 : 4;
        return n.length() <= 10
                ? n.substring(0, split) + " " + n.substring(split)
                : n.substring(0, split) + " " + n.substring(split, split + 6)
                        + " " + n.substring(split + 6);
    }
}
