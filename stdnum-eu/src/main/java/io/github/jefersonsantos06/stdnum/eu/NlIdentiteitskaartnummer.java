package io.github.jefersonsantos06.stdnum.eu;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidComponentException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.Message;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * The number of a Dutch identity card or passport: two letters, six
 * alphanumerics and a final digit. The letter O never appears, to keep it
 * from being read as a zero.
 */
public final class NlIdentiteitskaartnummer implements StdNum {

    public static final NlIdentiteitskaartnummer INSTANCE = new NlIdentiteitskaartnummer();

    private static final Pattern PATTERN = Pattern.compile("[A-Z]{2}[0-9A-Z]{6}[0-9]");

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("nl.identiteitskaartnummer", "Documentnummer")
                    .country("NL")
                    .title("Nederlands identiteitskaart- of paspoortnummer")
                    .description("Dutch identity card or passport number: 2 letters, 6"
                            + " alphanumerics and a digit, never containing the letter O.")
                    .tags(Tag.PERSON)
                    .references("https://www.rijksoverheid.nl/onderwerpen/paspoort-en-identiteitskaart")
                    .build();

    private NlIdentiteitskaartnummer() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " ").toUpperCase(Locale.ROOT);
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (n.length() != 9) {
            throw new InvalidLengthException();
        }
        if (!PATTERN.matcher(n).matches()) {
            throw new InvalidFormatException();
        }
        if (n.indexOf('O') >= 0) {
            throw new InvalidComponentException(Message.of(NlIdentiteitskaartnummer.class, "identiteitskaartnummer.letter-o",
                    "The letter O is not allowed."));
        }
        return n;
    }
}
