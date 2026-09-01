package io.github.jefersonsantos06.stdnum.international;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * MAC address (media access control address), the data-link layer
 * identifier of a network device: six octets, normalised here to the
 * lower-case colon-separated form.
 *
 * <p>Both {@code :} and {@code -} separators are accepted, and octets
 * written with a single digit are zero-padded. The manufacturer lookup in
 * the IEEE OUI registry is not implemented.</p>
 */
public final class Mac implements StdNum {

    public static final Mac INSTANCE = new Mac();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("mac", "MAC address")
                    .title("Media access control address")
                    .description("Network device identifier: six octets normalised to the"
                            + " lower-case colon-separated form.")
                    .tags(Tag.TELECOM)
                    .references("https://en.wikipedia.org/wiki/MAC_address")
                    .build();

    private static final Pattern STRUCTURE = Pattern.compile("([0-9a-f]{2}:){5}[0-9a-f]{2}");

    private Mac() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        String n = Strings.compact(number, " ").toLowerCase(Locale.ROOT).replace('-', ':');
        if (n.indexOf(':') < 0) {
            return n;
        }
        String[] parts = n.split(":", -1);
        StringBuilder sb = new StringBuilder(17);
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) {
                sb.append(':');
            }
            sb.append(parts[i].length() == 1 ? "0" + parts[i] : parts[i]);
        }
        return sb.toString();
    }

    /** Whether the address is locally administered rather than globally unique. */
    public static boolean isLocallyAdministered(String number) {
        String n = INSTANCE.validate(number);
        return (Integer.parseInt(n.substring(0, 2), 16) & 0x02) != 0;
    }

    /** Whether the address is a multicast rather than a unicast address. */
    public static boolean isMulticast(String number) {
        String n = INSTANCE.validate(number);
        return (Integer.parseInt(n.substring(0, 2), 16) & 0x01) != 0;
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (!STRUCTURE.matcher(n).matches()) {
            throw new InvalidFormatException();
        }
        return n;
    }

    /** The OUI (organisationally unique identifier): the first three octets. */
    public static String oui(String number) {
        return INSTANCE.validate(number).substring(0, 8).replace(":", "").toUpperCase(Locale.ROOT);
    }
}
