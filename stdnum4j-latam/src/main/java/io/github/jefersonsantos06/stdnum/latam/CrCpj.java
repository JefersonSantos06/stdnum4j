package io.github.jefersonsantos06.stdnum.latam;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidComponentException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.Message;
import io.github.jefersonsantos06.stdnum.spi.Reasons;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Mask;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * CPJ (Cédula de Persona Jurídica), the Costa Rican tax number for legal
 * entities: ten characters where the first gives the class of entity, the
 * next three the type within that class and the last six the consecutive
 * number. There is no check digit, so validation is structural.
 *
 * <p>Decreto Ejecutivo 44648-MJP (2024) lets the six consecutive characters
 * mix letters with digits once the numeric ones run out, as in
 * {@code 3-101-A00001}; class and type stay digits. The NITE, the special
 * number Hacienda gives taxpayers without a cédula, is written in the same
 * shape under the types 120 (natural persons) and 130 (legal entities).</p>
 */
public final class CrCpj implements StdNum {

    public static final CrCpj INSTANCE = new CrCpj();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("cr.cpj", "CPJ")
                    .country("CR")
                    .title("Cédula de Persona Jurídica")
                    .description("Costa Rican legal entity tax number: a 4-digit class and type"
                            + " prefix and a 6-character consecutive, with no check digit.")
                    .tags(Tag.TAX, Tag.VAT, Tag.COMPANY)
                    .references("https://pgrweb.go.cr/scij/Busqueda/Normativa/Normas/nrm_texto_completo.aspx"
                                    + "?param1=NRTC&nValor1=1&nValor2=102851&nValor3=142498&strTipM=TC",
                            "https://www.oecd.org/content/dam/oecd/en/topics/policy-issue-focus/aeoi/costa-rica-tin.pdf")
                    .build();

    private static final Mask MASK = Mask.of("#-###-######");

    private static final Set<String> CLASS_TWO_TYPES =
            Set.of("100", "200", "300", "400");

    private static final Set<String> CLASS_THREE_TYPES = Set.of(
            "002", "003", "004", "005", "006", "007", "008", "009", "010",
            "011", "012", "013", "014", "101", "102", "103", "104", "105",
            "106", "107", "108", "109", "110", "120", "130");

    private CrCpj() {
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
        if (n.length() != 10) {
            throw new InvalidLengthException();
        }
        if (!Strings.isDigits(n.substring(0, 4)) || !isAlphanumeric(n.substring(4))) {
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
        if (n.startsWith("000000", 4)) {
            throw new InvalidComponentException(Reasons.zeroSequence());
        }
        return n;
    }

    private static boolean isAlphanumeric(String s) {
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (!((c >= '0' && c <= '9') || (c >= 'A' && c <= 'Z'))) {
                return false;
            }
        }
        return true;
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

    @Override
    public List<Mask> masks() {
        return List.of(MASK);
    }
}
