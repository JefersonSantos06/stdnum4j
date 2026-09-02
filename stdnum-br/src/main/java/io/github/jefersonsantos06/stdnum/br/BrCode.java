package io.github.jefersonsantos06.stdnum.br;

import io.github.jefersonsantos06.stdnum.algo.Crc16;
import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidComponentException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.Message;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Pix BR Code, the EMV®QRCPS payload behind a Brazilian Pix charge.
 *
 * <p>The payload is a sequence of TLV (tag-length-value) fields; the last
 * one is always tag {@code 63}, holding a CRC-16/CCITT-FALSE over
 * everything up to and including {@code "6304"}. Unlike the other types
 * here the payload is not a "number", but it validates and normalises the
 * same way, so it fits the same contract.</p>
 */
public final class BrCode implements StdNum {

    public static final BrCode INSTANCE = new BrCode();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("br.pix", "Pix BR Code")
                    .country("BR")
                    .title("Pix BR Code (EMV QRCPS)")
                    .description("Payload of a Brazilian Pix charge: EMV TLV fields closed by"
                            + " a CRC-16/CCITT-FALSE in tag 63.")
                    .tags(Tag.BANK, Tag.PAYMENT)
                    .references("https://www.bcb.gov.br/estabilidadefinanceira/pix")
                    .build();

    private BrCode() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        // The CRC covers the exact characters, and a Pix key may legitimately
        // be lower case (an e-mail), so the payload keeps its case: only
        // surrounding whitespace is removed.
        return Strings.clean(number, "").strip();
    }

    /** The CRC that closes the payload, given everything up to {@code "6304"}. */
    public static String calcCrc(String payloadUpToCrcTag) {
        return Crc16.hex(payloadUpToCrcTag);
    }

    /**
     * The top-level TLV fields of the payload, in order, keyed by tag.
     * Nested templates are returned as their raw value.
     */
    public static Map<String, String> fields(String payload) {
        String n = INSTANCE.validate(payload);
        return parse(n);
    }

    private static Map<String, String> parse(String n) {
        Map<String, String> fields = new LinkedHashMap<>();
        int i = 0;
        while (i + 4 <= n.length()) {
            String tag = n.substring(i, i + 2);
            String lengthText = n.substring(i + 2, i + 4);
            if (!Strings.isDigits(tag) || !Strings.isDigits(lengthText)) {
                throw new InvalidFormatException(Message.of(BrCode.class, "brcode.tlv.malformed",
                        "Malformed TLV field in the payload."));
            }
            int length = Integer.parseInt(lengthText);
            if (i + 4 + length > n.length()) {
                throw new InvalidFormatException(Message.of(BrCode.class, "brcode.tlv.overrun",
                        "A TLV field runs past the end of the payload."));
            }
            fields.put(tag, n.substring(i + 4, i + 4 + length));
            i += 4 + length;
        }
        if (i != n.length()) {
            throw new InvalidFormatException(Message.of(BrCode.class, "brcode.tlv.trailing",
                    "Trailing bytes after the last TLV field."));
        }
        return fields;
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (n.length() < 8) {
            throw new InvalidFormatException(Message.of(BrCode.class, "brcode.crc.short",
                    "The payload is too short to hold a CRC."));
        }
        // the CRC tag sits at a fixed offset from the end; searching for
        // "6304" would misfire when the CRC value itself is 6304
        if (!n.startsWith("6304", n.length() - 8)) {
            throw new InvalidComponentException(Message.of(BrCode.class, "brcode.crc.tag",
                    "A Pix payload ends with tag 63, length 04 and the CRC."));
        }
        String expected = calcCrc(n.substring(0, n.length() - 4));
        if (!n.endsWith(expected)) {
            throw new InvalidChecksumException(Message.of(BrCode.class, "brcode.crc.mismatch",
                    "The payload CRC does not match."));
        }
        // structural check of the TLV fields, CRC field included
        parse(n);
        return n;
    }
}
