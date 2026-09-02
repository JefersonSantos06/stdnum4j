package io.github.jefersonsantos06.stdnum.apac;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidComponentException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.Message;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.time.LocalDate;
import java.util.Locale;
import java.util.Set;

/**
 * UEN (Unique Entity Number), issued by ACRA to entities operating in
 * Singapore, in three shapes:
 *
 * <ul>
 *   <li><b>Business (ROB)</b> — 8 digits and a check letter;</li>
 *   <li><b>Local company (ROC)</b> — 9 digits opening with the year of
 *       issuance, and a check letter;</li>
 *   <li><b>Other</b> — {@code R}, {@code S} or {@code T}, two year digits,
 *       a two-letter entity type, four digits and a check letter.</li>
 * </ul>
 *
 * <p>Each shape has its own weights and check alphabet.</p>
 */
public final class SgUen implements StdNum {

    public static final SgUen INSTANCE = new SgUen();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("sg.uen", "UEN")
                    .country("SG")
                    .title("Unique Entity Number")
                    .description("Singapore entity identifier in its business, local company"
                            + " and other forms, each with its own check letter.")
                    .tags(Tag.COMPANY, Tag.TAX)
                    .build();

    private static final int[] BUSINESS_WEIGHTS = {10, 4, 9, 3, 8, 2, 7, 1};
    private static final String BUSINESS_ALPHABET = "XMKECAWLJDB";

    private static final int[] COMPANY_WEIGHTS = {10, 8, 6, 4, 9, 7, 5, 3, 1};
    private static final String COMPANY_ALPHABET = "ZKCMDNERGWH";

    private static final int[] OTHER_WEIGHTS = {4, 3, 5, 3, 10, 2, 2, 5, 7};
    private static final String OTHER_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWX0123456789";

    /** Entity type codes accepted in the "other" form. */
    private static final Set<String> OTHER_ENTITY_TYPES = Set.of(
            "CC", "CD", "CH", "CL", "CM", "CP", "CS", "CX", "DP", "FB", "FC", "FM",
            "FN", "GA", "GB", "GS", "HS", "LL", "LP", "MB", "MC", "MD", "MH", "MM",
            "MQ", "NB", "NR", "PA", "PB", "PF", "RF", "RP", "SM", "SS", "TC", "TU",
            "VH", "XL");

    private SgUen() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " ").toUpperCase(Locale.ROOT);
    }

    /** The check letter of a business (ROB) number, from its first eight digits. */
    public static char calcBusinessCheckDigit(String base) {
        return weighted(base, BUSINESS_WEIGHTS, BUSINESS_ALPHABET, 0);
    }

    /** The check letter of a local company (ROC) number, from its first nine digits. */
    public static char calcCompanyCheckDigit(String base) {
        return weighted(base, COMPANY_WEIGHTS, COMPANY_ALPHABET, 0);
    }

    /** The check letter of an "other" number, from its first nine characters. */
    public static char calcOtherCheckDigit(String base) {
        int sum = 0;
        for (int i = 0; i < OTHER_WEIGHTS.length && i < base.length(); i++) {
            int value = OTHER_ALPHABET.indexOf(base.charAt(i));
            if (value < 0) {
                throw new InvalidFormatException();
            }
            sum += value * OTHER_WEIGHTS[i];
        }
        return OTHER_ALPHABET.charAt(Math.floorMod(sum - 5, 11));
    }

    private static char weighted(String base, int[] weights, String alphabet, int offset) {
        int sum = offset;
        for (int i = 0; i < weights.length && i < base.length(); i++) {
            sum += (base.charAt(i) - '0') * weights[i];
        }
        return alphabet.charAt(sum % 11);
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (n.length() != 9 && n.length() != 10) {
            throw new InvalidLengthException();
        }
        if (n.length() == 9) {
            validateBusiness(n);
        } else if (n.charAt(0) >= '0' && n.charAt(0) <= '9') {
            validateLocalCompany(n);
        } else {
            validateOther(n);
        }
        return n;
    }

    private static void validateBusiness(String n) {
        if (!Strings.isDigits(n.substring(0, 8)) || !Character.isLetter(n.charAt(8))) {
            throw new InvalidFormatException();
        }
        if (n.charAt(8) != calcBusinessCheckDigit(n)) {
            throw new InvalidChecksumException();
        }
    }

    private static void validateLocalCompany(String n) {
        if (!Strings.isDigits(n.substring(0, 9)) || !Character.isLetter(n.charAt(9))) {
            throw new InvalidFormatException();
        }
        if (Integer.parseInt(n.substring(0, 4)) > LocalDate.now().getYear()) {
            throw new InvalidComponentException(Message.of(SgUen.class, "uen.year.future",
                    "The year of issuance is in the future."));
        }
        if (n.charAt(9) != calcCompanyCheckDigit(n)) {
            throw new InvalidChecksumException();
        }
    }

    private static void validateOther(String n) {
        if ("RST".indexOf(n.charAt(0)) < 0) {
            throw new InvalidComponentException(Message.of(SgUen.class, "uen.other-prefix",
                    "An other-form UEN starts with R, S or T."));
        }
        if (!Strings.isDigits(n.substring(1, 3)) || !Strings.isDigits(n.substring(5, 9))
                || !Character.isLetter(n.charAt(9))) {
            throw new InvalidFormatException();
        }
        if (n.charAt(0) == 'T'
                && Integer.parseInt(n.substring(1, 3)) > LocalDate.now().getYear() % 100) {
            throw new InvalidComponentException(Message.of(SgUen.class, "uen.year.future",
                    "The year of issuance is in the future."));
        }
        if (!OTHER_ENTITY_TYPES.contains(n.substring(3, 5))) {
            throw new InvalidComponentException(Message.of(SgUen.class, "uen.entity-type",
                    "Unknown entity type."));
        }
        if (n.charAt(9) != calcOtherCheckDigit(n)) {
            throw new InvalidChecksumException();
        }
    }
}
