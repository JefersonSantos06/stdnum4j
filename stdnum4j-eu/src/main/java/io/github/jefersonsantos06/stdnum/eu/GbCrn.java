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
 * CRN, the number Companies House gives a United Kingdom company: eight
 * characters, either eight digits for England and Wales or a two-letter
 * prefix — the kind of body and where it is registered — and six digits.
 *
 * <p>The numbers are handed out in sequence and carry no check digit, so
 * validation is limited to the length and the prefix. Older numbers are
 * often quoted without their leading zeros ({@code 445790} for
 * {@code 00445790}); they are padded back to eight characters.</p>
 *
 * <p>A few registers break the prefix-and-six-digits rule, and those forms
 * are accepted as the register holds them: industrial and provident
 * societies ({@code IP}, {@code SP}) and registered societies ({@code RS})
 * may carry letters in the last six characters, a Scottish limited
 * partnership ({@code SL}) may end in an {@code A}, and one registered
 * society has a ninth character.</p>
 */
public final class GbCrn implements StdNum {

    public static final GbCrn INSTANCE = new GbCrn();

    /**
     * Every prefix Companies House documents. For most kinds of body there is
     * one per jurisdiction — England and Wales, Scotland, Northern Ireland:
     * limited liability partnerships (OC, SO, NC), limited partnerships
     * (LP, SL, NL), overseas companies (FC, SF, NF), EEIGs (GE, GS, GN),
     * European companies (SE, ES, EN), assurance companies (AC, SA, NA),
     * unregistered companies (ZC, SZ, NZ), Royal Charter companies (RC, SR, NR),
     * investment companies with variable capital (IC, SI, NV) and industrial
     * and provident societies (IP, SP, NP). SC and NI are Scottish and
     * Northern Irish companies, R0 a Northern Irish one from before 1922.
     */
    private static final Set<String> PREFIXES = Set.of(
            "AC", "CE", "CS", "EN", "ES", "FC", "FE", "GE", "GN", "GS", "IC", "IP",
            "LP", "NA", "NC", "NF", "NI", "NL", "NO", "NP", "NR", "NV", "NZ", "OC",
            "OE", "PC", "R0", "RC", "RS", "SA", "SC", "SE", "SF", "SG", "SI", "SL",
            "SO", "SP", "SR", "SZ", "ZC");

    /** Prefixes whose last six characters may include letters. */
    private static final Set<String> ALPHANUMERIC = Set.of("IP", "SP", "RS");

    /** Numbers on the register that fit no rule. */
    private static final Set<String> EXCEPTIONS = Set.of("RS007853Z");

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("gb.crn", "CRN")
                    .country("GB")
                    .title("Company Registration Number")
                    .description("United Kingdom company number from Companies House: 8 digits,"
                            + " or a 2-letter prefix and 6 digits.")
                    .tags(Tag.COMPANY)
                    .references(
                            "https://find-and-update.company-information.service.gov.uk/",
                            "https://chguide.co.uk/general/company-number.html",
                            "https://www.informdirect.co.uk/company-records/company-registration-number-crn-what-is-it/")
                    .build();

    private GbCrn() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        String n = Strings.compact(number, " -./").toUpperCase(Locale.ROOT);
        if (Strings.isDigits(n)) {
            return Strings.padStart(n, 8);
        }
        if (n.length() > 2 && n.length() < 8 && isLetter(n.charAt(0)) && isLetter(n.charAt(1))
                && Strings.isDigits(n.substring(2))) {
            return n.substring(0, 2) + Strings.padStart(n.substring(2), 6);
        }
        return n;
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (EXCEPTIONS.contains(n)) {
            return n;
        }
        if (n.length() != 8) {
            throw new InvalidLengthException();
        }
        if (Strings.isDigits(n)) {
            return n;
        }
        String prefix = n.substring(0, 2);
        String serial = n.substring(2);
        if (!PREFIXES.contains(prefix)) {
            throw new InvalidComponentException(Message.of(GbCrn.class, "crn.prefix",
                    "Not a Companies House prefix."));
        }
        if (Strings.isDigits(serial)
                || ALPHANUMERIC.contains(prefix) && isAlphanumeric(serial)
                || prefix.equals("SL") && Strings.isDigits(serial.substring(0, 5)) && serial.endsWith("A")) {
            return n;
        }
        throw new InvalidFormatException();
    }

    private static boolean isLetter(char c) {
        return c >= 'A' && c <= 'Z';
    }

    private static boolean isAlphanumeric(String s) {
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (!isLetter(c) && (c < '0' || c > '9')) {
                return false;
            }
        }
        return true;
    }
}
