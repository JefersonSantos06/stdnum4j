package io.github.jefersonsantos06.stdnum.international;

import io.github.jefersonsantos06.stdnum.numdb.NumDb;
import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidComponentException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.spi.ValidationException;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.math.BigDecimal;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A GS1-128 element string: the data carried by the barcode on a shipping
 * unit, as a run of application identifiers each followed by its value.
 *
 * <p>Unlike everything else here this is not one number but a small format.
 * Each identifier says what its value means and how wide it is, so reading
 * an element string means walking it identifier by identifier — and a value
 * of variable width either runs to its maximum or ends at a separator.</p>
 *
 * <p>{@link #validate(String)} returns the element string re-encoded from
 * what it decoded, which is what makes it a stable form: identifiers come
 * out in order, and variable values padded to their width unless a separator
 * was given.</p>
 *
 * <p>Three things the reference allows are refused here, each of which would
 * otherwise turn bad input into a plausible-looking result:</p>
 *
 * <ul>
 *   <li>an element string that carries nothing;</li>
 *   <li>one that names the same identifier twice, since decoding into a map
 *       keyed by identifier lets a repeat overwrite its predecessor, and a
 *       long run of digits could come back as a short valid string with most
 *       of it thrown away;</li>
 *   <li>one that ends part-way through a value, which would otherwise be
 *       read as a date of 1 January 1900 and re-encoded as though it had
 *       been there all along.</li>
 * </ul>
 */
public final class Gs1128 implements StdNum {

    public static final Gs1128 INSTANCE = new Gs1128();

    /** Identifiers whose value is itself a number this library knows. */
    private static final Map<String, StdNum> VALIDATORS = Map.of(
            "01", Ean.INSTANCE, "02", Ean.INSTANCE, "8007", Iban.INSTANCE);

    private static final Pattern PART = Pattern.compile("[NXY][0-9]*?[.]*([0-9]+)[\\[\\]]?");

    /** Two-digit years run 1969 to 2068, as the barcode standard reads them. */
    private static final DateTimeFormatter SHORT_YEAR = new DateTimeFormatterBuilder()
            .appendValueReduced(ChronoField.YEAR, 2, 2, 1969)
            .toFormatter();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("gs1_128", "GS1-128")
                    .title("GS1-128 element string")
                    .description("The data of a GS1-128 barcode: application identifiers, each"
                            + " followed by the value it introduces.")
                    .tags(Tag.PRODUCT, Tag.MEDIA)
                    .references("https://ref.gs1.org/ai/")
                    .build();

    private Gs1128() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    /** The application identifiers and what each one introduces. */
    private static NumDb identifiers() {
        return NumDb.load(Gs1128.class, "gs1-ai.dat");
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, "()");
    }

    /** The widest value a format can hold. */
    static int maxLength(String format) {
        int total = 0;
        for (String part : format.split("\\+")) {
            Matcher m = PART.matcher(part);
            if (m.matches()) {
                total += Integer.parseInt(m.group(1));
            }
        }
        return total;
    }

    /**
     * What the element string says, as application identifiers mapped to
     * their values: a {@code String}, a {@code Long}, a {@link BigDecimal},
     * a {@link LocalDate} or {@link LocalDateTime}, or a {@link List} of two
     * of those where the identifier introduces a pair.
     */
    public static Map<String, Object> info(String number) {
        return info(number, "");
    }

    /**
     * What the element string says.
     *
     * @param separator the FNC1 stand-in that ends a value of variable width;
     *                  without one such a value runs to its full width
     */
    public static Map<String, Object> info(String number, String separator) {
        String rest = INSTANCE.compact(number);
        Map<String, Object> data = new TreeMap<>();
        if (!separator.isEmpty() && rest.startsWith(separator)) {
            rest = rest.substring(separator.length());
        }
        if (rest.isEmpty()) {
            throw new InvalidLengthException("An element string carries at least one identifier.");
        }
        while (!rest.isEmpty()) {
            NumDb.Entry entry = identifiers().info(rest).get(0);
            String ai = entry.part();
            Map<String, String> properties = entry.properties();
            if (properties.isEmpty() || !rest.startsWith(ai)) {
                throw new InvalidComponentException("Not an application identifier: " + ai);
            }
            rest = rest.substring(ai.length());
            String format = properties.get("format");
            String value = rest.substring(0, Math.min(maxLength(format), rest.length()));
            if (!separator.isEmpty() && properties.containsKey("fnc1")) {
                int end = rest.indexOf(separator);
                if (end > 0) {
                    value = rest.substring(0, end);
                }
            }
            rest = rest.substring(value.length());
            requireWholeValue(ai, format, value);
            StdNum validator = VALIDATORS.get(ai);
            if (validator != null) {
                validator.validate(value);
            }
            if (data.put(ai, decode(ai, format, properties.get("type"), value)) != null) {
                // keeping only the last value would discard the rest of the
                // string without saying so
                throw new InvalidComponentException(
                        "The identifier " + ai + " appears more than once.");
            }
            if (!separator.isEmpty() && rest.startsWith(separator)) {
                rest = rest.substring(separator.length());
            }
        }
        return data;
    }

    /**
     * Refuses a value the string ran out before completing. A format with no
     * {@code ..} and no optional part is exactly as wide as it says, and an
     * identifier at the very end of a string with nothing after it carries
     * nothing at all.
     */
    private static void requireWholeValue(String ai, String format, String value) {
        if (value.isEmpty()) {
            throw new InvalidComponentException("The identifier " + ai + " carries no value.");
        }
        boolean fixed = !format.contains("..") && !format.contains("[");
        if (fixed && value.length() != maxLength(format)) {
            throw new InvalidComponentException(
                    "The value of " + ai + " is " + value.length() + " long, not "
                            + maxLength(format) + ".");
        }
    }

    /** One value, read as whatever its identifier says it is. */
    private static Object decode(String ai, String format, String type, String value) {
        return switch (type == null ? "str" : type) {
            case "decimal" -> decodeDecimal(ai, format, value);
            case "date" -> decodeDate(format, value);
            case "int" -> Long.parseLong(value);
            default -> value.strip();
        };
    }

    private static Object decodeDecimal(String ai, String format, String value) {
        if (format.startsWith("N3+")) {
            // the first three digits are a currency or country, not the amount
            return List.of(value.substring(0, 3),
                    decodeDecimal(ai, format.substring(3), value.substring(3)));
        }
        int decimals = ai.charAt(ai.length() - 1) - '0';
        String digits = decimals == 0 ? value
                : value.substring(0, value.length() - decimals) + '.'
                        + value.substring(value.length() - decimals);
        return new BigDecimal(digits);
    }

    private static Object decodeDate(String format, String value) {
        try {
            if (format.equals("N8")) {
                return LocalDate.parse(value, DateTimeFormatter.ofPattern("uuuuMMdd"));
            }
            if (format.equals("N12")) {
                return LocalDateTime.parse(value, DateTimeFormatter.ofPattern("uuuuMMddHHmm"));
            }
            if (value.length() == 6) {
                if (value.endsWith("00")) {
                    // a day of 00 means the end of that month
                    return YearMonth.parse(value.substring(0, 4),
                            new DateTimeFormatterBuilder().append(SHORT_YEAR)
                                    .appendValue(ChronoField.MONTH_OF_YEAR, 2).toFormatter())
                            .atEndOfMonth();
                }
                return LocalDate.parse(value, shortDate("MMdd"));
            }
            if (value.length() == 12 && (format.equals("N6..12") || format.equals("N6[+N6]"))) {
                return List.of(decodeDate("N6", value.substring(0, 6)),
                        decodeDate("N6", value.substring(6)));
            }
            String tail = "MMddHHmmss".substring(0, value.length() - 2);
            return LocalDateTime.parse(value, shortDate(tail));
        } catch (DateTimeException e) {
            throw new InvalidComponentException("Not a date: " + value);
        }
    }

    /** A formatter for a two-digit year followed by {@code tail}. */
    private static DateTimeFormatter shortDate(String tail) {
        return new DateTimeFormatterBuilder()
                .append(SHORT_YEAR)
                .appendPattern(tail)
                .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
                .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
                .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
                .toFormatter();
    }

    /** The element string these values make, in its stable form. */
    public static String encode(Map<String, Object> data) {
        return encode(data, "", false);
    }

    /**
     * The element string these values make.
     *
     * @param separator   the FNC1 stand-in to put after a value of variable
     *                    width; without one such values are padded instead
     * @param parentheses whether to set off each identifier for reading
     */
    public static String encode(Map<String, Object> data, String separator, boolean parentheses) {
        StringBuilder fixed = new StringBuilder();
        List<String[]> variable = new ArrayList<>();
        for (Map.Entry<String, Object> item : new TreeMap<>(data).entrySet()) {
            NumDb.Entry entry = identifiers().info(item.getKey()).get(0);
            Map<String, String> properties = entry.properties();
            if (properties.isEmpty()) {
                throw new InvalidComponentException(
                        "Not an application identifier: " + item.getKey());
            }
            String ai = entry.part();
            StdNum validator = VALIDATORS.get(ai);
            if (validator != null) {
                validator.validate(String.valueOf(item.getValue()));
            }
            String format = properties.get("format");
            String type = properties.get("type");
            String[] encoded = encodeValue(ai, format, type, item.getValue());
            String shown = parentheses ? "(" + encoded[0] + ")" : encoded[0];
            if (!properties.containsKey("fnc1")) {
                fixed.append(shown).append(encoded[1]);
            } else {
                variable.add(new String[] {shown, format, type, encoded[1]});
            }
        }
        StringBuilder out = new StringBuilder(fixed);
        for (int i = 0; i < variable.size(); i++) {
            String[] item = variable.get(i);
            boolean last = i == variable.size() - 1;
            out.append(item[0]);
            out.append(last || !separator.isEmpty() ? item[3] : pad(item[1], item[2], item[3]));
            if (!last) {
                out.append(separator);
            }
        }
        return out.toString();
    }

    /** A value widened to its format, numbers to the right and text to the left. */
    private static String pad(String format, String type, String value) {
        int width = maxLength(format);
        String padding = " ".repeat(Math.max(0, width - value.length()));
        if ("decimal".equals(type) || "int".equals(type)) {
            return "0".repeat(Math.max(0, width - value.length())) + value;
        }
        return value + padding;
    }

    /** One value written out, with the identifier it ends up under. */
    private static String[] encodeValue(String ai, String format, String type, Object value) {
        if ("decimal".equals(type)) {
            return encodeDecimal(ai, format, value);
        }
        if ("date".equals(type)) {
            return new String[] {ai, encodeDate(format, value)};
        }
        return new String[] {ai, String.valueOf(value)};
    }

    private static String[] encodeDecimal(String ai, String format, Object value) {
        if (value instanceof List<?> pair && format.startsWith("N3+")) {
            String[] rest = encodeDecimal(ai, format.substring(3), pair.get(1));
            String head = String.valueOf(pair.get(0));
            return new String[] {rest[0], "0".repeat(Math.max(0, 3 - head.length())) + head + rest[1]};
        }
        String text = value instanceof BigDecimal decimal
                ? decimal.toPlainString() : String.valueOf(value);
        boolean variable = format.startsWith("N..");
        int length = Integer.parseInt(format.substring(variable ? 3 : 1));
        text = text.substring(0, Math.min(length + 1, text.length()));
        int dot = text.indexOf('.');
        String whole = dot < 0 ? text : text.substring(0, dot);
        String decimals = dot < 0 ? "" : text.substring(dot + 1);
        decimals = decimals.substring(0, Math.min(9, decimals.length()));
        // the last digit of the identifier records how many decimals there are
        String withCount = ai.substring(0, ai.length() - 1) + decimals.length();
        String digits = whole + decimals;
        return new String[] {withCount,
                variable ? digits : "0".repeat(Math.max(0, length - digits.length())) + digits};
    }

    private static String encodeDate(String format, Object value) {
        if (value instanceof List<?> pair
                && (format.equals("N6..12") || format.equals("N6[+N6]"))) {
            return encodeDate("N6", pair.get(0)) + encodeDate("N6", pair.get(1));
        }
        if (!(value instanceof LocalDate) && !(value instanceof LocalDateTime)) {
            return String.valueOf(value);
        }
        LocalDateTime moment = value instanceof LocalDate date
                ? date.atStartOfDay() : (LocalDateTime) value;
        return switch (format) {
            case "N6", "N6..12", "N6[+N6]" -> moment.format(pattern("yyMMdd"));
            case "N8" -> moment.format(pattern("uuuuMMdd"));
            case "N10" -> moment.format(pattern("yyMMddHHmm"));
            case "N12" -> moment.format(pattern("uuuuMMddHHmm"));
            case "N6+N..4", "N6[+N..4]", "N6[+N4]" ->
                    trimZeroes(moment.format(pattern("yyMMddHHmm")));
            case "N8+N..4", "N8[+N..4]" ->
                    trimZeroes(moment.format(pattern("yyMMddHHmmss")));
            default -> throw new InvalidFormatException("Unsupported date format: " + format);
        };
    }

    private static DateTimeFormatter pattern(String pattern) {
        return DateTimeFormatter.ofPattern(pattern);
    }

    /** A time with its trailing zero fields dropped, as the standard allows. */
    private static String trimZeroes(String value) {
        String text = value;
        for (int i = 0; i < 2 && text.endsWith("00"); i++) {
            text = text.substring(0, text.length() - 2);
        }
        return text;
    }

    @Override
    public String validate(String number) {
        return validate(number, "");
    }

    /**
     * Validates the element string and returns it re-encoded, optionally with
     * a separator standing in for FNC1 in both directions.
     */
    public String validate(String number, String separator) {
        try {
            return encode(info(number, separator), separator, false);
        } catch (ValidationException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new InvalidFormatException();
        }
    }
}
