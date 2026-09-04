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

import java.util.List;

/**
 * CPF (Cédula de Persona Física), the Costa Rican identity number for
 * natural persons: ten digits in the form {@code 0P-TTTT-AAAA} — a
 * province, a volume ({@code tomo}) and an entry ({@code asiento}), each
 * zero-padded. Leading zeros are commonly dropped and restored here. There
 * is no check digit.
 */
public final class CrCpf implements StdNum {

    public static final CrCpf INSTANCE = new CrCpf();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("cr.cpf", "CPF")
                    .country("CR")
                    .title("Cédula de Persona Física")
                    .description("Costa Rican identity number: 10 digits of province, volume"
                            + " and entry, with no check digit.")
                    .tags(Tag.PERSON)
                    .build();

    private static final Mask MASK = Mask.of("##-####-####");

    private CrCpf() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        String n = Strings.clean(number, " ").strip();
        String[] parts = n.split("-", -1);
        if (parts.length == 3) {
            n = pad(parts[0], 2) + pad(parts[1], 4) + pad(parts[2], 4);
        } else {
            n = n.replace("-", "");
        }
        return n.length() == 9 ? "0" + n : n;
    }

    private static String pad(String part, int width) {
        return part.length() < width ? "0".repeat(width - part.length()) + part : part;
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
        if (n.charAt(0) != '0') {
            throw new InvalidComponentException(Message.of(CrCpf.class, "cpf.prefix",
                    "A CPF starts with 0."));
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
