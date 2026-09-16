package io.github.jefersonsantos06.stdnum.latam;

import io.github.jefersonsantos06.stdnum.spi.*;
import io.github.jefersonsantos06.stdnum.text.Mask;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.util.List;
import java.util.Locale;

/**
 * Cédula, the Nicaraguan identity number: fourteen characters — a three-digit
 * municipality of birth, a six-digit date in DDMMYY, a four-digit sequence and
 * a check letter.
 *
 * <p>The letter is the thirteen digits taken modulo 23, read off an alphabet
 * that leaves out I, O and Z because they are read as 1, 0 and 2. Two digits
 * name no century, so the date is checked against 2000 + YY, which accepts
 * 29 February of year 00 — the one day the two readings disagree on.</p>
 *
 * <p>A 2025 law renames the document <em>Cédula de Identificación Ciudadana</em>
 * and announces a number held from birth; the cards in circulation carry this
 * format, and it is the one validated here. The same number is a natural
 * person's {@link NiRuc}.</p>
 */
public final class NiCedula implements StdNum {

    public static final NiCedula INSTANCE = new NiCedula();

    /** The check alphabet: 23 letters, without I, O and Z. */
    private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXY";

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("ni.cedula", "Cédula")
                    .country("NI")
                    .title("Cédula de identidad")
                    .description("Nicaraguan identity number: 14 characters giving the"
                            + " municipality of birth, the date of birth and a sequence,"
                            + " closed by a mod 23 check letter.")
                    .tags(Tag.PERSON)
                    .references("https://www.roderickzapata.com/herramientas/verificar-cedula-nicaragua/")
                    .build();

    private static final Mask MASK = Mask.of("###-######-####A");

    private NiCedula() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " -").toUpperCase(Locale.ROOT);
    }

    /**
     * The check letter for the thirteen digits that precede it. The remainder
     * is accumulated digit by digit because thirteen digits overflow an int.
     */
    public static char calcCheckLetter(String base) {
        String b = Strings.requireDigits(INSTANCE.compact(base), 13);
        int remainder = 0;
        for (int i = 0; i < b.length(); i++) {
            remainder = (remainder * 10 + (b.charAt(i) - '0')) % 23;
        }
        return ALPHABET.charAt(remainder);
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (n.length() != 14) {
            throw new InvalidLengthException();
        }
        if (!Strings.isDigits(n.substring(0, 13)) || ALPHABET.indexOf(n.charAt(13)) < 0) {
            throw new InvalidFormatException();
        }
        Dates.birthDate(2000 + Integer.parseInt(n.substring(7, 9)),
                Integer.parseInt(n.substring(5, 7)),
                Integer.parseInt(n.substring(3, 5)));
        if (n.charAt(13) != calcCheckLetter(n.substring(0, 13))) {
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
