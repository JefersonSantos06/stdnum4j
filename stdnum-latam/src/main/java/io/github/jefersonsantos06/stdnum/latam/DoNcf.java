package io.github.jefersonsantos06.stdnum.latam;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidComponentException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.util.Locale;
import java.util.Set;

/**
 * NCF, the number a Dominican taxpayer puts on a receipt so the tax office
 * can trace it. It carries no check digit: what identifies it is the kind of
 * document it stands for.
 *
 * <p>Three generations are in circulation. The nineteen-character form came
 * first, the eleven-character B form replaced it, and the thirteen-character
 * E form is the electronic receipt — each with its own set of document
 * types.</p>
 */
public final class DoNcf implements StdNum {

    public static final DoNcf INSTANCE = new DoNcf();

    /** The kinds of document a paper receipt can stand for. */
    private static final Set<String> PAPER_TYPES = Set.of(
            "01",  // invoice for fiscal declaration
            "02",  // invoice for a final consumer
            "03",  // debit note
            "04",  // credit note
            "11",  // informal supplier invoice
            "12",  // single income invoice
            "13",  // minor expenses invoice
            "14",  // invoice for a special customer
            "15",  // invoice for the government
            "16",  // invoice for export
            "17"); // invoice for a payment abroad

    /** The kinds of document an electronic receipt can stand for. */
    private static final Set<String> ELECTRONIC_TYPES = Set.of(
            "31", "32", "33", "34", "41", "43", "44", "45", "46", "47");

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("do.ncf", "NCF")
                    .country("DO")
                    .title("Numero de Comprobante Fiscal")
                    .description("Dominican fiscal receipt number: 11, 13 or 19 characters"
                            + " naming the kind of document it stands for.")
                    .tags(Tag.TAX)
                    .references("https://dgii.gov.do/")
                    .build();

    private DoNcf() {
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
        String type;
        Set<String> allowed;
        switch (n.length()) {
            case 13 -> {
                requireLeadIn(n, "E");
                type = n.substring(1, 3);
                allowed = ELECTRONIC_TYPES;
            }
            case 11 -> {
                requireLeadIn(n, "B");
                type = n.substring(1, 3);
                allowed = PAPER_TYPES;
            }
            case 19 -> {
                requireLeadIn(n, "AP");
                type = n.substring(9, 11);
                allowed = PAPER_TYPES;
            }
            default -> throw new InvalidLengthException();
        }
        if (!allowed.contains(type)) {
            throw new InvalidComponentException("Not the code of a kind of document.");
        }
        return n;
    }

    /** The number must open with one of {@code series} and be digits thereafter. */
    private static void requireLeadIn(String n, String series) {
        if (series.indexOf(n.charAt(0)) < 0 || !Strings.isDigits(n.substring(1))) {
            throw new InvalidFormatException();
        }
    }
}
