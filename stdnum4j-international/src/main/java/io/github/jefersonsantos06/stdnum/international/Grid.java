package io.github.jefersonsantos06.stdnum.international;

import io.github.jefersonsantos06.stdnum.algo.Iso7064;
import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Mask;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.util.List;
import java.util.Locale;

/**
 * GRid, the Global Release Identifier of an electronic music release: an
 * identifier scheme, the party that issued it, the release itself and an
 * ISO 7064 MOD 37-36 check character.
 */
public final class Grid implements StdNum {

    public static final Grid INSTANCE = new Grid();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("grid", "GRid")
                    .title("Global Release Identifier")
                    .description("Identifier of an electronic music release: 18 characters"
                            + " with an ISO 7064 MOD 37-36 check character.")
                    .tags(Tag.MEDIA, Tag.PRODUCT)
                    .references("https://en.wikipedia.org/wiki/Global_Release_Identifier")
                    .build();

    private static final Mask MASK = Mask.of("##-#####-##########-#");

    private Grid() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        String n = Strings.compact(number, " -").toUpperCase(Locale.ROOT);
        return n.startsWith("GRID:") ? n.substring(5) : n;
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (n.length() != 18) {
            throw new InvalidLengthException();
        }
        return Iso7064.MOD_37_36.validate(n);
    }

    @Override
    public String format(String number) {
        return MASK.fill(validate(number));
    }

    @Override
    public List<Mask> masks() {
        return List.of(MASK);
    }
}
