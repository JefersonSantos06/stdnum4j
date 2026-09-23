package io.github.jefersonsantos06.stdnum.latam;

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

import java.util.Map;

/**
 * RUC, the Ecuadorian taxpayer number: thirteen digits made of the province
 * of issue, a taxpayer number and an establishment number.
 *
 * <p>The third digit says which of three schemes the checksum follows: below
 * 6 the number is a natural person's {@link EcCi} with an establishment
 * number appended, 6 marks a public body and 9 a company. The two upper
 * schemes overlap in practice, so a 6 that fails the public check is retried
 * as a natural person and a 9 as a company.</p>
 *
 * <p>The SRI changed how it numbers companies on 24 September 2021, and
 * neither change is published. A company check digit that works out to 10,
 * which the old series skipped, is now written as 1. And in all but six
 * provinces (01, 14, 19, 22, 23 and 24) new companies take their seven digits
 * from a plain counter that carries on after the last number of the old
 * series, with no check digit at all. Both rules were measured on the SRI's
 * open register of 1 September 2026: of its 827,234 numbers with a 9 in third
 * place, 718,832 close the old check digit, 2,642 close it with a 10 written
 * as 1, and the other 105,760 all lie in their province's counter.</p>
 *
 * <p>A number is taken on the counter's word only within the counter's first
 * million values. Pichincha, the busiest province, used 51,515 of them in five
 * years, so the bound leaves decades of room while still refusing a mistyped
 * number that lands far beyond anything issued.</p>
 */
public final class EcRuc implements StdNum {

    public static final EcRuc INSTANCE = new EcRuc();

    private static final int[] PUBLIC_WEIGHTS = {3, 2, 7, 6, 5, 4, 3, 2, 1};
    private static final int[] JURIDICAL_WEIGHTS = {4, 3, 2, 7, 6, 5, 4, 3, 2, 1};

    /**
     * The first value of each province's counter of company numbers without a
     * check digit, read as the seven digits after the 9. Provinces missing
     * here still give every company a check digit.
     */
    private static final Map<String, Integer> UNCHECKED_FROM = Map.ofEntries(
            Map.entry("02", 1_526_180),
            Map.entry("03", 1_034_037),
            Map.entry("04", 1_533_677),
            Map.entry("05", 1_762_673),
            Map.entry("06", 1_784_259),
            Map.entry("07", 1_840_210),
            Map.entry("08", 1_791_676),
            Map.entry("09", 3_366_529),
            Map.entry("10", 1_796_090),
            Map.entry("11", 1_795_188),
            Map.entry("12", 1_789_518),
            Map.entry("13", 1_931_701),
            Map.entry("15", 1_727_793),
            Map.entry("16", 1_728_754),
            Map.entry("17", 3_189_506),
            Map.entry("18", 1_809_245),
            Map.entry("20", 1_766_970),
            Map.entry("21", 1_773_318));

    /** How many values of a counter are accepted without a check digit. */
    private static final int UNCHECKED_SPAN = 1_000_000;

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("ec.ruc", "RUC")
                    .country("EC")
                    .title("Registro Unico de Contribuyentes")
                    .description("Ecuadorian taxpayer number: 13 digits giving the province,"
                            + " the taxpayer and the establishment.")
                    .tags(Tag.COMPANY, Tag.TAX)
                    .references("https://www.sri.gob.ec/registro-unico-de-contribuyentes-ruc")
                    .build();

    private EcRuc() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return EcCi.INSTANCE.compact(number);
    }

    private static int checksum(String n, int[] weights) {
        int sum = 0;
        for (int i = 0; i < weights.length && i < n.length(); i++) {
            sum += weights[i] * (n.charAt(i) - '0');
        }
        return sum % 11;
    }

    private static void validateNatural(String n) {
        if (n.endsWith("000")) {
            throw new InvalidComponentException(Message.of(EcRuc.class, "ruc.establishment",
                    "Not an establishment number."));
        }
        EcCi.INSTANCE.validate(n.substring(0, 10));
    }

    private static void validatePublic(String n) {
        if (n.endsWith("0000")) {
            throw new InvalidComponentException(Message.of(EcRuc.class, "ruc.establishment",
                    "Not an establishment number."));
        }
        if (checksum(n.substring(0, 9), PUBLIC_WEIGHTS) != 0) {
            throw new InvalidChecksumException();
        }
    }

    private static void validateJuridical(String n) {
        if (n.endsWith("000")) {
            throw new InvalidComponentException(Message.of(EcRuc.class, "ruc.establishment",
                    "Not an establishment number."));
        }
        int check = (11 - checksum(n.substring(0, 9), JURIDICAL_WEIGHTS)) % 11;
        if (n.charAt(9) - '0' == check || check == 10 && n.charAt(9) == '1') {
            return;
        }
        Integer uncheckedFrom = UNCHECKED_FROM.get(n.substring(0, 2));
        int serial = Integer.parseInt(n.substring(3, 10));
        if (uncheckedFrom == null || serial < uncheckedFrom
                || serial >= uncheckedFrom + UNCHECKED_SPAN) {
            throw new InvalidChecksumException();
        }
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (n.length() != 13) {
            throw new InvalidLengthException();
        }
        if (!Strings.isDigits(n)) {
            throw new InvalidFormatException();
        }
        String province = n.substring(0, 2);
        if ((province.compareTo("01") < 0 || province.compareTo("24") > 0)
                && !province.equals("30") && !province.equals("50")) {
            throw new InvalidComponentException(Reasons.provinceCode());
        }
        char kind = n.charAt(2);
        if (kind < '6') {
            validateNatural(n);
        } else if (kind == '6') {
            try {
                validatePublic(n);
            } catch (ValidationException e) {
                validateNatural(n);
            }
        } else if (kind == '9') {
            try {
                validatePublic(n);
            } catch (ValidationException e) {
                validateJuridical(n);
            }
        } else {
            throw new InvalidComponentException(Message.of(EcRuc.class, "ruc.scheme",
                    "The third digit names no taxpayer scheme."));
        }
        return n;
    }
}
