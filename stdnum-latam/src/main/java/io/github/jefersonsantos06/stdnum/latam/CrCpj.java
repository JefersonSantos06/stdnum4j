package io.github.jefersonsantos06.stdnum.latam;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidComponentException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.Message;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Mask;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.util.Set;

/**
 * CPJ (Cédula de Persona Jurídica), the Costa Rican tax number for legal
 * entities: ten digits where the first gives the class of entity and the
 * next three the type within that class. There is no check digit, so
 * validation is structural.
 */
public final class CrCpj implements StdNum {

    public static final CrCpj INSTANCE = new CrCpj();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("cr.cpj", "CPJ")
                    .country("CR")
                    .title("Cédula de Persona Jurídica")
                    .description("Costa Rican legal entity tax number: 10 digits with a class"
                            + " and type prefix, and no check digit.")
                    .tags(Tag.TAX, Tag.VAT, Tag.COMPANY)
                    .build();

    private static final Mask MASK = Mask.of("#-###-######");

    private static final Set<String> CLASS_TWO_TYPES =
            Set.of("100", "200", "300", "400");

    private static final Set<String> CLASS_THREE_TYPES = Set.of(
            "002", "003", "004", "005", "006", "007", "008", "009", "010",
            "011", "012", "013", "014", "101", "102", "103", "104", "105",
            "106", "107", "108", "109", "110");

    private CrCpj() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " -");
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (n.length() != 10) {
            throw new InvalidLengthException();
        }
        if (!Strings.isDigits(n)) {
            throw new InvalidFormatException();
        }
        String type = n.substring(1, 4);
        switch (n.charAt(0)) {
            case '2' -> require(CLASS_TWO_TYPES.contains(type));
            case '3' -> require(CLASS_THREE_TYPES.contains(type));
            case '4' -> require(type.equals("000"));
            case '5' -> require(type.equals("001"));
            default -> throw new InvalidComponentException(Message.of(CrCpj.class, "cpj.class",
                    "Unknown class of juridical person."));
        }
        return n;
    }

    private static void require(boolean condition) {
        if (!condition) {
            throw new InvalidComponentException(Message.of(CrCpj.class, "cpj.type",
                    "Unknown type of juridical person."));
        }
    }

    @Override
    public String format(String number) {
        return MASK.fill(validate(number));
    }
}
