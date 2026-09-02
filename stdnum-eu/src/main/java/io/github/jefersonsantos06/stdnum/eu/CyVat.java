package io.github.jefersonsantos06.stdnum.eu;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidComponentException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.Message;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.util.Locale;

/**
 * Αριθμός Εγγραφής Φ.Π.Α., the Cypriot VAT number: eight digits and a
 * check letter. Digits in even positions are translated through a fixed
 * table before summing; numbers starting {@code 12} are not issued.
 */
public final class CyVat implements StdNum {

    public static final CyVat INSTANCE = new CyVat();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("cy.vat", "ΦΠΑ")
                    .country("CY")
                    .title("Αριθμός Εγγραφής Φ.Π.Α.")
                    .description("Cypriot VAT number: 8 digits and a mod 26 check letter.")
                    .tags(Tag.VAT)
                    .build();

    /** Translation of digits in even positions. */
    private static final int[] TRANSLATION = {1, 0, 5, 7, 9, 13, 15, 17, 19, 21};

    private CyVat() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        String n = Strings.compact(number, " -").toUpperCase(Locale.ROOT);
        return n.startsWith("CY") ? n.substring(2) : n;
    }

    /** The check letter for the eight-digit base. */
    public static char calcCheckDigit(String base) {
        int sum = 0;
        for (int i = 0; i < base.length(); i++) {
            int digit = base.charAt(i) - '0';
            sum += i % 2 == 0 ? TRANSLATION[digit] : digit;
        }
        return (char) ('A' + sum % 26);
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (n.isEmpty() || !Strings.isDigits(n.substring(0, n.length() - 1))) {
            throw new InvalidFormatException();
        }
        if (n.length() != 9) {
            throw new InvalidLengthException();
        }
        if (n.startsWith("12")) {
            throw new InvalidComponentException(Message.of(CyVat.class, "vat.cy.prefix",
                    "Cypriot VAT numbers do not start with 12."));
        }
        if (n.charAt(8) != calcCheckDigit(n.substring(0, 8))) {
            throw new InvalidChecksumException();
        }
        return n;
    }
}
