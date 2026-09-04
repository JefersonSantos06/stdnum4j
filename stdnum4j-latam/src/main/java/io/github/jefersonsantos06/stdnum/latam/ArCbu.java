package io.github.jefersonsantos06.stdnum.latam;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Mask;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.util.List;

/**
 * CBU, the Argentinian bank account key: twenty-two digits in two blocks —
 * eight identifying the bank and branch, fourteen the account — each closed
 * by its own check digit.
 */
public final class ArCbu implements StdNum {

    public static final ArCbu INSTANCE = new ArCbu();

    private static final int[] WEIGHTS = {3, 1, 7, 9};

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("ar.cbu", "CBU")
                    .country("AR")
                    .title("Clave Bancaria Uniforme")
                    .description("Argentinian bank account key: 22 digits in two blocks, each"
                            + " with its own weighted mod 10 check digit.")
                    .tags(Tag.BANK, Tag.PAYMENT)
                    .references("https://es.wikipedia.org/wiki/Clave_Bancaria_Uniforme")
                    .build();

    private static final Mask MASK = Mask.of("######## ##############");

    private ArCbu() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " -");
    }

    /** The check digit closing a block, from the digits before it. */
    public static char calcCheckDigit(String block) {
        int check = 0;
        for (int i = 0; i < block.length(); i++) {
            check += (block.charAt(block.length() - 1 - i) - '0') * WEIGHTS[i % 4];
        }
        return (char) ('0' + Math.floorMod(10 - check, 10));
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (n.length() != 22) {
            throw new InvalidLengthException();
        }
        if (!Strings.isDigits(n)) {
            throw new InvalidFormatException();
        }
        if (n.charAt(7) != calcCheckDigit(n.substring(0, 7))
                || n.charAt(21) != calcCheckDigit(n.substring(8, 21))) {
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
