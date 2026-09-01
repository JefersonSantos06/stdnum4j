package io.github.jefersonsantos06.stdnum.latam;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidComponentException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.util.Locale;

/**
 * CI (Cédula de identidad), the Ecuadorian personal identity code: ten
 * digits opening with a province code (01-24, 30 or 50), a third digit
 * below 7, and a Luhn-style folded checksum.
 */
public final class EcCi implements StdNum {

    public static final EcCi INSTANCE = new EcCi();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("ec.ci", "CI")
                    .country("EC")
                    .title("Cédula de identidad")
                    .description("Ecuadorian personal identity code: 10 digits with a"
                            + " province code and a folded mod 10 checksum.")
                    .tags(Tag.PERSON)
                    .build();

    private EcCi() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " -").toUpperCase(Locale.ROOT);
    }

    /** The folded checksum; valid numbers yield 0. */
    static int checksum(String number) {
        int sum = 0;
        for (int i = 0; i < number.length(); i++) {
            int value = (i % 2 == 0 ? 2 : 1) * (number.charAt(i) - '0');
            sum += value > 9 ? value - 9 : value;
        }
        return sum % 10;
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (n.length() != 10) {
            throw new InvalidLengthException();
        }
        if (!Strings.isDigits(n)) {
            throw new InvalidFormatException();
        }
        String province = n.substring(0, 2);
        boolean knownProvince = (province.compareTo("01") >= 0 && province.compareTo("24") <= 0)
                || province.equals("30") || province.equals("50");
        if (!knownProvince) {
            throw new InvalidComponentException("Unknown province code.");
        }
        if (n.charAt(2) > '6') {
            throw new InvalidComponentException("The third digit must be 6 or lower.");
        }
        if (checksum(n) != 0) {
            throw new InvalidChecksumException();
        }
        return n;
    }
}
