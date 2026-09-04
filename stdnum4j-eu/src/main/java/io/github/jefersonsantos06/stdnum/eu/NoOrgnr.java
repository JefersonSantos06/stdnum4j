package io.github.jefersonsantos06.stdnum.eu;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Mask;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.util.List;

/**
 * Organisasjonsnummer, the Norwegian organisation number: nine digits
 * weighted 3,2,7,6,5,4,3,2,1 modulo 11.
 */
public final class NoOrgnr implements StdNum {

    public static final NoOrgnr INSTANCE = new NoOrgnr();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("no.orgnr", "Orgnr")
                    .country("NO")
                    .title("Organisasjonsnummer")
                    .description("Norwegian organisation number: 9 digits with a weighted"
                            + " mod 11 check.")
                    .tags(Tag.COMPANY)
                    .build();

    private static final Mask MASK = Mask.of("### ### ###");

    private static final int[] WEIGHTS = {3, 2, 7, 6, 5, 4, 3, 2, 1};

    private NoOrgnr() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " ");
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (!Strings.isDigits(n)) {
            throw new InvalidFormatException();
        }
        if (n.length() != 9) {
            throw new InvalidLengthException();
        }
        int sum = 0;
        for (int i = 0; i < WEIGHTS.length; i++) {
            sum += WEIGHTS[i] * (n.charAt(i) - '0');
        }
        if (sum % 11 != 0) {
            throw new InvalidChecksumException();
        }
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
