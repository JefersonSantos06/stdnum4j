package io.github.jefersonsantos06.stdnum.international;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidComponentException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.Message;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.Locale;

/**
 * A Bitcoin address, in either of the two forms in circulation: the older
 * base-58 one, which opens with a 1 or a 3 and ends in four bytes of a
 * double SHA-256, and the newer bech32 one, which opens with {@code bc1} and
 * carries a BCH code able to say where a mistyped character is.
 *
 * <p>Only witness version 0 is accepted, which is the version the bech32
 * checksum constant belongs to; a version 1 address (taproot) uses bech32m
 * and a different constant.</p>
 */
public final class Bitcoin implements StdNum {

    public static final Bitcoin INSTANCE = new Bitcoin();

    private static final String BASE58 =
            "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz";
    private static final String BECH32 = "qpzry9x8gf2tvdw0s3jn54khce6mua7l";
    private static final int[] BECH32_GENERATOR = {
            0x3b6a57b2, 0x26508e6d, 0x1ea119fa, 0x3d4233dd, 0x2a1462b3};
    /** The human-readable part of a mainnet bech32 address. */
    private static final String HRP = "bc";

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("bitcoin", "Bitcoin address")
                    .title("Bitcoin address")
                    .description("A Bitcoin address: base-58 with a double SHA-256 check, or"
                            + " bech32 with a BCH code.")
                    .tags(Tag.FINANCIAL, Tag.PAYMENT)
                    .references("https://en.bitcoin.it/wiki/Address")
                    .build();

    private Bitcoin() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    /**
     * {@inheritDoc}
     *
     * <p>A bech32 address is case insensitive and is lower-cased; a base-58
     * one is not, its case carrying information.</p>
     */
    @Override
    public String compact(String number) {
        String n = Strings.compact(number, " ");
        return n.length() >= 3 && n.substring(0, 3).equalsIgnoreCase("bc1")
                ? n.toLowerCase(Locale.ROOT)
                : n;
    }

    /** The bytes a base-58 string stands for. */
    public static byte[] base58Decode(String number) {
        BigInteger value = BigInteger.ZERO;
        BigInteger radix = BigInteger.valueOf(58);
        for (int i = 0; i < number.length(); i++) {
            int digit = BASE58.indexOf(number.charAt(i));
            if (digit < 0) {
                throw new InvalidFormatException();
            }
            value = value.multiply(radix).add(BigInteger.valueOf(digit));
        }
        ByteArrayOutputStream body = new ByteArrayOutputStream();
        // at least one byte is always emitted, even for a value of zero
        BigInteger byteRadix = BigInteger.valueOf(256);
        Deque<Integer> bytes = new ArrayDeque<>();
        while (value.compareTo(byteRadix) >= 0) {
            BigInteger[] divided = value.divideAndRemainder(byteRadix);
            value = divided[0];
            bytes.addFirst(divided[1].intValue());
        }
        bytes.addFirst(value.intValue());
        int leadingOnes = 0;
        while (leadingOnes < number.length() && number.charAt(leadingOnes) == '1') {
            leadingOnes++;
        }
        for (int i = 0; i < leadingOnes; i++) {
            body.write(0);
        }
        bytes.forEach(body::write);
        return body.toByteArray();
    }

    /** The BCH checksum of a bech32 address; a valid one yields 1. */
    public static int bech32Checksum(int[] values) {
        int check = 1;
        for (int value : values) {
            int top = check >>> 25;
            check = (check & 0x1ffffff) << 5 | value;
            for (int bit = 0; bit < BECH32_GENERATOR.length; bit++) {
                if ((top & 1 << bit) != 0) {
                    check ^= BECH32_GENERATOR[bit];
                }
            }
        }
        return check;
    }

    private static byte[] sha256(byte[] input) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(input);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is required of every Java platform", e);
        }
    }

    /** The five-bit groups of a bech32 address unpacked back into bytes. */
    private static byte[] fromFiveBitGroups(int[] data) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int accumulator = 0;
        int bits = 0;
        for (int value : data) {
            accumulator = (accumulator << 5 | value) & 0xfff;
            bits += 5;
            if (bits >= 8) {
                bits -= 8;
                out.write(accumulator >>> bits & 0xff);
            }
        }
        if (bits >= 5 || (accumulator & (1 << bits) - 1) != 0) {
            throw new InvalidComponentException(Message.of(Bitcoin.class, "bitcoin.trailing-bits",
                    "The address has trailing bits set."));
        }
        return out.toByteArray();
    }

    /** The human-readable part, expanded the way the checksum consumes it. */
    private static int[] expandHrp() {
        int[] expanded = new int[HRP.length() * 2 + 1];
        for (int i = 0; i < HRP.length(); i++) {
            expanded[i] = HRP.charAt(i) >>> 5;
            expanded[HRP.length() + 1 + i] = HRP.charAt(i) & 31;
        }
        return expanded;
    }

    private void validateBase58(String n) {
        byte[] address = base58Decode(n);
        if (address.length != 25) {
            throw new InvalidLengthException();
        }
        byte[] body = Arrays.copyOf(address, 21);
        byte[] expected = sha256(sha256(body));
        for (int i = 0; i < 4; i++) {
            if (expected[i] != address[21 + i]) {
                throw new InvalidChecksumException();
            }
        }
    }

    private void validateBech32(String n) {
        String payload = n.substring(3);
        int[] data = new int[payload.length()];
        for (int i = 0; i < payload.length(); i++) {
            data[i] = BECH32.indexOf(payload.charAt(i));
            if (data[i] < 0) {
                throw new InvalidFormatException();
            }
        }
        if (n.length() < 11 || n.length() > 90) {
            throw new InvalidLengthException();
        }
        int[] hrp = expandHrp();
        int[] checked = new int[hrp.length + data.length];
        System.arraycopy(hrp, 0, checked, 0, hrp.length);
        System.arraycopy(data, 0, checked, hrp.length, data.length);
        if (bech32Checksum(checked) != 1) {
            throw new InvalidChecksumException();
        }
        int witnessVersion = data[0];
        byte[] program = fromFiveBitGroups(Arrays.copyOfRange(data, 1, data.length - 6));
        if (witnessVersion > 16) {
            throw new InvalidComponentException(Message.of(Bitcoin.class, "bitcoin.witness-version",
                    "Not a witness version."));
        }
        if (program.length < 2 || program.length > 40) {
            throw new InvalidLengthException();
        }
        if (witnessVersion == 0 && program.length != 20 && program.length != 32) {
            throw new InvalidLengthException();
        }
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (n.startsWith("1") || n.startsWith("3")) {
            validateBase58(n);
        } else if (n.startsWith("bc1")) {
            validateBech32(n);
        } else {
            throw new InvalidComponentException(Message.of(Bitcoin.class, "bitcoin.form",
                    "Not a Bitcoin address."));
        }
        return n;
    }
}
