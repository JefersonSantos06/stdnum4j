package io.github.jefersonsantos06.stdnum.algo;

import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.Reasons;

import java.nio.charset.StandardCharsets;

/**
 * CRC-16/CCITT-FALSE, the cyclic redundancy check used by the EMV QR code
 * specification and therefore by the Brazilian Pix BR Code.
 *
 * <p>Parameters: polynomial {@code 0x1021}, initial value {@code 0xFFFF},
 * no input or output reflection, no final XOR.</p>
 */
public final class Crc16 {

    private Crc16() {
    }

    private static final int POLYNOMIAL = 0x1021;
    private static final int INITIAL = 0xFFFF;

    /** The CRC of the UTF-8 bytes of {@code text}, as an unsigned 16-bit value. */
    public static int checksum(String text) {
        if (text == null) {
            throw new InvalidFormatException(Reasons.nullText());
        }
        return checksum(text.getBytes(StandardCharsets.UTF_8));
    }

    /** The CRC of the given bytes, as an unsigned 16-bit value. */
    public static int checksum(byte[] bytes) {
        int crc = INITIAL;
        for (byte b : bytes) {
            crc ^= (b & 0xFF) << 8;
            for (int bit = 0; bit < 8; bit++) {
                crc = (crc & 0x8000) != 0 ? (crc << 1) ^ POLYNOMIAL : crc << 1;
                crc &= 0xFFFF;
            }
        }
        return crc;
    }

    /** The CRC of {@code text} as four upper-case hexadecimal characters. */
    public static String hex(String text) {
        return String.format("%04X", checksum(text));
    }
}
