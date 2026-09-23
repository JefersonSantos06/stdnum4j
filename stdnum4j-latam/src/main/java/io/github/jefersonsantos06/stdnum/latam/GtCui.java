package io.github.jefersonsantos06.stdnum.latam;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
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

/**
 * CUI (Código Único de Identificación), the number on the Guatemalan identity
 * card, the DPI: thirteen digits — an eight-digit sequence, a check digit, and
 * the department and municipality where the person was registered.
 *
 * <p>The check digit is the sequence weighted 2 to 9, modulo 11, as in the
 * validator the Ministry of Public Finance publishes; a sequence whose
 * remainder is 10 is never issued. The municipality counts are those since
 * Sipacate (Escuintla) and Petatán (Huehuetenango) were created in 2015.
 * Naturalised citizens and foreign residents are registered under Guatemala
 * City, 01-01.</p>
 *
 * <p>Besides identifying a person, the CUI is what an individual buyer gives
 * on an electronic invoice (FEL) in place of a NIT.</p>
 */
public final class GtCui implements StdNum {

    public static final GtCui INSTANCE = new GtCui();

    /** How many municipalities each department has, Guatemala (01) first. */
    private static final int[] MUNICIPALITIES =
            {17, 8, 16, 16, 14, 14, 19, 8, 24, 21, 9, 30, 33, 21, 8, 17, 14, 5, 11, 11, 7, 17};

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("gt.cui", "CUI")
                    .country("GT")
                    .title("Código Único de Identificación")
                    .description("Guatemalan personal identity number on the DPI: an 8-digit sequence,"
                            + " a mod 11 check digit and the department and municipality.")
                    .tags(Tag.PERSON, Tag.TAX)
                    .references(
                            "https://www.renap.gob.gt/noticias/codigo-unico-de-identificacion-cui",
                            "https://github.com/minfingt/validators/blob/master/src/Minfin.Validators/CuiValidator.cs")
                    .build();

    private static final Mask MASK = Mask.of("9999 99999 9999");

    private GtCui() {
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
        if (!Strings.isDigits(n)) {
            throw new InvalidFormatException();
        }
        if (n.length() != 13) {
            throw new InvalidLengthException();
        }
        int department = Integer.parseInt(n.substring(9, 11));
        if (department < 1 || department > MUNICIPALITIES.length) {
            throw new InvalidComponentException(Message.of(GtCui.class, "cui.department",
                    "Not the code of a Guatemalan department."));
        }
        int municipality = Integer.parseInt(n.substring(11));
        if (municipality < 1 || municipality > MUNICIPALITIES[department - 1]) {
            throw new InvalidComponentException(Message.of(GtCui.class, "cui.municipality",
                    "The department has no municipality with this code."));
        }
        int sum = 0;
        for (int i = 0; i < 8; i++) {
            sum += (n.charAt(i) - '0') * (i + 2);
        }
        if (sum % 11 == 10) {
            throw new InvalidChecksumException(Reasons.noCheckDigit());
        }
        if (n.charAt(8) - '0' != sum % 11) {
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
