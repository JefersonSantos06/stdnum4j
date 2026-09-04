package io.github.jefersonsantos06.stdnum.eu;

import io.github.jefersonsantos06.stdnum.algo.Iso7064;
import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.Message;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Mask;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.util.List;

/**
 * Steuerliche Identifikationsnummer, the German personal tax number: eleven
 * digits closing with an ISO 7064 MOD 11,10 check digit.
 *
 * <p>The first ten digits also obey a distribution rule that makes typos
 * easier to spot: exactly one digit occurs two or three times and every
 * other digit that occurs at all occurs once.</p>
 */
public final class DeIdnr implements StdNum {

    public static final DeIdnr INSTANCE = new DeIdnr();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("de.idnr", "IdNr")
                    .country("DE")
                    .title("Steuerliche Identifikationsnummer")
                    .description("German personal tax number: 11 digits with an ISO 7064"
                            + " MOD 11,10 check digit and a digit distribution rule.")
                    .tags(Tag.PERSON, Tag.TAX)
                    .references("https://de.wikipedia.org/wiki/Steuerliche_Identifikationsnummer")
                    .build();

    private static final Mask MASK = Mask.of("## ### ### ###");

    private DeIdnr() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " -./,");
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (n.length() != 11) {
            throw new InvalidLengthException();
        }
        if (!Strings.isDigits(n) || n.charAt(0) == '0') {
            throw new InvalidFormatException();
        }
        if (!hasValidDigitDistribution(n)) {
            throw new InvalidFormatException(Message.of(DeIdnr.class, "idnr.digit-distribution",
                    "In the first ten digits exactly one digit must repeat, two or three times."));
        }
        return Iso7064.MOD_11_10.validate(n);
    }

    /**
     * Whether the first ten digits hold exactly one repeated digit, occurring
     * either twice or three times.
     */
    private static boolean hasValidDigitDistribution(String n) {
        int[] counts = new int[10];
        for (int i = 0; i < 10; i++) {
            counts[n.charAt(i) - '0']++;
        }
        int repeated = 0;
        for (int count : counts) {
            if (count > 1) {
                repeated++;
                if (count > 3) {
                    return false;
                }
            }
        }
        return repeated == 1;
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
