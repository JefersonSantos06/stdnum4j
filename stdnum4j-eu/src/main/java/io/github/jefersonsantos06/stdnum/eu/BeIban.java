package io.github.jefersonsantos06.stdnum.eu;

import io.github.jefersonsantos06.stdnum.international.Iban;
import io.github.jefersonsantos06.stdnum.numdb.NumDb;
import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidComponentException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.Message;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.util.Map;

/**
 * The Belgian IBAN. Its account part carries a national check of its own —
 * the ten digits before it taken modulo 97, with a remainder of 0 written as
 * 97 — and opens with the three-digit code of the institution.
 */
public final class BeIban implements StdNum {

    public static final BeIban INSTANCE = new BeIban();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("be.iban", "IBAN")
                    .country("BE")
                    .title("Belgisch internationaal bankrekeningnummer")
                    .description("Belgian IBAN: the international number whose account part"
                            + " carries a mod 97 check and the code of the institution.")
                    .tags(Tag.BANK)
                    .references("https://www.nbb.be/en/payments-and-securities/bank-identification-codes")
                    .build();

    private BeIban() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    /** The institutions, keyed by the range of bank codes each holds. */
    private static NumDb banks() {
        return NumDb.load(BeIban.class, "be-banks.dat");
    }

    @Override
    public String compact(String number) {
        return Iban.INSTANCE.compact(number);
    }

    /**
     * The two national check digits, from the ten digits before them.
     *
     * @throws InvalidFormatException if the account part is not all digits,
     *                                which the general IBAN check allows but
     *                                a Belgian account never is
     */
    public static String calcCheckDigits(String base) {
        if (!Strings.isDigits(base)) {
            throw new InvalidFormatException(Message.of(BeIban.class, "iban.be.account-digits",
                    "A Belgian account number is all digits."));
        }
        long check = Long.parseLong(base) % 97;
        return String.format("%02d", check == 0 ? 97 : check);
    }

    /** What is known about the institution the account is held at. */
    public static Map<String, String> info(String number) {
        String n = INSTANCE.compact(number);
        if (n.length() < 7) {
            throw new InvalidComponentException(Message.of(BeIban.class, "iban.be.country",
                    "Not a Belgian IBAN."));
        }
        return banks().info(n.substring(4, 7)).get(0).properties();
    }

    /** The BIC of the institution, or {@code null} if it has none. */
    public static String toBic(String number) {
        return info(number).get("bic");
    }

    @Override
    public String validate(String number) {
        String n = Iban.INSTANCE.validate(number, false);
        if (!n.startsWith("BE")) {
            throw new InvalidComponentException(Message.of(BeIban.class, "iban.be.country",
                    "Not a Belgian IBAN."));
        }
        if (!n.substring(n.length() - 2).equals(calcCheckDigits(n.substring(4, n.length() - 2)))) {
            throw new InvalidChecksumException();
        }
        if (info(n).isEmpty()) {
            throw new InvalidComponentException(Message.of(BeIban.class, "bank.institution",
                    "Not the code of an institution."));
        }
        return n;
    }

    @Override
    public String format(String number) {
        // the generic grouping, but of a number this type accepts
        return Iban.INSTANCE.format(validate(number));
    }
}
