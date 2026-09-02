package io.github.jefersonsantos06.stdnum.br;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.Message;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

/**
 * Linha digitável, the 47-digit typeable line printed above the barcode of
 * a Brazilian bank payment slip.
 *
 * <p>It carries the same information as the {@link CodigoBarras}, shuffled
 * into three fields of 9, 10 and 10 digits — each closed by its own mod 10
 * check digit — followed by the general check digit and the 14 digits of
 * due-date factor and amount.</p>
 */
public final class LinhaDigitavel implements StdNum {

    public static final LinhaDigitavel INSTANCE = new LinhaDigitavel();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("br.boleto-linha", "Linha digitável")
                    .country("BR")
                    .title("Linha digitável de cobrança (FEBRABAN)")
                    .description("Typeable line of a Brazilian payment slip: 47 digits with"
                            + " three mod 10 field check digits and the general check digit.")
                    .tags(Tag.BANK, Tag.PAYMENT)
                    .references("https://portal.febraban.org.br/")
                    .build();

    private LinhaDigitavel() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " .-");
    }

    /** The mod 10 check digit of one field, weighted 2 and 1 from the right. */
    public static int calcFieldCheckDigit(String field) {
        int sum = 0;
        for (int i = field.length() - 1, weight = 2; i >= 0; i--, weight = 3 - weight) {
            int product = (field.charAt(i) - '0') * weight;
            sum += product > 9 ? product - 9 : product;
        }
        return (10 - sum % 10) % 10;
    }

    /** The 44-digit barcode this typeable line encodes. */
    public static String toCodigoBarras(String number) {
        String n = INSTANCE.validate(number);
        return n.substring(0, 4)            // bank and currency
                + n.substring(32, 33)       // general check digit
                + n.substring(33)           // due-date factor and amount
                + n.substring(4, 9)         // remainder of field 1
                + n.substring(10, 20)       // field 2
                + n.substring(21, 31);      // field 3
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (!Strings.isDigits(n)) {
            throw new InvalidFormatException();
        }
        if (n.length() != 47) {
            throw new InvalidLengthException();
        }
        checkField(n.substring(0, 9), n.charAt(9));
        checkField(n.substring(10, 20), n.charAt(20));
        checkField(n.substring(21, 31), n.charAt(31));
        // the general check digit is verified through the barcode it encodes
        CodigoBarras.INSTANCE.validate(toBarcode(n));
        return n;
    }

    private static void checkField(String field, char expected) {
        if (expected - '0' != calcFieldCheckDigit(field)) {
            throw new InvalidChecksumException(Message.of(LinhaDigitavel.class, "linha-digitavel.field-check",
                    "The check digit of a field of the typeable line is invalid."));
        }
    }

    /** Rearranges an already-compacted line into its barcode. */
    private static String toBarcode(String n) {
        return n.substring(0, 4) + n.substring(32, 33) + n.substring(33)
                + n.substring(4, 9) + n.substring(10, 20) + n.substring(21, 31);
    }

    @Override
    public String format(String number) {
        String n = validate(number);
        return n.substring(0, 5) + "." + n.substring(5, 10) + " "
                + n.substring(10, 15) + "." + n.substring(15, 21) + " "
                + n.substring(21, 26) + "." + n.substring(26, 32) + " "
                + n.substring(32, 33) + " " + n.substring(33);
    }
}
