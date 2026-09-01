package io.github.jefersonsantos06.stdnum.eu;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Firmenbuchnummer, the Austrian company register number: a run of digits
 * followed by a lower-case check letter. The number is usually written with
 * an {@code FN} prefix, which carries no information and is stripped.
 */
public final class AtBusinessid implements StdNum {

    public static final AtBusinessid INSTANCE = new AtBusinessid();

    private static final Pattern PATTERN = Pattern.compile("[0-9]+[a-z]");

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("at.businessid", "Firmenbuchnummer")
                    .country("AT")
                    .title("Osterreichische Firmenbuchnummer")
                    .description("Austrian company register number: digits followed by a"
                            + " lower-case check letter, optionally prefixed with FN.")
                    .tags(Tag.COMPANY)
                    .references("https://en.wikipedia.org/wiki/Firmenbuch")
                    .build();

    private AtBusinessid() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        String n = Strings.compact(number, " -./");
        return n.toUpperCase(Locale.ROOT).startsWith("FN") ? n.substring(2) : n;
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (!PATTERN.matcher(n).matches()) {
            throw new InvalidFormatException();
        }
        return n;
    }
}
