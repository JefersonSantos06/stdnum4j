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
 * VAT registration number of the United Kingdom (and the Isle of Man).
 *
 * <p>Four shapes are accepted: a 9-digit standard number, a 12-digit branch
 * trader number (the last three digits identify the branch and are not
 * checked), a 5-character government department ({@code GDnnn}, n &lt; 500)
 * or health authority ({@code HAnnn}, n ≥ 500) number, and the 11-character
 * {@code GD8888}/{@code HA8888} form. Standard numbers use the weighted
 * mod 97 checksum; numbers whose first three digits reach 100 belong to the
 * restarted series and accept the 55 offset as well.</p>
 */
public final class GbVat implements StdNum {

    public static final GbVat INSTANCE = new GbVat();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("gb.vat", "VAT")
                    .country("GB")
                    .title("United Kingdom VAT registration number")
                    .description("UK (and Isle of Man) VAT number: 9 or 12 digits with a"
                            + " weighted mod 97 checksum, or a GD/HA institutional number.")
                    .tags(Tag.VAT)
                    .build();

    private static final int[] WEIGHTS = {8, 7, 6, 5, 4, 3, 2, 10, 1};

    private GbVat() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        String n = Strings.compact(number, " -.").toUpperCase(Locale.ROOT);
        return n.startsWith("GB") || n.startsWith("XI") ? n.substring(2) : n;
    }

    /** The weighted mod 97 checksum of the first nine digits. */
    static int checksum(String number) {
        int sum = 0;
        for (int i = 0; i < WEIGHTS.length; i++) {
            sum += WEIGHTS[i] * (number.charAt(i) - '0');
        }
        return sum % 97;
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (n.length() == 5) {
            validateInstitutional(n, n.substring(2));
        } else if (n.length() == 11 && (n.startsWith("GD8888") || n.startsWith("HA8888"))) {
            validateInstitutional(n, n.substring(6, 9));
            if (!Strings.isDigits(n.substring(6))) {
                throw new InvalidFormatException();
            }
            if (Integer.parseInt(n.substring(6, 9)) % 97 != Integer.parseInt(n.substring(9))) {
                throw new InvalidChecksumException();
            }
        } else if (n.length() == 9 || n.length() == 12) {
            if (!Strings.isDigits(n)) {
                throw new InvalidFormatException();
            }
            int checksum = checksum(n.substring(0, 9));
            boolean restarted = Integer.parseInt(n.substring(0, 3)) >= 100;
            boolean valid = restarted
                    ? checksum == 0 || checksum == 42 || checksum == 55
                    : checksum == 0;
            if (!valid) {
                throw new InvalidChecksumException();
            }
        } else {
            throw new InvalidLengthException();
        }
        return n;
    }

    /** Government departments are below 500, health authorities from 500 up. */
    private static void validateInstitutional(String number, String digits) {
        if (!Strings.isDigits(digits)) {
            throw new InvalidFormatException();
        }
        int value = Integer.parseInt(digits);
        boolean valid = (number.startsWith("GD") && value < 500)
                || (number.startsWith("HA") && value >= 500);
        if (!valid) {
            throw new InvalidComponentException(Message.of(GbVat.class, "vat.gb.institutional",
                    "Not a valid government department or health authority number."));
        }
    }

    @Override
    public String format(String number) {
        String n = validate(number);
        if (n.length() == 5 || n.length() == 11) {
            // government department and health authority numbers are not grouped
            return n;
        }
        String grouped = n.substring(0, 3) + " " + n.substring(3, 7) + " " + n.substring(7, 9);
        return n.length() == 12 ? grouped + " " + n.substring(9) : grouped;
    }

}
