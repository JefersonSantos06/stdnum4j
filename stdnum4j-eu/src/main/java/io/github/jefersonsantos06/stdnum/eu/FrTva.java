package io.github.jefersonsantos06.stdnum.eu;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidComponentException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.Message;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;


/**
 * n° TVA (taxe sur la valeur ajoutée), the French VAT number: a SIREN
 * prefixed by a two-character key. Old-style keys are fully numeric
 * ({@code key == (siren + "12") mod 97}); new-style keys contain at least
 * one letter (the letters I and O are never used) and follow their own
 * modular rule. Numbers whose body starts with {@code 000} belong to Monaco
 * and skip the SIREN check. An optional {@code FR} prefix is accepted.
 */
public final class FrTva implements StdNum {

    public static final FrTva INSTANCE = new FrTva();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("fr.tva", "TVA")
                    .country("FR")
                    .title("Numéro d'identification à la taxe sur la valeur ajoutée")
                    .description("French VAT number: a two-character key (numeric or"
                            + " lettered, excluding I and O) followed by the SIREN.")
                    .tags(Tag.VAT)
                    .build();

    /** Valid key characters; the letters I and O are excluded. */
    private static final String ALPHABET = "0123456789ABCDEFGHJKLMNPQRSTUVWXYZ";

    private FrTva() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " -.", "FR");
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (n.length() != 11) {
            throw new InvalidLengthException();
        }
        if (ALPHABET.indexOf(n.charAt(0)) < 0 || ALPHABET.indexOf(n.charAt(1)) < 0
                || !Strings.isDigits(n.substring(2))) {
            throw new InvalidFormatException();
        }
        if (!n.startsWith("000", 2)) {
            // bodies starting with 000 are Monaco numbers, valid TVA but not SIREN
            FrSiren.INSTANCE.validate(n.substring(2));
        }
        if (Strings.isDigits(n)) {
            int key = Integer.parseInt(n.substring(0, 2));
            if (key != Long.parseLong(n.substring(2) + "12") % 97) {
                throw new InvalidChecksumException();
            }
        } else {
            int first = ALPHABET.indexOf(n.charAt(0));
            int second = ALPHABET.indexOf(n.charAt(1));
            int check = first < 10
                    ? first * 24 + second - 10
                    : first * 34 + second - 100;
            long body = Long.parseLong(n.substring(2));
            if ((body + 1 + check / 11) % 11 != check % 11) {
                throw new InvalidChecksumException();
            }
        }
        return n;
    }

    /** The SIREN embedded in this VAT number (absent for Monaco numbers). */
    public static String toSiren(String number) {
        String n = INSTANCE.validate(number);
        if (n.startsWith("000", 2)) {
            throw new InvalidComponentException(Message.of(FrTva.class, "tva.fr.monaco-siren",
                    "Monaco TVA numbers carry no SIREN."));
        }
        return n.substring(2);
    }
}
