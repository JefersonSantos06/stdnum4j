package io.github.jefersonsantos06.stdnum.international;

import io.github.jefersonsantos06.stdnum.algo.Luhn;
import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidComponentException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

/**
 * MEID, the identifier of a CDMA mobile handset: a manufacturer code and a
 * serial number, written either as fourteen hexadecimal digits or as
 * eighteen decimal ones.
 *
 * <p>The two forms carry different check digits — the decimal one an
 * ordinary Luhn, the hexadecimal one a Luhn over base 16 — so converting
 * between them means recomputing it. A hexadecimal MEID that happens to be
 * all digits is an {@link Imei} and is checked as one.</p>
 */
public final class Meid implements StdNum {

    public static final Meid INSTANCE = new Meid();

    private static final String HEX = "0123456789ABCDEF";

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("meid", "MEID")
                    .title("Mobile Equipment Identifier")
                    .description("Identifier of a CDMA mobile handset: 14 hexadecimal digits,"
                            + " or the same number written as 18 decimal ones.")
                    .tags(Tag.TELECOM, Tag.PRODUCT)
                    .references("https://en.wikipedia.org/wiki/Mobile_equipment_identifier")
                    .build();

    private Meid() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    /** The number split into its body and the check digit it may carry. */
    private static String[] parse(String number) {
        String n = Strings.compact(number, " -").toUpperCase(Locale.ROOT);
        if (n.length() == 14 || n.length() == 15) {
            for (int i = 0; i < n.length(); i++) {
                if (HEX.indexOf(n.charAt(i)) < 0) {
                    throw new InvalidFormatException();
                }
            }
            return new String[] {n.substring(0, 14), n.substring(14)};
        }
        if (n.length() == 18 || n.length() == 19) {
            if (!Strings.isDigits(n)) {
                throw new InvalidFormatException();
            }
            return new String[] {n.substring(0, 18), n.substring(18)};
        }
        throw new InvalidLengthException();
    }

    /** The check digit of a body, in whichever base it is written. */
    public static char calcCheckDigit(String number) {
        String n = Strings.compact(number, " -").toUpperCase(Locale.ROOT);
        return Strings.isDigits(n) ? Luhn.calcCheckDigit(n) : Luhn.calcCheckDigit(n, HEX);
    }

    /**
     * The pseudo ESN of the handset: the reserved manufacturer code 80
     * followed by the last three bytes of the SHA-1 of the number. The ESN
     * space ran out, and a pseudo ESN is what a device with only an MEID
     * presents to a network that still expects one.
     */
    public static String toPseudoEsn(String number) {
        String n = INSTANCE.validate(number);
        byte[] bytes = new byte[7];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) Integer.parseInt(n.substring(i * 2, i * 2 + 2), 16);
        }
        byte[] digest;
        try {
            digest = MessageDigest.getInstance("SHA-1").digest(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Every Java runtime provides SHA-1.", e);
        }
        StringBuilder sb = new StringBuilder("80");
        for (int i = digest.length - 3; i < digest.length; i++) {
            sb.append(String.format("%02X", digest[i]));
        }
        return sb.toString();
    }

    /** The eighteen decimal digits as the fourteen hexadecimal ones. */
    private static String toHex(String decimal) {
        long manufacturer = Long.parseLong(decimal.substring(0, 10));
        long serial = Long.parseLong(decimal.substring(10, 18));
        if (manufacturer > 0xFFFFFFFFL || serial > 0xFFFFFFL) {
            throw new InvalidComponentException(
                    "The number does not fit the manufacturer and serial fields.");
        }
        return String.format("%08X%06X", manufacturer, serial);
    }

    /**
     * {@inheritDoc}
     *
     * <p>A decimal number is converted to hexadecimal, and the check digit,
     * which differs between the two forms, is dropped.</p>
     */
    @Override
    public String compact(String number) {
        String[] parts = parse(number);
        return parts[0].length() == 18 ? toHex(parts[0]) : parts[0];
    }

    @Override
    public String validate(String number) {
        String[] parts = parse(number);
        String body = parts[0];
        String check = parts[1];
        if (body.length() == 18) {
            if (!check.isEmpty()) {
                Luhn.validate(body + check);
            }
            return toHex(body);
        }
        if (Strings.isDigits(body)) {
            // an all-digit hexadecimal MEID is an IMEI
            Imei.INSTANCE.validate(body + check);
        } else if (!check.isEmpty()) {
            Luhn.validate(body + check, HEX);
        }
        return body;
    }

    @Override
    public String format(String number) {
        String n = validate(number);
        StringBuilder sb = new StringBuilder(20);
        for (int i = 0; i < n.length(); i += 2) {
            if (i > 0) {
                sb.append(' ');
            }
            sb.append(n, i, i + 2);
        }
        return sb.toString();
    }
}
