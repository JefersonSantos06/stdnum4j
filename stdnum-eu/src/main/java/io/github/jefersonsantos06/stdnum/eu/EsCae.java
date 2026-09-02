package io.github.jefersonsantos06.stdnum.eu;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.util.Locale;
import java.util.Set;

/**
 * CAE, the code the Spanish tax agency gives an establishment dealing in
 * goods that carry excise duty: ES, three zeros, the managing office, the
 * activity carried out there, a serial number and a check letter.
 */
public final class EsCae implements StdNum {

    public static final EsCae INSTANCE = new EsCae();

    /**
     * The activities an establishment can be authorised for: warehousing,
     * production, receipt and the rest, each under its own key.
     */
    private static final Set<String> ACTIVITY_KEYS = Set.of(
            "A0", "A1", "A2", "A6", "A7", "A9", "AC", "AF", "AT", "AV", "AW", "AX", "B0", "B1",
            "B6", "B7", "B9", "BA", "BT", "C1", "C7", "DA", "DB", "DF", "DM", "DP", "E7", "EC",
            "F1", "H0", "H1", "H2", "H4", "H6", "H7", "H8", "H9", "HA", "HB", "HC", "HD", "HE",
            "HF", "HH", "HI", "HJ", "HK", "HL", "HM", "HN", "HP", "HQ", "HR", "HS", "HT", "HU",
            "HV", "HW", "HX", "HZ", "L0", "L1", "L2", "L3", "L7", "M7", "OA", "OB", "OE", "OH",
            "OR", "OT", "OV", "PF", "RF", "T1", "T7", "TT", "V1", "V7", "VD");

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("es.cae", "CAE")
                    .country("ES")
                    .title("Codigo de Actividad y Establecimiento")
                    .description("Code of a Spanish establishment dealing in excise goods:"
                            + " 13 characters naming the office and the activity.")
                    .tags(Tag.COMPANY, Tag.EXCISE, Tag.TAX)
                    .references("https://sede.agenciatributaria.gob.es/Sede/"
                            + "impuestos-especiales-medioambientales/"
                            + "censo-impuestos-especiales-medioambientales/"
                            + "registro-impuestos-especiales-fabricacion.html")
                    .build();

    private EsCae() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, "").toUpperCase(Locale.ROOT);
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (n.length() != 13) {
            throw new InvalidLengthException();
        }
        if (!n.startsWith("ES") || !n.startsWith("000", 2)) {
            throw new InvalidFormatException();
        }
        // the managing offices are numbered contiguously from 01 to 56
        String office = n.substring(5, 7);
        if (!Strings.isDigits(office)
                || office.compareTo("01") < 0 || office.compareTo("56") > 0) {
            throw new InvalidFormatException();
        }
        if (!ACTIVITY_KEYS.contains(n.substring(7, 9))) {
            throw new InvalidFormatException();
        }
        if (!Strings.isDigits(n.substring(9, 12)) || !Character.isLetter(n.charAt(12))) {
            throw new InvalidFormatException();
        }
        return n;
    }
}
