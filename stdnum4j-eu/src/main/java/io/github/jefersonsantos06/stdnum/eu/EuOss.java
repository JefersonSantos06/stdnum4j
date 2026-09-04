package io.github.jefersonsantos06.stdnum.eu;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidComponentException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.Message;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.util.Locale;
import java.util.Set;

/**
 * The VAT number of a trader registered under one of the EU One Stop Shop
 * schemes: {@code EU} and nine digits for the non-union scheme, or {@code IM}
 * and ten digits for the import scheme.
 *
 * <p>The three digits after the prefix are the ISO 3166-1 numeric code of the
 * member state of identification, which is the only part that can be checked
 * — the rest is a sequence number with no check digit.</p>
 */
public final class EuOss implements StdNum {

    public static final EuOss INSTANCE = new EuOss();

    /**
     * The member states of identification. This is the ISO 3166-1 numeric code
     * of each EU country, plus 900 for Northern Ireland, which stays inside
     * the schemes for goods.
     */
    private static final Set<String> MEMBER_STATES = Set.of(
            "040", "056", "100", "191", "196", "203", "208", "233", "246", "250",
            "276", "300", "348", "372", "380", "428", "440", "442", "470", "528",
            "616", "620", "642", "703", "705", "724", "752", "900");

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("eu.oss", "OSS")
                    .title("EU One Stop Shop VAT number")
                    .description("VAT number of a trader in an EU One Stop Shop scheme: EU and"
                            + " 9 digits, or IM and 10, opening with a member state code.")
                    .tags(Tag.VAT, Tag.TAX)
                    .references("https://vat-one-stop-shop.ec.europa.eu/one-stop-shop/register-oss_en")
                    .build();

    private EuOss() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " -").toUpperCase(Locale.ROOT);
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (n.startsWith("EU")) {
            if (n.length() != 11) {
                throw new InvalidLengthException();
            }
        } else if (n.startsWith("IM")) {
            if (n.length() != 12) {
                throw new InvalidLengthException();
            }
        } else {
            throw new InvalidComponentException(Message.of(EuOss.class, "oss.prefix",
                    "One Stop Shop numbers start with EU or IM."));
        }
        if (!Strings.isDigits(n.substring(2))) {
            throw new InvalidFormatException();
        }
        if (!MEMBER_STATES.contains(n.substring(2, 5))) {
            throw new InvalidComponentException(Message.of(EuOss.class, "oss.member-state",
                    "Not the code of a member state of identification."));
        }
        return n;
    }
}
