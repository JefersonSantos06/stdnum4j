package io.github.jefersonsantos06.stdnum.international;

import io.github.jefersonsantos06.stdnum.algo.Mod97;
import io.github.jefersonsantos06.stdnum.numdb.NumDb;
import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidComponentException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * IBAN (International Bank Account Number, ISO 13616).
 *
 * <p>Two-letter country code, two MOD 97-10 check digits and a
 * country-specific BBAN. The country registry and each country's BBAN
 * structure live in {@code iban.dat}; unknown countries are rejected as an
 * invalid component, and the BBAN is matched against the registered
 * structure ({@code 8!n16!c} notation).</p>
 */
public final class Iban implements StdNum {

    public static final Iban INSTANCE = new Iban();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("iban", "IBAN")
                    .title("International Bank Account Number")
                    .description("ISO 13616 bank account identifier: country code, MOD 97-10"
                            + " check digits and a country-specific BBAN validated against"
                            + " the country registry.")
                    .tags(Tag.BANK)
                    .references("https://en.wikipedia.org/wiki/International_Bank_Account_Number")
                    .build();

    private static final Pattern STRUCTURE_TOKEN = Pattern.compile("(\\d+)!([nac])");
    private static final Map<String, Pattern> STRUCTURE_CACHE = new ConcurrentHashMap<>();

    private Iban() {
    }

    private static NumDb registry() {
        return NumDb.load(Iban.class, "iban.dat");
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " -.").toUpperCase(Locale.ROOT);
    }

    /**
     * The two check digits that make the number valid; the check digit
     * positions of the input are ignored.
     */
    public static String calcCheckDigits(String number) {
        String n = INSTANCE.compact(number);
        if (n.length() < 4) {
            throw new InvalidLengthException();
        }
        return Mod97.calcCheckDigits(n.substring(4) + n.substring(0, 2));
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (n.length() < 5) {
            throw new InvalidLengthException();
        }
        // rearranged checksum over the whole number
        Mod97.validate(n.substring(4) + n.substring(0, 4));
        // country lookup
        NumDb.Entry country = registry().info(n).get(0);
        String structure = country.properties().get("bban");
        if (structure == null) {
            throw new InvalidComponentException("Unknown IBAN country code.");
        }
        // country-specific BBAN structure
        if (!structurePattern(structure).matcher(n.substring(4)).matches()) {
            throw new InvalidFormatException(
                    "The BBAN does not match the structure registered for " + n.substring(0, 2) + ".");
        }
        return n;
    }

    @Override
    public String format(String number) {
        String n = validate(number);
        StringBuilder sb = new StringBuilder(n.length() + n.length() / 4);
        for (int i = 0; i < n.length(); i += 4) {
            if (i > 0) {
                sb.append(' ');
            }
            sb.append(n, i, Math.min(i + 4, n.length()));
        }
        return sb.toString();
    }

    private static Pattern structurePattern(String structure) {
        return STRUCTURE_CACHE.computeIfAbsent(structure, s -> {
            StringBuilder regex = new StringBuilder("^");
            Matcher matcher = STRUCTURE_TOKEN.matcher(s);
            while (matcher.find()) {
                String charset = switch (matcher.group(2)) {
                    case "n" -> "[0-9]";
                    case "a" -> "[A-Z]";
                    default -> "[0-9A-Z]";
                };
                regex.append(charset).append('{').append(matcher.group(1)).append('}');
            }
            return Pattern.compile(regex.append('$').toString());
        });
    }
}
