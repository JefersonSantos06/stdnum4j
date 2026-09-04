package io.github.jefersonsantos06.stdnum.eu;

import io.github.jefersonsantos06.stdnum.numdb.NumDb;
import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidComponentException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.Message;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The Czech bank account number: an optional prefix, the account itself and
 * the four-digit code of the institution, written {@code prefix-account/bank}.
 *
 * <p>The prefix and the account each carry their own weighted mod 11 check,
 * and the bank code has to be one the Czech National Bank has issued.</p>
 */
public final class CzBankaccount implements StdNum {

    public static final CzBankaccount INSTANCE = new CzBankaccount();

    private static final Pattern PATTERN = Pattern.compile(
            "(?:(?<prefix>[0-9]{0,6})-)?(?<root>[0-9]{2,10})/(?<bank>[0-9]{4})");
    private static final int[] WEIGHTS = {6, 3, 7, 9, 10, 5, 8, 4, 2, 1};

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("cz.bankaccount", "Cislo uctu")
                    .country("CZ")
                    .title("Ceske cislo bankovniho uctu")
                    .description("Czech bank account number: an optional prefix and an account"
                            + " number, each with a weighted mod 11 check, and a bank code.")
                    .tags(Tag.BANK)
                    .references("https://www.cnb.cz/cs/platebni-styk/ucty-kody-bank/")
                    .build();

    private CzBankaccount() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    /** The institutions, keyed by their payment system code. */
    private static NumDb banks() {
        return NumDb.load(CzBankaccount.class, "cz-banks.dat");
    }

    /**
     * The parts of a number: prefix, account and bank code.
     *
     * <p>Anything after the bank code is ignored, which is what the reference
     * does by anchoring its pattern at the start only.</p>
     */
    private static Matcher split(String number) {
        Matcher m = PATTERN.matcher(number);
        if (!m.lookingAt()) {
            throw new InvalidFormatException();
        }
        return m;
    }

    @Override
    public String compact(String number) {
        String n = Strings.compact(number, "");
        Matcher m = PATTERN.matcher(n);
        if (!m.lookingAt()) {
            return n;
        }
        String prefix = m.group("prefix") == null ? "" : m.group("prefix");
        return pad(prefix, 6) + '-' + pad(m.group("root"), 10) + '/' + m.group("bank");
    }

    private static String pad(String part, int width) {
        return "0".repeat(Math.max(0, width - part.length())) + part;
    }

    /** The weighted mod 11 sum of a part; a valid one yields 0. */
    public static int checksum(String part) {
        String n = pad(part, 10);
        int sum = 0;
        for (int i = 0; i < WEIGHTS.length && i < n.length(); i++) {
            sum += WEIGHTS[i] * (n.charAt(i) - '0');
        }
        return sum % 11;
    }

    /** What is known about the institution the account is held at. */
    public static Map<String, String> info(String number) {
        Matcher m = split(INSTANCE.compact(number));
        Map<String, String> info = new LinkedHashMap<>();
        for (NumDb.Entry entry : banks().info(m.group("bank"))) {
            info.putAll(entry.properties());
        }
        return info;
    }

    /** The BIC of the institution, or {@code null} if it has none. */
    public static String toBic(String number) {
        return info(number).get("bic");
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        Matcher m = split(n);
        if (checksum(m.group("prefix")) != 0 || checksum(m.group("root")) != 0) {
            throw new InvalidChecksumException();
        }
        if (!info(n).containsKey("bank")) {
            throw new InvalidComponentException(Message.of(CzBankaccount.class, "bank.institution",
                    "Not the code of an institution."));
        }
        return n;
    }
}
