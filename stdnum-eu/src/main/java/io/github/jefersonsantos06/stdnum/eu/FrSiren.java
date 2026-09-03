package io.github.jefersonsantos06.stdnum.eu;

import io.github.jefersonsantos06.stdnum.algo.Luhn;
import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Mask;
import io.github.jefersonsantos06.stdnum.text.Strings;

/**
 * SIREN (Système d'Identification du Répertoire des Entreprises), the
 * nine-digit French company identifier, validated with the Luhn checksum.
 */
public final class FrSiren implements StdNum {

    public static final FrSiren INSTANCE = new FrSiren();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("fr.siren", "SIREN")
                    .country("FR")
                    .title("Système d'Identification du Répertoire des Entreprises")
                    .description("French company identifier: 9 digits with a Luhn checksum.")
                    .tags(Tag.COMPANY)
                    .build();

    private static final Mask MASK = Mask.of("### ### ###");

    private FrSiren() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " .");
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
        Luhn.validate(n);
        return n;
    }

    @Override
    public String format(String number) {
        return MASK.fill(validate(number));
    }

    /** The French VAT number derived from this SIREN (two check digits prepended). */
    public static String toTva(String number) {
        String n = INSTANCE.validate(number);
        long check = Long.parseLong(n + "12") % 97;
        return (check < 10 ? "0" : "") + check + n;
    }
}
