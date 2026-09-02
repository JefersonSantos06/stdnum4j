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

/**
 * RUC, the Ecuadorian taxpayer number: thirteen digits made of the province
 * of issue, a taxpayer number and an establishment number.
 *
 * <p>The third digit says which of three schemes the checksum follows: below
 * 6 the number is a natural person's {@link EcCi} with an establishment
 * number appended, 6 marks a public body and 9 a company. The two upper
 * schemes overlap in practice, so a 6 that fails the public check is retried
 * as a natural person and a 9 as a company.</p>
 */
public final class EcRuc implements StdNum {

    public static final EcRuc INSTANCE = new EcRuc();

    private static final int[] PUBLIC_WEIGHTS = {3, 2, 7, 6, 5, 4, 3, 2, 1};
    private static final int[] JURIDICAL_WEIGHTS = {4, 3, 2, 7, 6, 5, 4, 3, 2, 1};

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("ec.ruc", "RUC")
                    .country("EC")
                    .title("Registro Unico de Contribuyentes")
                    .description("Ecuadorian taxpayer number: 13 digits giving the province,"
                            + " the taxpayer and the establishment.")
                    .tags(Tag.COMPANY, Tag.TAX)
                    .references("https://www.sri.gob.ec/ruc")
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
        if (checksum(n.substring(0, 10), JURIDICAL_WEIGHTS) != 0) {
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
