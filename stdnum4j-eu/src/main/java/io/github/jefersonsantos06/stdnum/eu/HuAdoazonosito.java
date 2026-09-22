package io.github.jefersonsantos06.stdnum.eu;

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

/**
 * Adóazonosító jel, the Hungarian tax identification number of a private
 * individual: ten digits — an 8, the date of birth as the number of days
 * since 1 January 1867, a three-digit serial and a check digit.
 *
 * <p>The check digit is the first nine digits weighted 1 to 9, modulo 11. A
 * serial whose remainder would be 10 is never issued, so such a number has no
 * check digit that could close it.</p>
 */
public final class HuAdoazonosito implements StdNum {

    public static final HuAdoazonosito INSTANCE = new HuAdoazonosito();

    private static final LocalDate EPOCH = LocalDate.of(1867, 1, 1);

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("hu.adoazonosito", "Adóazonosító jel")
                    .country("HU")
                    .title("Hungarian personal tax identification number")
                    .description("Hungarian tax number of a private individual: 10 digits"
                            + " starting with 8, encoding the date of birth, with a weighted"
                            + " mod 11 check digit.")
                    .tags(Tag.PERSON, Tag.TAX)
                    .references(
                            "https://www.oecd.org/tax/automatic-exchange/crs-implementation-and-assistance/tax-identification-numbers/Hungary-TIN.pdf",
                            "https://hu.wikipedia.org/wiki/Ad%C3%B3azonos%C3%ADt%C3%B3_jel")
                    .build();

    private HuAdoazonosito() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " -");
    }

    /** The birth date encoded in the number. */
    public static LocalDate getBirthDate(String number) {
        String n = Strings.requireDigits(INSTANCE.compact(number), 10);
        return EPOCH.plusDays(Integer.parseInt(n.substring(1, 6)));
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (!Strings.isDigits(n)) {
            throw new InvalidFormatException();
        }
        if (n.length() != 10) {
            throw new InvalidLengthException();
        }
        if (n.charAt(0) != '8') {
            throw new InvalidComponentException(Message.of(HuAdoazonosito.class,
                    "adoazonosito.prefix", "A personal tax number starts with 8."));
        }
        int sum = 0;
        for (int i = 0; i < 9; i++) {
            sum += (i + 1) * (n.charAt(i) - '0');
        }
        int check = sum % 11;
        if (check == 10) {
            throw new InvalidChecksumException(Reasons.noCheckDigit());
        }
        if (n.charAt(9) - '0' != check) {
            throw new InvalidChecksumException();
        }
        if (getBirthDate(n).isAfter(LocalDate.now())) {
            throw new InvalidComponentException(Message.of(HuAdoazonosito.class,
                    "adoazonosito.birth-date.future",
                    "The birth date is valid, but this person has not been born yet."));
        }
        return n;
    }
}
