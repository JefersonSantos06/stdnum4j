package io.github.jefersonsantos06.stdnum.apac;

import io.github.jefersonsantos06.stdnum.numdb.NumDb;
import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidComponentException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.math.BigInteger;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * The number on a Chinese resident identity card: the administrative
 * division the holder was registered in, their date of birth, a serial
 * number and a check character.
 *
 * <p>Division codes are reused as counties are created, renamed and merged,
 * so the place a number names depends on when its holder was born: the
 * registry records the years each name applied to, and the birth date in the
 * number itself picks between them.</p>
 */
public final class CnRic implements StdNum {

    public static final CnRic INSTANCE = new CnRic();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("cn.ric", "RIC")
                    .country("CN")
                    .title("Chinese Resident Identity Card number")
                    .description("Number on a Chinese resident identity card: 18 characters"
                            + " giving the division of registration, the birth date and a"
                            + " check character.")
                    .tags(Tag.PERSON)
                    .references("https://en.wikipedia.org/wiki/Resident_Identity_Card")
                    .build();

    private CnRic() {
    }

    /** The administrative divisions, province then county. */
    private static NumDb divisions() {
        return NumDb.load(CnRic.class, "cn-loc.dat");
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, "").toUpperCase(Locale.ROOT);
    }

    /**
     * The check character, from the seventeen digits before it. Reading them
     * as a base-13 number is a shorter way of writing the weighted mod 11
     * the standard specifies.
     */
    public static char calcCheckDigit(String number) {
        String n = INSTANCE.compact(number);
        String base = n.length() > 17 ? n.substring(0, 17) : n;
        int checksum;
        try {
            checksum = BigInteger.ONE
                    .subtract(BigInteger.TWO.multiply(new BigInteger(base, 13)))
                    .mod(BigInteger.valueOf(11)).intValue();
        } catch (NumberFormatException e) {
            throw new InvalidFormatException();
        }
        return checksum == 10 ? 'X' : (char) ('0' + checksum);
    }

    /**
     * The birth date encoded in the number. For some older numbers this is
     * the date of registration instead.
     */
    public static LocalDate getBirthDate(String number) {
        String n = INSTANCE.compact(number);
        if (n.length() < 14 || !Strings.isDigits(n.substring(0, 14))) {
            throw new InvalidFormatException();
        }
        try {
            return LocalDate.of(Integer.parseInt(n.substring(6, 10)),
                    Integer.parseInt(n.substring(10, 12)), Integer.parseInt(n.substring(12, 14)));
        } catch (DateTimeException e) {
            throw new InvalidComponentException(
                    "The number does not contain a valid birth date.");
        }
    }

    /**
     * The province and county the holder was registered in, the county being
     * the one that carried the code in the year they were born.
     */
    public static Map<String, String> getBirthPlace(String number) {
        String n = INSTANCE.compact(number);
        if (n.length() < 6) {
            throw new InvalidLengthException();
        }
        Map<String, String> info = new LinkedHashMap<>();
        for (NumDb.Entry entry : divisions().info(n.substring(0, 6))) {
            info.putAll(entry.properties());
        }
        String counties = info.get("county");
        if (info.isEmpty() || counties == null) {
            throw new InvalidComponentException("Not a division that has been allocated.");
        }
        int year = getBirthDate(n).getYear();
        for (String county : counties.split(",")) {
            if (!county.startsWith("[")) {
                return info;
            }
            int close = county.indexOf(']');
            String[] bounds = county.substring(1, close).split("-", -1);
            String name = county.substring(close + 1);
            if (!bounds[0].isEmpty() && year < Integer.parseInt(bounds[0])) {
                continue;
            }
            if (bounds.length > 1 && !bounds[1].isEmpty() && year > Integer.parseInt(bounds[1])) {
                continue;
            }
            info.put("county", name);
            return info;
        }
        throw new InvalidComponentException(
                "The division carried no county in the year of birth.");
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (n.length() != 18) {
            throw new InvalidLengthException();
        }
        if (!Strings.isDigits(n.substring(0, 17))) {
            throw new InvalidFormatException();
        }
        if (n.charAt(17) != calcCheckDigit(n)) {
            throw new InvalidChecksumException();
        }
        getBirthDate(n);
        getBirthPlace(n);
        return n;
    }
}
