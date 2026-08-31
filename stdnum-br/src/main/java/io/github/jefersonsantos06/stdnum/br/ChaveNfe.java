package io.github.jefersonsantos06.stdnum.br;

import io.github.jefersonsantos06.stdnum.algo.Weighted;
import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidComponentException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

/**
 * Chave de Acesso da NF-e/NFC-e, the 44-digit access key of Brazilian
 * electronic fiscal documents.
 *
 * <p>Structure: IBGE state code (2), year and month of emission (4), emitter
 * document (14), fiscal document model (2), series (3), document number (9),
 * emission type (1), random code (8) and one check digit computed with
 * cyclic weights 2..9 modulo 11 (Manual de Orientação do Contribuinte).</p>
 *
 * <p>Validation checks structure, state code, month and check digit. The
 * emitter document field is <em>not</em> validated as a CNPJ because it may
 * also carry a zero-padded CPF; the model and emission type fields are not
 * restricted so that CT-e/MDF-e style keys are not wrongly rejected. Use
 * {@link #parse(String)} to inspect the parts.</p>
 */
public final class ChaveNfe implements StdNum {

    public static final ChaveNfe INSTANCE = new ChaveNfe();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("br.nfe", "Chave NF-e")
                    .country("BR")
                    .title("Chave de Acesso da NF-e")
                    .description("Access key of Brazilian electronic fiscal documents:"
                            + " 44 digits carrying state, emission date, emitter, model,"
                            + " series, number and a weighted mod 11 check digit.")
                    .tags(Tag.TAX, Tag.COMPANY)
                    .references("https://www.nfe.fazenda.gov.br/")
                    .build();

    private static final int[] WEIGHTS = Weighted.cyclic(43, 2, 3, 4, 5, 6, 7, 8, 9);

    private ChaveNfe() {
    }

    /** The decomposed fields of an access key. */
    public record Partes(
            Uf uf,
            String anoMes,
            String documentoEmitente,
            String modelo,
            String serie,
            String numero,
            String formaEmissao,
            String codigoNumerico,
            char digito) {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " ./-");
    }

    /** Calculates the check digit for the given 43-digit base. */
    public static int calcCheckDigit(String base) {
        String b = INSTANCE.compact(base);
        if (!Strings.isDigits(b)) {
            throw new InvalidFormatException();
        }
        if (b.length() != 43) {
            throw new InvalidLengthException();
        }
        return Weighted.mod11CheckDigit(b, WEIGHTS);
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (!Strings.isDigits(n)) {
            throw new InvalidFormatException();
        }
        if (n.length() != 44) {
            throw new InvalidLengthException();
        }
        if (Uf.fromIbge(Integer.parseInt(n.substring(0, 2))).isEmpty()) {
            throw new InvalidComponentException("Unknown IBGE state code.");
        }
        int month = Integer.parseInt(n.substring(4, 6));
        if (month < 1 || month > 12) {
            throw new InvalidComponentException("The emission month must be 01-12.");
        }
        if (n.charAt(43) - '0' != calcCheckDigit(n.substring(0, 43))) {
            throw new InvalidChecksumException();
        }
        return n;
    }

    @Override
    public String format(String number) {
        String n = validate(number);
        StringBuilder sb = new StringBuilder(54);
        for (int i = 0; i < 44; i += 4) {
            if (i > 0) {
                sb.append(' ');
            }
            sb.append(n, i, i + 4);
        }
        return sb.toString();
    }

    /** Validates the key and returns its decomposed fields. */
    public static Partes parse(String number) {
        String n = INSTANCE.validate(number);
        return new Partes(
                Uf.fromIbge(Integer.parseInt(n.substring(0, 2))).orElseThrow(),
                n.substring(2, 6),
                n.substring(6, 20),
                n.substring(20, 22),
                n.substring(22, 25),
                n.substring(25, 34),
                n.substring(34, 35),
                n.substring(35, 43),
                n.charAt(43));
    }
}
