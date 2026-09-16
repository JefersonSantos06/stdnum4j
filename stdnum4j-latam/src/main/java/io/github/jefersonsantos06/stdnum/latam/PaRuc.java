package io.github.jefersonsantos06.stdnum.latam;

import io.github.jefersonsantos06.stdnum.algo.Weighted;
import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidComponentException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.Message;
import io.github.jefersonsantos06.stdnum.spi.Reasons;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.spi.ValidationException;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.util.List;
import java.util.Locale;

/**
 * RUC (Registro Único de Contribuyentes), the Panamanian taxpayer number: the
 * fields of the register entry it was issued from, written with hyphens
 * between them, and the two check digits — the DV, dígito verificador —
 * appended when they are written out at all.
 *
 * <p>A company carries the three fields of the Public Registry entry,
 * {@code 1870951-1-1751}: rollo/tomo, folio/imagen and asiento/ficha. A
 * natural person carries their cédula, which opens with a province code
 * ({@code 8-473-515}) or with a letter saying how nationality was acquired —
 * {@code E} foreigner, {@code PE} Panamanian foreigner, {@code N} naturalised,
 * and {@code AV} and {@code PI}, which follow a province ({@code 8AV-1-196}).
 * Whoever qualifies for neither gets an NT, número tributario, whose second
 * field is the letters themselves: {@code 8-NT-2-3437}.</p>
 *
 * <p>The check digits are mod 11 over a twenty-digit base, and the base is
 * laid out differently for each register — which is why the DGI's own
 * calculator asks for the kind of taxpayer before it answers. The number does
 * not always say: a first field of one or two digits naming a province reads
 * both as a cédula and as a tomo, and an NT is written identically for a
 * person and for a company. Both readings are therefore tried, and check
 * digits that close either one are accepted. Numbers written before the
 * register was renumbered — a tomo below 50000 — take a cross-reference table
 * and weight 11 twice.</p>
 *
 * <p>A RUC written without its DV can only be taken on its shape: there is no
 * checksum in it to verify. The forms this does not cover are fincas, whose
 * rule the DGI does not publish, and the letters {@code SB} and {@code EE}.</p>
 */
public final class PaRuc implements StdNum {

    public static final PaRuc INSTANCE = new PaRuc();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("pa.ruc", "RUC")
                    .country("PA")
                    .title("Registro Único de Contribuyentes")
                    .description("Panamanian taxpayer number: the hyphen-separated fields of"
                            + " a register entry, optionally followed by two mod 11 check"
                            + " digits.")
                    .tags(Tag.TAX, Tag.VAT, Tag.PERSON, Tag.COMPANY)
                    .references("https://dgi.mef.gob.pa/DV",
                            "https://www.oecd.org/content/dam/oecd/en/topics/"
                                    + "policy-issue-focus/aeoi/panama-tin.pdf")
                    .build();

    /** The shortest RUC is {@code 0-0-0}; the longest is padded well past its fields. */
    private static final int MIN_LENGTH = 5;
    private static final int MAX_LENGTH = 40;

    /**
     * The old register's cross reference: in a number from before the
     * renumbering, the two digits at positions 5 and 6 are replaced by the
     * entry at their value. Only 00 to 49 can be reached, since a higher pair
     * is what makes a number new.
     */
    private static final String[] OLD_CROSS = {
            "00", "01", "02", "03", "04", "05", "06", "07", "08", "09",
            "01", "02", "03", "04", "05", "06", "07", "08", "09", "01",
            "02", "03", "04", "07", "08", "09", "02", "03", "04", "05",
            "06", "07", "08", "09", "01", "02", "03", "04", "05", "06",
            "07", "08", "09", "01", "02", "03", "04", "05", "06", "07"};

    private static final Message SHAPE = Message.of(PaRuc.class, "ruc.pa.shape",
            "A RUC is written as three or four fields separated by hyphens.");
    private static final Message FIELD = Message.of(PaRuc.class, "ruc.pa.field",
            "A RUC field is digits only and no longer than its register allows.");
    private static final Message LETTER = Message.of(PaRuc.class, "ruc.pa.letter",
            "Unknown letter in the first field of a RUC.");
    private static final Message CHECK_DIGITS = Message.of(PaRuc.class, "ruc.pa.check-digits",
            "The check digits appended to a RUC are two digits.");
    private static final Message AMBIGUOUS = Message.of(PaRuc.class, "ruc.pa.ambiguous",
            "The number reads as coming from more than one register, so the number"
                    + " alone does not fix its check digits.");

    private static final int[] WEIGHTS = Weighted.descending(21, 20);
    private static final int[] WEIGHTS_WITH_CHECK = Weighted.descending(22, 21);
    private static final int[] OLD_WEIGHTS = oldWeights(20);
    private static final int[] OLD_WEIGHTS_WITH_CHECK = oldWeights(21);

    /**
     * The letter a natural person's RUC may carry. {@code code} is what the
     * base takes while the folio is short enough to keep the province;
     * {@code validationCode} replaces the province once it is not. The
     * register writes the code over two positions in every branch but one,
     * which is what {@code paddedCode} is for.
     */
    private enum Letter {
        NONE("", "00", "00"),
        E("E", "5", "66"),
        PE("PE", "75", "82"),
        N("N", "4", "92"),
        AV("AV", "15", "9595"),
        PI("PI", "79", "9595");

        private final String letter;
        private final String code;
        private final String paddedCode;
        private final String validationCode;

        Letter(String letter, String code, String validationCode) {
            this.letter = letter;
            this.code = code;
            this.paddedCode = code.length() < 2 ? code + "0" : code;
            this.validationCode = validationCode;
        }

        /** Whether the letter fills the first field on its own: {@code E-8-127702}. */
        boolean standsAlone() {
            return this == E || this == PE || this == N;
        }

        /** Whether the letter follows a province code: {@code 8AV-1-196}. */
        boolean followsProvince() {
            return this == AV || this == PI;
        }
    }

    /** The register a RUC was issued from. The number does not always say which. */
    private enum Kind { NATURAL, JURIDICAL, NATURAL_NT, JURIDICAL_NT }

    /** The fields of a RUC as written, and the check digits if they were. */
    private record Fields(String head, Letter letter, boolean nt,
                          String folio, String asiento, String dv) {
    }

    /** A twenty-digit base, and whether it takes the old register's weights. */
    private record Base(String digits, boolean old) {
    }

    private PaRuc() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    /**
     * Unlike most types this keeps the hyphens: they separate the fields of
     * the register entry, and dropping them would lose what the number says.
     */
    @Override
    public String compact(String number) {
        return Strings.compact(number, " .").toUpperCase(Locale.ROOT);
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        requireAlphabet(n);
        if (n.length() < MIN_LENGTH || n.length() > MAX_LENGTH) {
            throw new InvalidLengthException();
        }
        Fields fields = split(n);
        ValidationException componentFailure = null;
        boolean understood = false;
        for (Kind kind : kinds(fields)) {
            try {
                Base base = base(kind, fields);
                understood = true;
                if (fields.dv() == null || fields.dv().equals(checkDigits(base))) {
                    return n;
                }
            } catch (ValidationException e) {
                if (componentFailure == null) {
                    componentFailure = e;
                }
            }
        }
        if (understood) {
            // a register understood the number; only the check digits refused
            throw new InvalidChecksumException();
        }
        throw componentFailure != null ? componentFailure : new InvalidFormatException(FIELD);
    }

    /**
     * The two check digits — the DV — of a RUC.
     *
     * <p>This answers only where the answer is one: a number that reads as
     * coming from two registers has two sets of check digits, and which one
     * the taxpayer carries is not in the number. The DGI's own calculator
     * asks for the kind of taxpayer first, and this refuses rather than
     * guess. Both readings really do occur — {@code 8-473-515} carries the
     * cédula's digits and {@code 1-513-153} the company's. A number written
     * with its DV gets it back, since {@link #validate} has just proved it
     * closes, and that settles the ambiguous ones for whoever has the whole
     * number.</p>
     *
     * @throws ValidationException if the number is not a valid RUC, or reads
     *                             as coming from more than one register
     */
    public static String calcCheckDigits(String number) {
        String n = INSTANCE.validate(number);
        Fields fields = split(n);
        if (fields.dv() != null) {
            return fields.dv();
        }
        String answer = null;
        for (Kind kind : kinds(fields)) {
            String digits;
            try {
                digits = checkDigits(base(kind, fields));
            } catch (ValidationException e) {
                continue; // a register this number cannot have come from
            }
            if (answer == null) {
                answer = digits;
            } else if (!answer.equals(digits)) {
                throw new InvalidComponentException(AMBIGUOUS);
            }
        }
        if (answer == null) {
            throw new InvalidComponentException(FIELD);
        }
        return answer;
    }

    private static void requireAlphabet(String n) {
        for (int i = 0; i < n.length(); i++) {
            char c = n.charAt(i);
            if (!((c >= '0' && c <= '9') || (c >= 'A' && c <= 'Z') || c == '-')) {
                throw new InvalidFormatException();
            }
        }
    }

    private static Fields split(String n) {
        // -1 keeps a trailing empty field, which is an error rather than absent
        String[] parts = n.split("-", -1);
        for (String part : parts) {
            if (part.isEmpty()) {
                throw new InvalidFormatException(SHAPE);
            }
        }
        String head = parts[0];
        boolean nt;
        int folioAt;
        if (parts.length >= 2 && parts[1].equals("NT")) {
            requireParts(parts.length, 4, 5);
            nt = true;
            folioAt = 2;
        } else if (head.length() > 2 && head.endsWith("NT")) {
            // 8 NT-1-22684 loses its space to compact and arrives like this
            requireParts(parts.length, 3, 4);
            nt = true;
            head = head.substring(0, head.length() - 2);
            folioAt = 1;
        } else {
            requireParts(parts.length, 3, 4);
            nt = false;
            folioAt = 1;
        }
        Letter letter = nt ? Letter.NONE : letterOf(head);
        String province = head.substring(0, head.length() - letter.letter.length());
        String dv = parts.length == folioAt + 3 ? parts[parts.length - 1] : null;
        if (dv != null && !(dv.length() == 2 && Strings.isDigits(dv))) {
            throw new InvalidFormatException(CHECK_DIGITS);
        }
        return new Fields(province, letter, nt, parts[folioAt], parts[folioAt + 1], dv);
    }

    private static void requireParts(int count, int without, int with) {
        if (count != without && count != with) {
            throw new InvalidFormatException(SHAPE);
        }
    }

    private static Letter letterOf(String head) {
        if (Strings.isDigits(head)) {
            return Letter.NONE;
        }
        for (Letter letter : Letter.values()) {
            if (letter.standsAlone() && head.equals(letter.letter)) {
                return letter; // equals, not endsWith: PE is not an E
            }
            if (letter.followsProvince() && head.endsWith(letter.letter)
                    && Strings.isDigits(head.substring(0, head.length() - 2))) {
                return letter;
            }
        }
        throw new InvalidComponentException(LETTER);
    }

    private static List<Kind> kinds(Fields fields) {
        if (fields.nt()) {
            return List.of(Kind.NATURAL_NT, Kind.JURIDICAL_NT);
        }
        if (fields.letter() != Letter.NONE) {
            return List.of(Kind.NATURAL);
        }
        return isProvinceCode(fields.head())
                ? List.of(Kind.NATURAL, Kind.JURIDICAL) // 8-473-515 reads as both
                : List.of(Kind.JURIDICAL);              // 49-572-8631 as only one
    }

    private static Base base(Kind kind, Fields fields) {
        String folio = trimZeros(fields.folio());
        String asiento = trimZeros(fields.asiento());
        switch (kind) {
            case JURIDICAL: {
                String tomo = requireField(trimZeros(fields.head()), 9);
                requireField(folio, 4);
                requireField(asiento, 6);
                String digits = Strings.padStart(tomo, 10)
                        + Strings.padStart(folio, 4)
                        + Strings.padStart(asiento, 6);
                boolean old = digits.charAt(3) == '0' && digits.charAt(4) == '0'
                        && digits.charAt(5) < '5';
                if (old) {
                    int pair = (digits.charAt(5) - '0') * 10 + (digits.charAt(6) - '0');
                    digits = digits.substring(0, 5) + OLD_CROSS[pair] + digits.substring(7);
                }
                return new Base(digits, old);
            }
            case NATURAL:
                return new Base(Strings.padStart(natural(fields, folio, asiento), 20), false);
            case NATURAL_NT: {
                String province = Strings.padStart(requireProvince(fields.head()), 2);
                requireField(folio, 3);
                requireField(asiento, 6);
                String head = "5" + province + "43" + Strings.padStart(folio, 3);
                return new Base(asiento.length() <= 5
                        ? "0".repeat(7) + head + Strings.padStart(asiento, 5)
                        : "0".repeat(6) + head + Strings.padStart(asiento, 6), false);
            }
            default: { // JURIDICAL_NT
                String province = Strings.padStart(requireProvince(fields.head()), 2);
                requireField(folio, 3);
                requireField(asiento, 7);
                // the same base as a person's, without the 5 in front
                String head = province + "43" + Strings.padStart(folio, 3);
                return new Base(asiento.length() == 6
                        ? "0".repeat(7) + head + Strings.padStart(asiento, 6)
                        : "0".repeat(8) + head + Strings.padStart(Strings.first(asiento, 5), 5),
                        false);
            }
        }
    }

    private static String natural(Fields fields, String folio, String asiento) {
        Letter letter = fields.letter();
        requireField(folio, 4);
        requireField(asiento, letter.followsProvince() ? 8 : 9);
        String five = Strings.padStart(Strings.first(asiento, 5), 5);
        String shortFolio = Strings.padStart(folio, 3);
        String longFolio = Strings.padStart(folio, 4);
        // a folio of four digits pushes the province out of the base
        boolean keepsProvince = folio.length() < 4;
        if (letter.followsProvince()) {
            String province = Strings.padStart(requireProvince(fields.head()), 2);
            return keepsProvince
                    ? "5" + province + letter.code + shortFolio + five
                    : "5" + letter.validationCode + longFolio + five;
        }
        if (letter.standsAlone()) {
            // E and N spell a six-digit asiento out in full rather than cut it
            // to five, and the register writes the bare code in the last
            // branch alone; both asymmetries are its own, not slips
            boolean six = asiento.length() == 6 && (letter == Letter.E || letter == Letter.N);
            String tail = six ? asiento : five;
            return keepsProvince
                    ? "5" + "00" + letter.paddedCode + shortFolio + tail
                    : "5" + letter.validationCode
                            + (six ? letter.paddedCode : letter.code) + longFolio + tail;
        }
        return "5" + Strings.padStart(requireProvince(fields.head()), 2) + "00"
                + shortFolio + five;
    }

    private static String checkDigits(Base base) {
        int[] first = base.old() ? OLD_WEIGHTS : WEIGHTS;
        int[] second = base.old() ? OLD_WEIGHTS_WITH_CHECK : WEIGHTS_WITH_CHECK;
        int one = Weighted.mod11CheckDigit(base.digits(), first);
        int two = Weighted.mod11CheckDigit(base.digits() + one, second);
        return "" + one + two;
    }

    /**
     * The weights of a number from the old register, right to left: 2 up to
     * 11 as usual, then 11 a second time, and on up. Everything to the left
     * of the repeat is therefore one lower than the plain descending run the
     * current numbering takes.
     */
    private static int[] oldWeights(int length) {
        int[] weights = new int[length];
        for (int i = 0; i < length; i++) {
            int fromRight = length - 1 - i;
            weights[i] = fromRight < 10 ? fromRight + 2 : fromRight + 1;
        }
        return weights;
    }

    /** The field without the zeros a printed form pads it with; never empty. */
    private static String trimZeros(String field) {
        int i = 0;
        while (i < field.length() - 1 && field.charAt(i) == '0') {
            i++;
        }
        return field.substring(i);
    }

    private static String requireField(String field, int maxLength) {
        if (!Strings.isDigits(field) || field.length() > maxLength) {
            throw new InvalidFormatException(FIELD);
        }
        return field;
    }

    private static String requireProvince(String head) {
        String province = trimZeros(head);
        if (!isProvinceCode(head)) {
            throw new InvalidComponentException(Reasons.unknownProvince());
        }
        return province;
    }

    private static boolean isProvinceCode(String head) {
        String province = trimZeros(head);
        return Strings.isDigits(province) && province.length() <= 2
                && Integer.parseInt(province) <= 13;
    }
}
