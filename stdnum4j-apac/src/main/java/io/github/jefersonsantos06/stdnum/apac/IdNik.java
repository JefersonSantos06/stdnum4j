package io.github.jefersonsantos06.stdnum.apac;

import io.github.jefersonsantos06.stdnum.numdb.NumDb;
import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidComponentException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.Message;
import io.github.jefersonsantos06.stdnum.spi.Reasons;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * NIK, the Indonesian citizen number: the region the holder was registered
 * in, their date of birth and a serial number.
 *
 * <p>There is no check digit; what makes a number valid is naming a region
 * that exists and a date that does. A woman's day of birth is written with
 * 40 added to it, which is how the number records sex.</p>
 */
public final class IdNik implements StdNum {

    public static final IdNik INSTANCE = new IdNik();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("id.nik", "NIK")
                    .country("ID")
                    .title("Nomor Induk Kependudukan")
                    .description("Indonesian citizen number: 16 digits giving the region of"
                            + " registration, the date of birth and a serial number.")
                    .tags(Tag.PERSON)
                    .references("https://id.wikipedia.org/wiki/Nomor_Induk_Kependudukan")
                    .build();

    private IdNik() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    /** The provinces, with their regencies and cities nested under them. */
    private static NumDb regions() {
        return NumDb.load(IdNik.class, "id-loc.dat");
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " -.");
    }

    /**
     * The birth date encoded in the number. Only two digits of the year are
     * there, so a number from before 2000 and one from after it cannot be
     * told apart; the twentieth century is preferred.
     */
    public static LocalDate getBirthDate(String number) {
        String n = INSTANCE.compact(number);
        if (n.length() < 12 || !Strings.isDigits(n.substring(0, 12))) {
            throw new InvalidFormatException();
        }
        int day = Integer.parseInt(n.substring(6, 8)) % 40;
        int month = Integer.parseInt(n.substring(8, 10));
        int year = Integer.parseInt(n.substring(10, 12));
        try {
            return LocalDate.of(year + 1900, month, day);
        } catch (DateTimeException first) {
            try {
                return LocalDate.of(year + 2000, month, day);
            } catch (DateTimeException second) {
                throw new InvalidComponentException(Reasons.birthDate());
            }
        }
    }

    /** The sex recorded in the number: a woman's day of birth is offset by 40. */
    public static char getGender(String number) {
        String n = INSTANCE.compact(number);
        if (n.length() < 8 || !Strings.isDigits(n.substring(6, 8))) {
            throw new InvalidFormatException();
        }
        return Integer.parseInt(n.substring(6, 8)) > 40 ? 'F' : 'M';
    }

    /** The province and, where the register names one, the regency or city. */
    public static Map<String, String> getRegistrationPlace(String number) {
        String n = INSTANCE.compact(number);
        if (n.length() < 4) {
            throw new InvalidLengthException();
        }
        List<NumDb.Entry> entries = regions().info(n.substring(0, 4));
        if (entries.isEmpty() || entries.get(0).properties().isEmpty()) {
            throw new InvalidComponentException(Message.of(IdNik.class, "nik.region",
                    "Not a region that has been allocated."));
        }
        // both levels name themselves name_id, so they are kept apart rather
        // than merged, where the regency would silently shadow its province
        Map<String, String> info = new LinkedHashMap<>();
        info.put("province", entries.get(0).properties().get("name_id"));
        if (entries.size() > 1 && entries.get(1).properties().containsKey("name_id")) {
            info.put("regency", entries.get(1).properties().get("name_id"));
        }
        return info;
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (!Strings.isDigits(n)) {
            throw new InvalidFormatException();
        }
        if (n.length() != 16) {
            throw new InvalidLengthException();
        }
        getBirthDate(n);
        getRegistrationPlace(n);
        return n;
    }
}
