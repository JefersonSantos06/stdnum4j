package io.github.jefersonsantos06.stdnum.international;

import io.github.jefersonsantos06.stdnum.algo.Iso7064;
import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Mask;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.util.List;
import java.util.Locale;

/**
 * ISNI (International Standard Name Identifier), identifying contributors
 * to media content: sixteen characters closed by an ISO 7064 MOD 11-2
 * check character, which may be {@code X}.
 */
public final class Isni implements StdNum {

    public static final Isni INSTANCE = new Isni();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("isni", "ISNI")
                    .title("International Standard Name Identifier")
                    .description("Contributor identifier: 16 characters with an ISO 7064"
                            + " MOD 11-2 check character.")
                    .tags(Tag.MEDIA)
                    .references("https://en.wikipedia.org/wiki/International_Standard_Name_Identifier")
                    .build();

    private static final Mask MASK = Mask.of("#### #### #### ####");

    private Isni() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " -").toUpperCase(Locale.ROOT);
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (n.isEmpty() || !Strings.isDigits(n.substring(0, n.length() - 1))) {
            throw new InvalidFormatException();
        }
        if (n.length() != 16) {
            throw new InvalidLengthException();
        }
        Iso7064.MOD_11_2.validate(n);
        return n;
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
