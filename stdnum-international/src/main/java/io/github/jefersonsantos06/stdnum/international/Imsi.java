package io.github.jefersonsantos06.stdnum.international;

import io.github.jefersonsantos06.stdnum.numdb.NumDb;
import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidComponentException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * IMSI, the number that identifies a subscriber to a mobile network: the
 * country, the network within it, and the subscriber's own number.
 *
 * <p>There is no check digit and no fixed split. How many digits the network
 * takes is the country's own business, so the number is read against the
 * registry: an IMSI is valid when its country is one that has been allocated.
 * A network code that country has not allocated is still accepted, being
 * simply one the registry has yet to hear about.</p>
 */
public final class Imsi implements StdNum {

    public static final Imsi INSTANCE = new Imsi();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("imsi", "IMSI")
                    .title("International Mobile Subscriber Identity")
                    .description("Subscriber identity on a mobile network: a country code, a"
                            + " network code and the subscriber's number, in 14 or 15 digits.")
                    .tags(Tag.TELECOM)
                    .references("https://en.wikipedia.org/wiki/"
                            + "International_mobile_subscriber_identity")
                    .build();

    private Imsi() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    /** The country and network codes that have been allocated. */
    private static NumDb registry() {
        return NumDb.load(Imsi.class, "imsi.dat");
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " -").toUpperCase(Locale.ROOT);
    }

    /**
     * The number in its three parts: country code, network code and
     * subscriber number. A number whose country is unknown comes back whole.
     */
    public static List<String> split(String number) {
        return registry().split(INSTANCE.compact(number));
    }

    /**
     * What is known about the network the subscriber belongs to, together
     * with the three parts under the keys {@code mcc}, {@code mnc} and
     * {@code msin}.
     */
    public static Map<String, String> info(String number) {
        String n = INSTANCE.compact(number);
        List<NumDb.Entry> entries = registry().info(n);
        if (entries.size() != 3) {
            throw new InvalidComponentException("Not a country code that has been allocated.");
        }
        Map<String, String> info = new LinkedHashMap<>();
        info.put("number", n);
        String[] names = {"mcc", "mnc", "msin"};
        for (int i = 0; i < 3; i++) {
            info.put(names[i], entries.get(i).part());
            info.putAll(entries.get(i).properties());
        }
        return info;
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (!Strings.isDigits(n)) {
            throw new InvalidFormatException();
        }
        if (n.length() != 14 && n.length() != 15) {
            throw new InvalidLengthException();
        }
        if (split(n).size() < 2) {
            throw new InvalidComponentException("Not a country code that has been allocated.");
        }
        return n;
    }
}
