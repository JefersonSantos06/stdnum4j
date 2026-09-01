package io.github.jefersonsantos06.stdnum.eu;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidComponentException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

/**
 * CNP (Cod Numeric Personal), the Romanian personal numeric code: thirteen
 * digits carrying gender and century in the first digit, a six-digit birth
 * date, a two-digit county, a serial and a weighted mod 11 check digit.
 */
public final class RoCnp implements StdNum {

    public static final RoCnp INSTANCE = new RoCnp();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("ro.cnp", "CNP")
                    .country("RO")
                    .title("Cod Numeric Personal")
                    .description("Romanian personal numeric code: 13 digits encoding gender,"
                            + " birth date and county, with a weighted mod 11 check digit.")
                    .tags(Tag.PERSON)
                    .build();

    private static final int[] WEIGHTS = {2, 7, 9, 1, 4, 6, 3, 5, 8, 2, 7, 9};

    /** The century each leading digit selects; others default to the 1900s. */
    private static final Map<Character, Integer> CENTURIES = Map.of(
            '1', 1900, '2', 1900, '3', 1800, '4', 1800, '5', 2000, '6', 2000);

    private static final Map<String, String> COUNTIES = Map.ofEntries(
            Map.entry("01", "Alba"), Map.entry("02", "Arad"), Map.entry("03", "Argeș"),
            Map.entry("04", "Bacău"), Map.entry("05", "Bihor"), Map.entry("06", "Bistrița-Năsăud"),
            Map.entry("07", "Botoșani"), Map.entry("08", "Brașov"), Map.entry("09", "Brăila"),
            Map.entry("10", "Buzău"), Map.entry("11", "Caraș-Severin"), Map.entry("12", "Cluj"),
            Map.entry("13", "Constanța"), Map.entry("14", "Covasna"), Map.entry("15", "Dâmbovița"),
            Map.entry("16", "Dolj"), Map.entry("17", "Galați"), Map.entry("18", "Gorj"),
            Map.entry("19", "Harghita"), Map.entry("20", "Hunedoara"), Map.entry("21", "Ialomița"),
            Map.entry("22", "Iași"), Map.entry("23", "Ilfov"), Map.entry("24", "Maramureș"),
            Map.entry("25", "Mehedinți"), Map.entry("26", "Mureș"), Map.entry("27", "Neamț"),
            Map.entry("28", "Olt"), Map.entry("29", "Prahova"), Map.entry("30", "Satu Mare"),
            Map.entry("31", "Sălaj"), Map.entry("32", "Sibiu"), Map.entry("33", "Suceava"),
            Map.entry("34", "Teleorman"), Map.entry("35", "Timiș"), Map.entry("36", "Tulcea"),
            Map.entry("37", "Vaslui"), Map.entry("38", "Vâlcea"), Map.entry("39", "Vrancea"),
            Map.entry("40", "București"),
            Map.entry("41", "București - Sector 1"), Map.entry("42", "București - Sector 2"),
            Map.entry("43", "București - Sector 3"), Map.entry("44", "București - Sector 4"),
            Map.entry("45", "București - Sector 5"), Map.entry("46", "București - Sector 6"),
            Map.entry("47", "București - Sector 7 (desființat)"),
            Map.entry("48", "București - Sector 8 (desființat)"),
            Map.entry("51", "Călărași"), Map.entry("52", "Giurgiu"),
            Map.entry("70", "Any"),
            Map.entry("80", "Unknown"), Map.entry("81", "Unknown"),
            Map.entry("82", "Unknown"), Map.entry("83", "Unknown"));

    private RoCnp() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " -.");
    }

    /** The check digit for the twelve-digit base; a remainder of 10 gives 1. */
    public static char calcCheckDigit(String base) {
        int sum = 0;
        for (int i = 0; i < WEIGHTS.length && i < base.length(); i++) {
            sum += WEIGHTS[i] * (base.charAt(i) - '0');
        }
        int check = sum % 11;
        return check == 10 ? '1' : (char) ('0' + check);
    }

    /** The birth date encoded in the number. */
    public static LocalDate getBirthDate(String number) {
        String n = INSTANCE.compact(number);
        if (!Strings.isDigits(n) || n.length() != 13) {
            throw new InvalidFormatException();
        }
        int year = Integer.parseInt(n.substring(1, 3))
                + CENTURIES.getOrDefault(n.charAt(0), 1900);
        try {
            return LocalDate.of(year,
                    Integer.parseInt(n.substring(3, 5)),
                    Integer.parseInt(n.substring(5, 7)));
        } catch (DateTimeException e) {
            throw new InvalidComponentException(
                    "The number does not contain a valid birth date.");
        }
    }

    /** The county the number was issued in. */
    public static Optional<String> getCounty(String number) {
        String n = INSTANCE.compact(number);
        if (n.length() != 13) {
            throw new InvalidLengthException();
        }
        return Optional.ofNullable(COUNTIES.get(n.substring(7, 9)));
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (!Strings.isDigits(n)) {
            throw new InvalidFormatException();
        }
        if (n.charAt(0) == '0') {
            // 7 and 8 mark foreign residents, 9 another kind of foreigner
            throw new InvalidComponentException("A CNP does not start with 0.");
        }
        if (n.length() != 13) {
            throw new InvalidLengthException();
        }
        getBirthDate(n);
        if (getCounty(n).isEmpty()) {
            throw new InvalidComponentException("Unknown county code.");
        }
        if (n.charAt(12) != calcCheckDigit(n)) {
            throw new InvalidChecksumException();
        }
        return n;
    }
}
