package io.github.jefersonsantos06.stdnum.br;

import io.github.jefersonsantos06.stdnum.algo.Weighted;
import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.Message;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.util.Locale;

/**
 * CNPJ (Cadastro Nacional da Pessoa Jurídica), the Brazilian company
 * identifier.
 *
 * <p>Fourteen positions: a 12-position base (8 for the company root, 4 for
 * the establishment) followed by two numeric check digits. From July 2026 the
 * base may also contain the letters {@code A-Z} (the "CNPJ alfanumérico");
 * the check digits are then computed over {@code ASCII - 48} values, which
 * this implementation supports transparently. Numbers made of a single
 * repeated character are rejected.</p>
 */
public final class Cnpj implements StdNum {

    public static final Cnpj INSTANCE = new Cnpj();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("br.cnpj", "CNPJ")
                    .country("BR")
                    .title("Cadastro Nacional da Pessoa Jurídica")
                    .description("Brazilian company identifier: 12 alphanumeric positions"
                            + " plus two weighted mod 11 check digits, supporting the"
                            + " alphanumeric format valid from July 2026.")
                    .tags(Tag.TAX, Tag.COMPANY, Tag.VAT)
                    .references("https://www.gov.br/receitafederal/pt-br/assuntos/orientacao-tributaria/"
                            + "cadastros/cnpj",
                            "https://en.wikipedia.org/wiki/CNPJ")
                    .build();

    private static final int[] WEIGHTS_1 = Weighted.cyclic(12, 2, 3, 4, 5, 6, 7, 8, 9);
    private static final int[] WEIGHTS_2 = Weighted.cyclic(13, 2, 3, 4, 5, 6, 7, 8, 9);

    private Cnpj() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " -./").toUpperCase(Locale.ROOT);
    }

    /**
     * Calculates the two check digits for the given 12-position base
     * (digits, or digits and letters in the alphanumeric format).
     */
    public static String calcCheckDigits(String base) {
        String b = INSTANCE.compact(base);
        if (!isBaseAlphabet(b)) {
            throw new InvalidFormatException();
        }
        if (b.length() != 12) {
            throw new InvalidLengthException();
        }
        int d1 = Weighted.mod11CheckDigit(b, WEIGHTS_1);
        int d2 = Weighted.mod11CheckDigit(b + d1, WEIGHTS_2);
        return "" + d1 + d2;
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (n.length() != 14) {
            if (!isBaseAlphabet(n)) {
                throw new InvalidFormatException();
            }
            throw new InvalidLengthException();
        }
        if (!isBaseAlphabet(n.substring(0, 12)) || !Strings.isDigits(n.substring(12))) {
            throw new InvalidFormatException();
        }
        if (Strings.allSame(n)) {
            throw new InvalidFormatException(Message.of(Cnpj.class, "cnpj.repeated",
                    "A CNPJ consisting of a single repeated character is not valid."));
        }
        if (!n.endsWith(calcCheckDigits(n.substring(0, 12)))) {
            throw new InvalidChecksumException();
        }
        return n;
    }

    @Override
    public String format(String number) {
        String n = validate(number);
        return n.substring(0, 2) + "." + n.substring(2, 5) + "." + n.substring(5, 8)
                + "/" + n.substring(8, 12) + "-" + n.substring(12);
    }

    private static boolean isBaseAlphabet(String s) {
        if (s.isEmpty()) {
            return false;
        }
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            boolean valid = (c >= '0' && c <= '9') || (c >= 'A' && c <= 'Z');
            if (!valid) {
                return false;
            }
        }
        return true;
    }
}
