package io.github.jefersonsantos06.stdnum.international;

import io.github.jefersonsantos06.stdnum.StdNums;
import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidComponentException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.Message;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * The excise number of an operator authorised to move alcohol, tobacco or
 * energy products under duty suspension: a member state and eleven more
 * characters.
 *
 * <p>What those eleven characters look like is left to each member state,
 * so this checks the length and the country and then hands the number to
 * that country's excise type if one is registered.</p>
 */
public final class EuExcise implements StdNum {

    public static final EuExcise INSTANCE = new EuExcise();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("eu.excise", "Excise number")
                    .title("European Union excise number")
                    .description("Excise number of an authorised operator: a member state and"
                            + " 11 characters the state decides the shape of.")
                    .tags(Tag.COMPANY, Tag.TAX)
                    .references("https://ec.europa.eu/taxation_customs/dds2/seed/")
                    .build();

    private EuExcise() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    /**
     * The type that refines an excise number for a country, if one is
     * registered: the {@code <cc>.excise} id, or failing that the country's
     * single {@link Tag#EXCISE} type, since states name theirs differently
     * (France calls its own the numero d'accise).
     */
    private static Optional<StdNum> moduleFor(String countryCode) {
        String cc = countryCode.toLowerCase(Locale.ROOT);
        Optional<StdNum> byId = StdNums.byId(cc + ".excise");
        if (byId.isPresent()) {
            return byId;
        }
        List<StdNum> tagged = StdNums.byCountry(cc).stream()
                .filter(n -> n.descriptor().tags().contains(Tag.EXCISE))
                .toList();
        return tagged.size() == 1 ? Optional.of(tagged.get(0)) : Optional.empty();
    }

    @Override
    public String compact(String number) {
        String n = Strings.compact(number, " ").toUpperCase(Locale.ROOT);
        // the serial is quoted without its leading zeros as often as with them
        if (n.length() < 13 && n.length() > 2) {
            return n.substring(0, 2) + "0".repeat(13 - n.length()) + n.substring(2);
        }
        return n;
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (n.length() != 13) {
            throw new InvalidLengthException();
        }
        String cc = n.substring(0, 2);
        if (!EuVat.MEMBER_STATES.contains(cc)) {
            throw new InvalidComponentException(Message.of(EuExcise.class, "eu.member-state",
                    "{0} is not an EU member state.", cc));
        }
        moduleFor(cc).ifPresent(m -> m.validate(n));
        return n;
    }
}
