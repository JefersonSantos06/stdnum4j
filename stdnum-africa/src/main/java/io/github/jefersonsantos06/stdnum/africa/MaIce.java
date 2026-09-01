package io.github.jefersonsantos06.stdnum.africa;

import io.github.jefersonsantos06.stdnum.algo.Mod97;
import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

/**
 * ICE (Identifiant Commun de l'Entreprise), the Moroccan company
 * identifier used across all administrations: fifteen digits — nine for
 * the enterprise, four for the establishment and two control digits that
 * make the whole number a multiple of 97.
 *
 * <p>The three parts are not written apart: an ICE is quoted as fifteen
 * unbroken digits, which is what {@code format} returns.</p>
 */
public final class MaIce implements StdNum {

    public static final MaIce INSTANCE = new MaIce();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("ma.ice", "ICE")
                    .country("MA")
                    .title("Identifiant Commun de l'Entreprise")
                    .description("Moroccan company identifier: 15 digits divisible by 97.")
                    .tags(Tag.COMPANY, Tag.TAX)
                    .build();

    private MaIce() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " -.");
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (n.length() != 15) {
            throw new InvalidLengthException();
        }
        if (!Strings.isDigits(n)) {
            throw new InvalidFormatException();
        }
        if (Mod97.checksum(n) != 0) {
            throw new InvalidChecksumException();
        }
        return n;
    }
}
