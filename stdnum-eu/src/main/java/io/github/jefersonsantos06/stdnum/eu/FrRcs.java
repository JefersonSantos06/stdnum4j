package io.github.jefersonsantos06.stdnum.eu;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The French RCS number, the way a company registered with the trade and
 * companies register quotes itself: the letters RCS, the city of the
 * registry, an A for a trader or a B for a company, and the {@link FrSiren}.
 *
 * <p>Only the SIREN carries a checksum; the rest is the presentation the law
 * requires around it.</p>
 */
public final class FrRcs implements StdNum {

    public static final FrRcs INSTANCE = new FrRcs();

    private static final Pattern PATTERN = Pattern.compile(
            " *RCS +(?<city>.*?) +(?<letter>[AB]) *(?<siren>(?:[0-9] *){9})\\b *",
            Pattern.CASE_INSENSITIVE);

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("fr.rcs", "RCS")
                    .country("FR")
                    .title("Numero RCS")
                    .description("French trade and companies register number: the city of the"
                            + " registry, a category letter and the SIREN.")
                    .tags(Tag.COMPANY)
                    .references("https://fr.wikipedia.org/wiki/"
                            + "Registre_du_commerce_et_des_soci%C3%A9t%C3%A9s_(France)")
                    .build();

    private FrRcs() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        String[] parts = Strings.compact(number, "").split("\\s+");
        if (parts.length < 3) {
            throw new InvalidFormatException();
        }
        StringBuilder rest = new StringBuilder();
        for (int i = 2; i < parts.length; i++) {
            rest.append(parts[i]);
        }
        if (rest.isEmpty()) {
            throw new InvalidFormatException();
        }
        return parts[0] + ' ' + parts[1] + ' ' + rest.charAt(0) + ' '
                + FrSiren.INSTANCE.compact(rest.substring(1));
    }

    /** The SIREN the number is built around. */
    public static String toSiren(String number) {
        Matcher m = PATTERN.matcher(INSTANCE.compact(number));
        if (!m.matches()) {
            throw new InvalidFormatException();
        }
        return m.group("siren");
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        Matcher m = PATTERN.matcher(n);
        if (!m.matches()) {
            throw new InvalidFormatException();
        }
        FrSiren.INSTANCE.validate(m.group("siren"));
        return n;
    }

    @Override
    public String format(String number) {
        Matcher m = PATTERN.matcher(validate(number));
        if (!m.matches()) {
            throw new InvalidFormatException();
        }
        return "RCS " + m.group("city") + ' ' + m.group("letter") + ' '
                + FrSiren.INSTANCE.format(m.group("siren"));
    }
}
