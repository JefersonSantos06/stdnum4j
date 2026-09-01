package io.github.jefersonsantos06.stdnum.eu;

import io.github.jefersonsantos06.stdnum.algo.Mod97;
import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.util.Locale;

/**
 * The SEPA Identifier of the Creditor (AT-02): a country code, two ISO 7064
 * MOD 97-10 check digits, a three-character creditor business code and the
 * national identifier of the creditor.
 *
 * <p>The business code is deliberately left out of the checksum, so a
 * creditor can change it without invalidating the identifier: the digits are
 * verified over the national identifier followed by the country code and the
 * check digits.</p>
 */
public final class EuAt02 implements StdNum {

    public static final EuAt02 INSTANCE = new EuAt02();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("eu.at_02", "SEPA Creditor Identifier")
                    .title("SEPA Identifier of the Creditor (AT-02)")
                    .description("SEPA creditor identifier: country code, two mod 97-10 check"
                            + " digits, a 3-character business code and a national identifier.")
                    .tags(Tag.PAYMENT, Tag.FINANCIAL)
                    .references("https://www.europeanpaymentscouncil.eu/document-library/"
                            + "guidance-documents/creditor-identifier-overview")
                    .build();

    private EuAt02() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " -/?:().,'\"+").toUpperCase(Locale.ROOT);
    }

    /** The national identifier and the country code, in checksum order. */
    private static String checksumOrder(String n) {
        String tail = n.length() > 7 ? n.substring(7) : "";
        String head = n.length() > 4 ? n.substring(0, 4) : n;
        return tail + head;
    }

    /** The two check digits that make {@code number} valid; those it carries are ignored. */
    public static String calcCheckDigits(String number) {
        String n = INSTANCE.compact(number);
        String tail = n.length() > 7 ? n.substring(7) : "";
        String countryCode = n.length() > 2 ? n.substring(0, 2) : n;
        return Mod97.calcCheckDigits(tail + countryCode);
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        Mod97.validate(checksumOrder(n));
        return n;
    }
}
