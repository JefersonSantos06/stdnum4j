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
 * CUPS, the code of a Spanish electricity or gas supply point: ES, the
 * distributor, the supply point, two check letters and, on a meter that
 * serves more than one dwelling, which one.
 */
public final class EsCups implements StdNum {

    public static final EsCups INSTANCE = new EsCups();

    private static final String CHECK_LETTERS = "TRWAGMYFPDXBNJZSQVHLCKE";
    /** The kinds of border a supply point can sit on. */
    private static final String POINT_TYPES = "FPRCXYZ";

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("es.cups", "CUPS")
                    .country("ES")
                    .title("Codigo Unificado de Punto de Suministro")
                    .description("Code of a Spanish electricity or gas supply point: 20 or 22"
                            + " characters with two check letters.")
                    .tags(Tag.OTHER)
                    .references("https://es.wikipedia.org/wiki/"
                            + "C%C3%B3digo_Unificado_de_Punto_de_Suministro")
                    .build();

    private EsCups() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " -").toUpperCase(Locale.ROOT);
    }

    /** The two check letters of a code, from the sixteen digits before them. */
    public static String calcCheckDigits(String number) {
        String n = INSTANCE.compact(number);
        long value = Long.parseLong(n.substring(2, 18)) % 529;
        return "" + CHECK_LETTERS.charAt((int) (value / 23)) + CHECK_LETTERS.charAt((int) (value % 23));
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (n.length() != 20 && n.length() != 22) {
            throw new InvalidLengthException();
        }
        if (!n.startsWith("ES")) {
            throw new InvalidComponentException(Message.of(EsCups.class, "cups.prefix",
                    "A supply point code starts with ES."));
        }
        if (!Strings.isDigits(n.substring(2, 18))) {
            throw new InvalidFormatException();
        }
        if (n.length() == 22
                && (!Strings.isDigits(n.substring(20, 21)) || POINT_TYPES.indexOf(n.charAt(21)) < 0)) {
            throw new InvalidFormatException();
        }
        if (!calcCheckDigits(n).equals(n.substring(18, 20))) {
            throw new InvalidChecksumException();
        }
        return n;
    }

    @Override
    public String format(String number) {
        String n = validate(number);
        StringBuilder sb = new StringBuilder(n.substring(0, 2));
        for (int i = 2; i + 4 <= 18; i += 4) {
            sb.append(' ').append(n, i, i + 4);
        }
        sb.append(' ').append(n, 18, 20);
        if (n.length() > 20) {
            sb.append(' ').append(n.substring(20));
        }
        return sb.toString();
    }
}
