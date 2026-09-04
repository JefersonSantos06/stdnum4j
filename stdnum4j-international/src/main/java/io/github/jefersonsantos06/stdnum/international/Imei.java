package io.github.jefersonsantos06.stdnum.international;

import io.github.jefersonsantos06.stdnum.algo.Luhn;
import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.spi.ValidationException;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.util.Locale;
import java.util.Optional;

/**
 * IMEI (International Mobile Equipment Identity), the identifier of mobile
 * phones: 14 digits without the check digit, 15 with it (Luhn-checked), or
 * 16 for an IMEISV, which carries a software version instead.
 */
public final class Imei implements StdNum {

    public static final Imei INSTANCE = new Imei();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("imei", "IMEI")
                    .title("International Mobile Equipment Identity")
                    .description("Mobile phone identifier: 14, 15 (Luhn-checked) or 16"
                            + " digits (IMEISV).")
                    .tags(Tag.TELECOM)
                    .references("https://en.wikipedia.org/wiki/International_Mobile_Equipment_Identity")
                    .build();

    private Imei() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " -").toUpperCase(Locale.ROOT);
    }

    /** The two kinds of number: the identifier alone, or with a version. */
    public enum Type { IMEI, IMEISV }

    /** Which kind the number is, or empty when it is not a valid IMEI. */
    public static Optional<Type> imeiType(String number) {
        try {
            return Optional.of(INSTANCE.validate(number).length() == 16
                    ? Type.IMEISV : Type.IMEI);
        } catch (ValidationException e) {
            return Optional.empty();
        }
    }

    /** Whether the number carries a software version instead of a check digit. */
    public static boolean isImeiSv(String number) {
        return INSTANCE.validate(number).length() == 16;
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (!Strings.isDigits(n)) {
            throw new InvalidFormatException();
        }
        if (n.length() == 15) {
            // only the 15-digit form carries a check digit
            Luhn.validate(n);
        } else if (n.length() != 14 && n.length() != 16) {
            throw new InvalidLengthException();
        }
        return n;
    }

    @Override
    public String format(String number) {
        String n = validate(number);
        return n.substring(0, 2) + "-" + n.substring(2, 8) + "-"
                + n.substring(8, 14) + (n.length() > 14 ? "-" + n.substring(14) : "");
    }

    /** The three parts of an IMEI: allocation code, serial and tail. */
    public record Parts(String typeAllocationCode, String serialNumber, String tail) {
    }

    /**
     * Splits the number into its Type Allocation Code, serial number and
     * tail — the check digit for an IMEI, the software version for an
     * IMEISV, and empty for the 14-digit form.
     */
    public static Parts split(String number) {
        String n = INSTANCE.validate(number);
        return new Parts(n.substring(0, 8), n.substring(8, 14), n.substring(14));
    }

}
