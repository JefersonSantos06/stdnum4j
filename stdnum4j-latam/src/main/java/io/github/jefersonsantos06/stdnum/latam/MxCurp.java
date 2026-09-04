package io.github.jefersonsantos06.stdnum.latam;

import io.github.jefersonsantos06.stdnum.spi.Dates;
import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidComponentException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.Message;
import io.github.jefersonsantos06.stdnum.spi.Reasons;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.time.LocalDate;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * CURP, the Mexican population registry code: letters from the name, the date
 * of birth, the sex, the state of birth, three more letters from the name and
 * a check digit.
 *
 * <p>Because the letters are drawn from the name, a code can come out reading
 * as an obscenity. The registry keeps a list of those four-letter openings and
 * replaces the second letter with an X, so a code beginning with one of them
 * was never issued.</p>
 */
public final class MxCurp implements StdNum {

    public static final MxCurp INSTANCE = new MxCurp();

    private static final String ALPHABET = "0123456789ABCDEFGHIJKLMN&OPQRSTUVWXYZ";
    private static final Pattern PATTERN =
            Pattern.compile("[A-Z]{4}[0-9]{6}[A-Z]{6}[0-9A-Z][0-9]");

    /** The four-letter openings the registry replaces rather than issues. */
    private static final Set<String> NAME_BLACKLIST = Set.of(
            "BACA", "BAKA", "BUEI", "BUEY", "CACA", "CACO", "CAGA", "CAGO", "CAKA", "CAKO",
            "COGE", "COGI", "COJA", "COJE", "COJI", "COJO", "COLA", "CULO", "FALO", "FETO",
            "GETA", "GUEI", "GUEY", "JETA", "JOTO", "KACA", "KACO", "KAGA", "KAGO", "KAKA",
            "KAKO", "KOGE", "KOGI", "KOJA", "KOJE", "KOJI", "KOJO", "KOLA", "KULO", "LILO",
            "LOCA", "LOCO", "LOKA", "LOKO", "MAME", "MAMO", "MEAR", "MEAS", "MEON", "MIAR",
            "MION", "MOCO", "MOKO", "MULA", "MULO", "NACA", "NACO", "PEDA", "PEDO", "PENE",
            "PIPI", "PITO", "POPO", "PUTA", "PUTO", "QULO", "RATA", "ROBA", "ROBE", "ROBO",
            "RUIN", "SENO", "TETA", "VACA", "VAGA", "VAGO", "VAKA", "VUEI", "VUEY", "WUEI",
            "WUEY");

    /** The states of birth, plus NE for a Mexican born abroad. */
    private static final Set<String> STATES = Set.of(
            "AS", "BC", "BS", "CC", "CH", "CL", "CM", "CS", "DF", "DG", "GR", "GT", "HG",
            "JC", "MC", "MN", "MS", "NE", "NL", "NT", "OC", "PL", "QR", "QT", "SL", "SP",
            "SR", "TC", "TL", "TS", "VZ", "YN", "ZS");

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("mx.curp", "CURP")
                    .country("MX")
                    .title("Clave Unica de Registro de Poblacion")
                    .description("Mexican population registry code: 18 characters giving the"
                            + " name, the date of birth, the sex and the state of birth.")
                    .tags(Tag.PERSON)
                    .references("https://en.wikipedia.org/wiki/Unique_Population_Registry_Code")
                    .build();

    private MxCurp() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, "-_ ").toUpperCase(Locale.ROOT);
    }

    /**
     * The birth date encoded in the code. The century comes from the
     * seventeenth character, a digit for the twentieth century and a letter
     * for the twenty-first.
     */
    public static LocalDate getBirthDate(String number) {
        String n = INSTANCE.compact(number);
        if (n.length() != 18) {
            throw new InvalidLengthException();
        }
        // all three parses inside the guard: an 18-character CURP whose date
        // is not digits at all must be refused, not thrown out of
        if (!Strings.isDigits(n.substring(4, 10))) {
            throw new InvalidComponentException(Reasons.birthDate());
        }
        int year = Integer.parseInt(n.substring(4, 6))
                + (Character.isDigit(n.charAt(16)) ? 1900 : 2000);
        return Dates.birthDate(year, Integer.parseInt(n.substring(6, 8)),
                Integer.parseInt(n.substring(8, 10)));
    }

    /** The sex recorded in the code, {@code 'M'} or {@code 'F'}. */
    public static char getGender(String number) {
        String n = INSTANCE.compact(number);
        if (n.length() != 18) {
            throw new InvalidLengthException();
        }
        return switch (n.charAt(10)) {
            case 'H' -> 'M';
            case 'M' -> 'F';
            default -> throw new InvalidComponentException(Message.of(MxCurp.class, "curp.sex",
                    "The character in eleventh position is not a sex."));
        };
    }

    /** The check digit of a code, from its first seventeen characters. */
    public static char calcCheckDigit(String number) {
        String n = INSTANCE.compact(number);
        int check = 0;
        for (int i = 0; i < 17 && i < n.length(); i++) {
            int value = ALPHABET.indexOf(n.charAt(i));
            if (value < 0) {
                throw new InvalidFormatException();
            }
            check += value * (18 - i);
        }
        return (char) ('0' + (10 - check % 10) % 10);
    }

    @Override
    public String validate(String number) {
        return validate(number, true);
    }

    /**
     * Validates the code, optionally without the check digit. Codes issued
     * before the digit was introduced do not carry a meaningful one.
     */
    public String validate(String number, boolean validateCheckDigit) {
        String n = compact(number);
        if (n.length() != 18) {
            throw new InvalidLengthException();
        }
        if (!PATTERN.matcher(n).matches()) {
            throw new InvalidFormatException();
        }
        if (NAME_BLACKLIST.contains(n.substring(0, 4))) {
            throw new InvalidComponentException(Message.of(MxCurp.class, "curp.name-blacklist",
                    "This opening is replaced rather than issued."));
        }
        getBirthDate(n);
        getGender(n);
        if (!STATES.contains(n.substring(11, 13))) {
            throw new InvalidComponentException(Message.of(MxCurp.class, "curp.state",
                    "Not the code of a state of birth."));
        }
        if (validateCheckDigit && n.charAt(17) != calcCheckDigit(n)) {
            throw new InvalidChecksumException();
        }
        return n;
    }
}
