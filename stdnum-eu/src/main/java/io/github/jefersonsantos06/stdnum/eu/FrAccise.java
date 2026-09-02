package io.github.jefersonsantos06.stdnum.eu;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidComponentException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.Message;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.util.Locale;

/**
 * The French excise number, which an operator in alcohol, tobacco or energy
 * products quotes when moving them under duty suspension: FR0, the
 * department, an office, a letter for the kind of operator and a serial.
 */
public final class FrAccise implements StdNum {

    public static final FrAccise INSTANCE = new FrAccise();

    /** Warehouse keeper, registered consignee, registered consignor, tax representative. */
    private static final String OPERATORS = "ENCB";

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("fr.accise", "Numero d'accise")
                    .country("FR")
                    .title("Numero d'accise francais")
                    .description("French excise number: FR0, a department, an office, a letter"
                            + " for the kind of operator and a serial.")
                    .tags(Tag.COMPANY, Tag.TAX, Tag.EXCISE)
                    .references("https://www.douane.gouv.fr/fiche/"
                            + "fiches-dactivite-evaluez-votre-activite-fiscale")
                    .build();

    private FrAccise() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " ").toUpperCase(Locale.ROOT);
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (!n.startsWith("FR0")) {
            throw new InvalidFormatException();
        }
        if (n.length() != 13) {
            throw new InvalidLengthException();
        }
        if (!Strings.isDigits(n.substring(3, 8)) || !Strings.isDigits(n.substring(9, 12))) {
            throw new InvalidFormatException();
        }
        if (OPERATORS.indexOf(n.charAt(8)) < 0) {
            throw new InvalidComponentException(Message.of(FrAccise.class, "accise.operator",
                    "Not the letter of a kind of operator."));
        }
        return n;
    }
}
