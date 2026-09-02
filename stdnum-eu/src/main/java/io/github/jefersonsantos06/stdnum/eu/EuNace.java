package io.github.jefersonsantos06.stdnum.eu;

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
import java.util.Locale;
import java.util.Map;

/**
 * NACE, the classification the European Union puts an economic activity
 * under: a section letter, a two-digit division, then a digit of group and a
 * digit of class.
 *
 * <p>A code is valid when every level of it names a heading that exists, so
 * validation is a walk down the classification. Two revisions are in use;
 * Rev. 2.1, which added a section, is the default.</p>
 */
public final class EuNace implements StdNum {

    public static final EuNace INSTANCE = new EuNace();

    /** The revision a code is read against unless another is named. */
    public static final String DEFAULT_REVISION = "2.1";

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("eu.nace", "NACE")
                    .title("Statistical Classification of Economic Activities")
                    .description("The European classification of an economic activity: a"
                            + " section, a division, a group and a class.")
                    .tags(Tag.COMPANY, Tag.OTHER)
                    .references("https://ec.europa.eu/eurostat/web/nace")
                    .build();

    private EuNace() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    /** The classification of one revision. */
    private static NumDb classification(String revision) {
        String key = revision.replace(".", "");
        if (!key.equals("20") && !key.equals("21")) {
            throw new InvalidComponentException(Message.of(EuNace.class, "nace.revision",
                    "No such revision: {0}", revision));
        }
        return NumDb.load(EuNace.class, "eu-nace" + key + ".dat");
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, ".").toUpperCase(Locale.ROOT);
    }

    /** What the code says, level by level: its section and the headings under it. */
    public static Map<String, String> info(String number) {
        return info(number, DEFAULT_REVISION);
    }

    /**
     * What the code says, read against one revision of the classification.
     *
     * @throws InvalidComponentException if any level names no heading
     */
    public static Map<String, String> info(String number, String revision) {
        Map<String, String> info = new LinkedHashMap<>();
        for (NumDb.Entry entry : classification(revision).info(INSTANCE.compact(number))) {
            if (entry.properties().isEmpty()) {
                throw new InvalidComponentException(Message.of(EuNace.class, "nace.heading",
                        "Not a heading of the classification."));
            }
            info.putAll(entry.properties());
        }
        return info;
    }

    /** What the code is called, at the level it reaches. */
    public static String getLabel(String number) {
        return getLabel(number, DEFAULT_REVISION);
    }

    /** What the code is called in one revision, at the level it reaches. */
    public static String getLabel(String number, String revision) {
        return info(number, revision).get("label");
    }

    /**
     * The code with a full stop between the division and the group under it
     * ({@code "62.01"}), which is how the classification prints it. A section
     * letter and a bare division have nothing to separate and come back as
     * they are.
     *
     * <p>The code is validated first, so a code that no longer exists in the
     * default revision is refused rather than dressed up; the reference has no
     * such check, and also doubles the separator of an already-dotted code
     * ({@code "03.30"} to {@code "03..30"}).</p>
     */
    @Override
    public String format(String number) {
        String n = validate(number);
        return n.length() > 2 ? n.substring(0, 2) + "." + n.substring(2) : n;
    }

    @Override
    public String validate(String number) {
        return validate(number, DEFAULT_REVISION);
    }

    /** Validates the code against one revision of the classification. */
    public String validate(String number, String revision) {
        String n = compact(number);
        if (n.length() > 4) {
            throw new InvalidLengthException();
        }
        if (n.length() == 1) {
            if (n.charAt(0) < 'A' || n.charAt(0) > 'Z') {
                throw new InvalidFormatException();
            }
        } else if (!Strings.isDigits(n)) {
            throw new InvalidFormatException();
        }
        info(n, revision);
        return n;
    }
}
