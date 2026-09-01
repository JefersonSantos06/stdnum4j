package io.github.jefersonsantos06.stdnum.international;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidComponentException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * BIC (Business Identifier Code, ISO 9362), also known as the SWIFT code.
 *
 * <p>Eight or eleven characters: a four-letter business party prefix, a
 * two-letter ISO country code, a two-character alphanumeric suffix and an
 * optional three-character branch code. There is no check digit in a BIC:
 * beyond the structure, what is checked is that the country component is a
 * country.</p>
 */
public final class Bic implements StdNum {

    public static final Bic INSTANCE = new Bic();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("bic", "BIC")
                    .title("Business Identifier Code")
                    .description("ISO 9362 bank/business identifier (SWIFT code): structural"
                            + " validation of the 8 or 11 character form.")
                    .tags(Tag.BANK)
                    .references("https://en.wikipedia.org/wiki/ISO_9362")
                    .build();

    private static final Pattern STRUCTURE =
            Pattern.compile("[A-Z]{4}[A-Z]{2}[A-Z0-9]{2}(?:[A-Z0-9]{3})?");

    /**
     * The country codes a BIC may carry: ISO 3166-1 alpha-2, plus the XK
     * SWIFT assigned to Kosovo, which has no official code of its own.
     */
    private static final Set<String> COUNTRY_CODES = Set.of(
            "AD", "AE", "AF", "AG", "AI", "AL", "AM", "AO", "AQ", "AR",
            "AS", "AT", "AU", "AW", "AX", "AZ", "BA", "BB", "BD", "BE",
            "BF", "BG", "BH", "BI", "BJ", "BL", "BM", "BN", "BO", "BQ",
            "BR", "BS", "BT", "BV", "BW", "BY", "BZ", "CA", "CC", "CD",
            "CF", "CG", "CH", "CI", "CK", "CL", "CM", "CN", "CO", "CR",
            "CU", "CV", "CW", "CX", "CY", "CZ", "DE", "DJ", "DK", "DM",
            "DO", "DZ", "EC", "EE", "EG", "EH", "ER", "ES", "ET", "FI",
            "FJ", "FK", "FM", "FO", "FR", "GA", "GB", "GD", "GE", "GF",
            "GG", "GH", "GI", "GL", "GM", "GN", "GP", "GQ", "GR", "GS",
            "GT", "GU", "GW", "GY", "HK", "HM", "HN", "HR", "HT", "HU",
            "ID", "IE", "IL", "IM", "IN", "IO", "IQ", "IR", "IS", "IT",
            "JE", "JM", "JO", "JP", "KE", "KG", "KH", "KI", "KM", "KN",
            "KP", "KR", "KW", "KY", "KZ", "LA", "LB", "LC", "LI", "LK",
            "LR", "LS", "LT", "LU", "LV", "LY", "MA", "MC", "MD", "ME",
            "MF", "MG", "MH", "MK", "ML", "MM", "MN", "MO", "MP", "MQ",
            "MR", "MS", "MT", "MU", "MV", "MW", "MX", "MY", "MZ", "NA",
            "NC", "NE", "NF", "NG", "NI", "NL", "NO", "NP", "NR", "NU",
            "NZ", "OM", "PA", "PE", "PF", "PG", "PH", "PK", "PL", "PM",
            "PN", "PR", "PS", "PT", "PW", "PY", "QA", "RE", "RO", "RS",
            "RU", "RW", "SA", "SB", "SC", "SD", "SE", "SG", "SH", "SI",
            "SJ", "SK", "SL", "SM", "SN", "SO", "SR", "SS", "ST", "SV",
            "SX", "SY", "SZ", "TC", "TD", "TF", "TG", "TH", "TJ", "TK",
            "TL", "TM", "TN", "TO", "TR", "TT", "TV", "TW", "TZ", "UA",
            "UG", "UM", "US", "UY", "UZ", "VA", "VC", "VE", "VG", "VI",
            "VN", "VU", "WF", "WS", "XK", "YE", "YT", "ZA", "ZM", "ZW");

    private Bic() {
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
        if (n.length() != 8 && n.length() != 11) {
            throw new InvalidLengthException();
        }
        if (!STRUCTURE.matcher(n).matches()) {
            throw new InvalidFormatException();
        }
        if (!COUNTRY_CODES.contains(n.substring(4, 6))) {
            throw new InvalidComponentException(
                    "No country is coded " + n.substring(4, 6) + ".");
        }
        return n;
    }

    /** The ISO country code embedded in the BIC, always a known country. */
    public static String countryCode(String number) {
        return INSTANCE.validate(number).substring(4, 6);
    }

    /** The branch code, present only in the 11-character form. */
    public static Optional<String> branchCode(String number) {
        String n = INSTANCE.validate(number);
        return n.length() == 11 ? Optional.of(n.substring(8)) : Optional.empty();
    }
}
