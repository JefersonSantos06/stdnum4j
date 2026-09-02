package io.github.jefersonsantos06.stdnum.international;

import io.github.jefersonsantos06.stdnum.numdb.NumDb;
import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidComponentException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.Message;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * CFI, the ISO 10962 classification of a financial instrument: six letters
 * giving the category, the group within it, and four attributes whose
 * meaning depends on that group.
 *
 * <p>There is no check digit. A code is valid when every one of its six
 * letters means something for the instrument it classifies, so validation
 * is a walk down the classification itself. A position that does not apply
 * to a group is written X.</p>
 */
public final class Cfi implements StdNum {

    public static final Cfi INSTANCE = new Cfi();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("cfi", "CFI")
                    .title("Classification of Financial Instruments code")
                    .description("ISO 10962 classification of a financial instrument: a"
                            + " category, a group and four attributes, in six letters.")
                    .tags(Tag.FINANCIAL, Tag.PRODUCT)
                    .references("https://www.six-group.com/en/products-services/"
                            + "financial-information/data-standards.html")
                    .build();

    private Cfi() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    /** The classification, nested category, group and four attributes deep. */
    private static NumDb classification() {
        return NumDb.load(Cfi.class, "cfi.dat");
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " -").toUpperCase(Locale.ROOT);
    }

    /**
     * What the code says about the instrument: its category, its group, and
     * each attribute that applies, under the name of that attribute.
     *
     * @throws InvalidComponentException if any position means nothing for
     *                                   this group
     */
    public static Map<String, String> info(String number) {
        List<NumDb.Entry> entries = classification().info(INSTANCE.compact(number));
        if (entries.size() != 6) {
            throw new InvalidComponentException(Message.of(Cfi.class, "cfi.classification",
                    "Not a classification that exists."));
        }
        Map<String, String> info = new LinkedHashMap<>();
        info.putAll(entries.get(0).properties());
        info.putAll(entries.get(1).properties());
        for (NumDb.Entry entry : entries.subList(2, 6)) {
            String value = entry.properties().get("v");
            if (value == null) {
                // X stands for a position that does not apply to this group
                if (!entry.part().equals("X")) {
                    throw new InvalidComponentException(Message.of(Cfi.class, "cfi.attribute",
                            "The letter {0} means nothing here.", entry.part()));
                }
                continue;
            }
            info.put(entry.properties().get("a"), value);
        }
        return info;
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        for (int i = 0; i < n.length(); i++) {
            if (n.charAt(i) < 'A' || n.charAt(i) > 'Z') {
                throw new InvalidFormatException();
            }
        }
        if (n.length() != 6) {
            throw new InvalidLengthException();
        }
        info(n);
        return n;
    }
}
