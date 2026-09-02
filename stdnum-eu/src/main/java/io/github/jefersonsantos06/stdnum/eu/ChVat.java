package io.github.jefersonsantos06.stdnum.eu;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidComponentException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.Message;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;

/**
 * The Swiss VAT number: the business identifier ({@link ChUid}) followed by
 * the tax suffix in one of the national languages — {@code MWST},
 * {@code TVA}, {@code IVA} or {@code TPV}.
 */
public final class ChVat implements StdNum {

    public static final ChVat INSTANCE = new ChVat();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("ch.vat", "MWST/TVA/IVA")
                    .country("CH")
                    .title("Mehrwertsteuernummer")
                    .description("Swiss VAT number: the UID followed by MWST, TVA, IVA"
                            + " or TPV.")
                    .tags(Tag.VAT)
                    .build();

    private ChVat() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return ChUid.INSTANCE.compact(number);
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (n.length() != 15 && n.length() != 16) {
            throw new InvalidLengthException();
        }
        ChUid.INSTANCE.validate(n.substring(0, 12));
        String suffix = n.substring(12);
        if (!suffix.equals("MWST") && !suffix.equals("TVA")
                && !suffix.equals("IVA") && !suffix.equals("TPV")) {
            throw new InvalidComponentException(Message.of(ChVat.class, "vat.ch.suffix",
                    "A Swiss VAT number ends with MWST, TVA, IVA or TPV."));
        }
        return n;
    }

    @Override
    public String format(String number) {
        String n = validate(number);
        return ChUid.INSTANCE.format(n.substring(0, 12)) + " " + n.substring(12);
    }
}
