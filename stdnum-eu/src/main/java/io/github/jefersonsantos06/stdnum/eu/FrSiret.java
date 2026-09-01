package io.github.jefersonsantos06.stdnum.eu;

import io.github.jefersonsantos06.stdnum.algo.Luhn;
import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

/**
 * SIRET (Système d'Identification du Répertoire des ETablissements), the
 * fourteen-digit French establishment identifier: a SIREN plus a five-digit
 * establishment number, validated with the Luhn checksum — except La Poste
 * establishments (prefix 356000000), whose digit sum must be a multiple of
 * five instead.
 */
public final class FrSiret implements StdNum {

    public static final FrSiret INSTANCE = new FrSiret();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("fr.siret", "SIRET")
                    .country("FR")
                    .title("Système d'Identification du Répertoire des ETablissements")
                    .description("French establishment identifier: 14 digits (SIREN plus"
                            + " establishment number) with a Luhn checksum and the La Poste"
                            + " digit-sum exception.")
                    .tags(Tag.COMPANY)
                    .build();

    private FrSiret() {
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
        if (n.length() != 14) {
            throw new InvalidLengthException();
        }
        // La Poste establishments (except the head office) do not use Luhn
        if (n.startsWith("356000000") && !n.equals("35600000000048")) {
            int sum = 0;
            for (int i = 0; i < n.length(); i++) {
                sum += n.charAt(i) - '0';
            }
            if (sum % 5 != 0) {
                throw new InvalidChecksumException();
            }
        } else {
            Luhn.validate(n);
        }
        FrSiren.INSTANCE.validate(n.substring(0, 9));
        return n;
    }

    @Override
    public String format(String number) {
        String n = validate(number);
        return n.substring(0, 3) + " " + n.substring(3, 6) + " "
                + n.substring(6, 9) + " " + n.substring(9);
    }

    /** The SIREN of this establishment (the first nine digits). */
    public static String toSiren(String number) {
        return INSTANCE.validate(number).substring(0, 9);
    }

    /** The French VAT number of the company owning this establishment. */
    public static String toTva(String number) {
        return FrSiren.toTva(toSiren(number));
    }
}
