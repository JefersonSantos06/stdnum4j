package io.github.jefersonsantos06.stdnum.br;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidComponentException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

/**
 * Título de Eleitor, the Brazilian voter registration number.
 *
 * <p>Twelve digits: an 8-digit sequential number, a 2-digit TSE state code
 * (01-28, where 28 identifies voters abroad) and two check digits. Numbers
 * with dropped leading zeros are accepted and zero-padded.</p>
 *
 * <p>Check digit rule (TSE): the first digit is the remainder of the weighted
 * sum (weights 2..9 over the sequential part) modulo 11 — with remainder 10
 * mapping to 0 and, for São Paulo (01) and Minas Gerais (02) only, remainder
 * 0 mapping to 1. The second digit applies the same rule to
 * {@code uf1*7 + uf2*8 + dv1*9}. Note: published descriptions of this
 * algorithm diverge (some use 11 minus the remainder); this implementation
 * follows the rule used by the TSE itself and the mainstream validators.</p>
 */
public final class TituloEleitor implements StdNum {

    public static final TituloEleitor INSTANCE = new TituloEleitor();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("br.titulo-eleitor", "Título de Eleitor")
                    .country("BR")
                    .title("Título de Eleitor")
                    .description("Brazilian voter registration number: 8 sequential digits,"
                            + " a 2-digit TSE state code (01-28) and two check digits"
                            + " (remainder mod 11, with the São Paulo/Minas Gerais zero rule).")
                    .tags(Tag.PERSON)
                    .references("https://www.tse.jus.br/eleitor/titulo-de-eleitor")
                    .build();

    private TituloEleitor() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        String n = Strings.compact(number, " .-/");
        // leading zeros are commonly dropped when stored as integers
        if (!n.isEmpty() && n.length() < 12 && Strings.isDigits(n)) {
            return "0".repeat(12 - n.length()) + n;
        }
        return n;
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (!Strings.isDigits(n)) {
            throw new InvalidFormatException();
        }
        if (n.length() != 12) {
            throw new InvalidLengthException();
        }
        int uf = Integer.parseInt(n.substring(8, 10));
        if (uf < 1 || uf > 28) {
            throw new InvalidComponentException(
                    "The TSE state code must be between 01 and 28.");
        }
        boolean spMg = uf == 1 || uf == 2;

        int sum1 = 0;
        for (int i = 0; i < 8; i++) {
            sum1 += (n.charAt(i) - '0') * (i + 2);
        }
        int dv1 = digit(sum1, spMg);
        if (n.charAt(10) - '0' != dv1) {
            throw new InvalidChecksumException();
        }

        int sum2 = (n.charAt(8) - '0') * 7 + (n.charAt(9) - '0') * 8 + dv1 * 9;
        int dv2 = digit(sum2, spMg);
        if (n.charAt(11) - '0' != dv2) {
            throw new InvalidChecksumException();
        }
        return n;
    }

    @Override
    public String format(String number) {
        String n = validate(number);
        return n.substring(0, 4) + " " + n.substring(4, 8) + " " + n.substring(8);
    }

    private static int digit(int sum, boolean spMg) {
        int remainder = sum % 11;
        if (remainder == 10) {
            return 0;
        }
        if (remainder == 0) {
            return spMg ? 1 : 0;
        }
        return remainder;
    }
}
