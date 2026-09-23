package io.github.jefersonsantos06.stdnum.apac;

import io.github.jefersonsantos06.stdnum.algo.Weighted;
import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

/**
 * BRN, the Hong Kong business registration number: the first eight digits of
 * the certificate the Inland Revenue Department issues, the last of them a
 * check digit. Since December 2023 it is also the Unique Business Identifier
 * of every company, replacing the Companies Registry number.
 *
 * <p>The department does not publish the check digit. The rule here is the
 * one Webb-site found — the first seven digits weighted 8, 1, 2, 3, 6, 7, 8,
 * modulo 10 — and every numeric BRN in the Companies Registry's mapping list
 * of non-Hong Kong companies satisfies it.</p>
 *
 * <p>A company registered without a BRN is given a stand-in identifier, a
 * letter and its registry number ({@code F0000001}); that is not a BRN and is
 * refused.</p>
 */
public final class HkBrn implements StdNum {

    public static final HkBrn INSTANCE = new HkBrn();

    private static final int[] WEIGHTS = {8, 1, 2, 3, 6, 7, 8};

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("hk.brn", "BRN")
                    .country("HK")
                    .title("Business Registration Number")
                    .description("Hong Kong business registration number: 8 digits, the last"
                            + " a weighted mod 10 check digit.")
                    .tags(Tag.COMPANY, Tag.TAX)
                    .references(
                            "https://www.ird.gov.hk/eng/tax/bre.htm",
                            "https://www.cr.gov.hk/en/electronic/e-servicesportal/e-search/lists_for_the_mapping.htm",
                            "https://github.com/automihk/webb_site/blob/main/Webb-site%20ASP%20files/dbpub/HKBRcheck.asp")
                    .build();

    private HkBrn() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " -");
    }

    /** The check digit for the seven digits that come before it. */
    public static char calcCheckDigit(String base) {
        Strings.requireDigits(base, 7);
        return (char) ('0' + Weighted.weightedSum(base, WEIGHTS) % 10);
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (!Strings.isDigits(n)) {
            throw new InvalidFormatException();
        }
        if (n.length() != 8) {
            throw new InvalidLengthException();
        }
        if (n.charAt(7) != calcCheckDigit(n.substring(0, 7))) {
            throw new InvalidChecksumException();
        }
        return n;
    }
}
