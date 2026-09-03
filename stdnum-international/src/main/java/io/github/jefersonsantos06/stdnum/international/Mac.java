package io.github.jefersonsantos06.stdnum.international;

import io.github.jefersonsantos06.stdnum.numdb.NumDb;
import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidComponentException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.Message;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.spi.ValidationException;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * MAC address (media access control address), the data-link layer
 * identifier of a network device: six octets, normalised here to the
 * lower-case colon-separated form.
 *
 * <p>Both {@code :} and {@code -} separators are accepted, and octets
 * written with a single digit are zero-padded.</p>
 *
 * <p>The leading octets name the manufacturer, and are looked up in the
 * registry the IEEE publishes. An address whose second-least-significant bit
 * is clear claims to have been assigned by a manufacturer, so it is checked
 * against that registry; one that is locally administered claims nothing and
 * is not. {@link #validate(String, Boolean)} overrides that either way.</p>
 */
public final class Mac implements StdNum {

    public static final Mac INSTANCE = new Mac();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("mac", "MAC address")
                    .title("Media access control address")
                    .description("Network device identifier: six octets normalised to the"
                            + " lower-case colon-separated form, with the manufacturer"
                            + " block looked up in the IEEE registry.")
                    .tags(Tag.TELECOM)
                    .references("https://en.wikipedia.org/wiki/MAC_address",
                            "https://standards-oui.ieee.org/")
                    .build();

    private static final Pattern STRUCTURE = Pattern.compile("([0-9a-f]{2}:){5}[0-9a-f]{2}");

    private Mac() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    /** The blocks the IEEE has assigned, and to whom. */
    private static NumDb registry() {
        return NumDb.load(Mac.class, "oui.dat");
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

    /**
     * The registry entry the address falls under.
     *
     * @throws InvalidComponentException if no assigned block covers it
     */
    private static List<NumDb.Entry> lookup(String compact) {
        String hex = compact.replace(":", "").toUpperCase(Locale.ROOT);
        List<NumDb.Entry> parts = registry().info(hex);
        // the last part is what the block does not cover, so the block itself
        // is the one before it, and it must name someone
        if (parts.size() < 2 || !parts.get(parts.size() - 2).properties().containsKey("o")) {
            throw new InvalidComponentException(Message.of(Mac.class, "mac.block",
                    "No manufacturer holds the block {0}.", hex.substring(0, 6)));
        }
        return parts;
    }

    /** Whether the first octet says the address was assigned by a manufacturer. */
    private static boolean assignedByManufacturer(String compact) {
        return (Integer.parseInt(compact.substring(0, 2), 16) & 0x02) == 0;
    }

    @Override
    public String validate(String number) {
        return validate(number, null);
    }

    /**
     * Validates the address, saying whether the manufacturer must be one the
     * IEEE has registered.
     *
     * @param validateManufacturer {@code null} to check only an address that
     *                             claims to be universally administered, which
     *                             is what an address is normally read as;
     *                             {@code true} to demand it of every address
     *                             and {@code false} of none
     */
    public String validate(String number, Boolean validateManufacturer) {
        String n = compact(number);
        if (!STRUCTURE.matcher(n).matches()) {
            throw new InvalidFormatException();
        }
        if (validateManufacturer == null ? assignedByManufacturer(n) : validateManufacturer) {
            lookup(n);
        }
        return n;
    }

    /** Whether the address is valid, saying whether to demand a manufacturer. */
    public boolean isValid(String number, Boolean validateManufacturer) {
        try {
            validate(number, validateManufacturer);
            return true;
        } catch (ValidationException e) {
            return false;
        }
    }

    /** Whether the address is one the manufacturer was assigned. */
    public static boolean isUniversallyAdministered(String number) {
        return assignedByManufacturer(INSTANCE.validate(number));
    }

    /** Whether the address is locally administered rather than globally unique. */
    public static boolean isLocallyAdministered(String number) {
        return !isUniversallyAdministered(number);
    }

    /** Whether the address is a multicast rather than a unicast address. */
    public static boolean isMulticast(String number) {
        String n = INSTANCE.validate(number);
        return (Integer.parseInt(n.substring(0, 2), 16) & 0x01) != 0;
    }

    /**
     * The OUI (organisationally unique identifier): the block the address
     * falls in, which is 24, 28 or 36 bits wide depending on how large a
     * block the manufacturer bought.
     *
     * @throws InvalidComponentException if no assigned block covers it
     */
    public static String oui(String number) {
        List<NumDb.Entry> parts = lookup(INSTANCE.validate(number, false));
        StringBuilder sb = new StringBuilder(9);
        for (int i = 0; i < parts.size() - 1; i++) {
            sb.append(parts.get(i).part());
        }
        return sb.toString();
    }

    /**
     * The IAB (individual address block): what the manufacturer is free to
     * assign, which is whatever the OUI leaves.
     *
     * @throws InvalidComponentException if no assigned block covers it
     */
    public static String iab(String number) {
        String hex = INSTANCE.validate(number, false).replace(":", "").toUpperCase(Locale.ROOT);
        return hex.substring(oui(number).length());
    }

    /** The manufacturer the block belongs to, as the IEEE records the name. */
    public static String manufacturer(String number) {
        List<NumDb.Entry> parts = lookup(INSTANCE.validate(number, false));
        return parts.get(parts.size() - 2).properties().get("o");
    }

    /** The upper-case dash-separated EUI-48 spelling of the address. */
    public static String toEui48(String number) {
        return INSTANCE.validate(number).replace(':', '-').toUpperCase(Locale.ROOT);
    }

    /** Whether the address is the broadcast address. */
    public static boolean isBroadcast(String number) {
        return "ff:ff:ff:ff:ff:ff".equals(INSTANCE.validate(number));
    }

    /** Whether the address is globally unique rather than locally assigned. */
    public static boolean isUnicast(String number) {
        return !isMulticast(number);
    }

}
