package io.github.jefersonsantos06.stdnum.international;

import io.github.jefersonsantos06.stdnum.algo.Mod97;
import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.util.Locale;

/**
 * LEI (Legal Entity Identifier, ISO 17442), used to identify legal
 * entities in financial transactions: twenty alphanumeric characters — a
 * four-character issuing LOU, two reserved digits, thirteen identifying the
 * organisation and two ISO 7064 MOD 97-10 check digits.
 */
public final class Lei implements StdNum {

    public static final Lei INSTANCE = new Lei();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("lei", "LEI")
                    .title("Legal Entity Identifier")
                    .description("ISO 17442 legal entity identifier: 20 characters closed by"
                            + " MOD 97-10 check digits.")
                    .tags(Tag.COMPANY, Tag.FINANCIAL)
                    .references("https://en.wikipedia.org/wiki/Legal_Entity_Identifier")
                    .build();

    private Lei() {
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
        if (n.length() != 20) {
            throw new InvalidLengthException();
        }
        Mod97.validate(n);
        return n;
    }
}
