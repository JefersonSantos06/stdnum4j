package io.github.jefersonsantos06.stdnum.br.ie;

import io.github.jefersonsantos06.stdnum.br.Uf;
import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.Message;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Mask;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Inscrição Estadual, the Brazilian state tax registration for ICMS
 * taxpayers. Every federative unit defines its own format and check digit
 * routine, so the number can only be validated <em>for a given state</em>.
 *
 * <p>There is one {@code InscricaoEstadual} instance per state, each a full
 * {@link StdNum} registered under the id {@code br.ie.<uf>}:</p>
 *
 * <pre>{@code
 * InscricaoEstadual.of(Uf.SP).isValid("110.042.490.114");
 * InscricaoEstadual.isValid("110042490114", Uf.SP);   // shortcut
 * StdNums.byId("br.ie.sp");                           // via the registry
 * }</pre>
 *
 * <p>Rules follow the official SINTEGRA "Roteiro de Crítica" pages; see
 * {@link UfRules} for the two states with incomplete official sources, and
 * {@link UfMasks} for how each state writes its number.</p>
 */
public final class InscricaoEstadual implements StdNum {

    private static final Map<Uf, InscricaoEstadual> BY_UF;

    static {
        Map<Uf, InscricaoEstadual> byUf = new EnumMap<>(Uf.class);
        for (Uf uf : Uf.values()) {
            byUf.put(uf, new InscricaoEstadual(uf));
        }
        BY_UF = Collections.unmodifiableMap(byUf);
    }

    private final Uf uf;
    private final UfRule rule;
    private final Descriptor descriptor;

    private InscricaoEstadual(Uf uf) {
        this.uf = uf;
        this.rule = UfRules.of(uf);
        this.descriptor = Descriptor
                .of("br.ie." + uf.name().toLowerCase(Locale.ROOT), "IE " + uf.name())
                .country("BR")
                .title("Inscrição Estadual - " + uf.displayName())
                .description("State tax registration (ICMS) of " + uf.displayName()
                        + ", validated with the state-specific SINTEGRA routine.")
                .tags(Tag.TAX, Tag.COMPANY)
                .references("https://www.sintegra.gov.br/insc_est.html")
                .build();
    }

    /** The validator for the given federative unit. */
    public static InscricaoEstadual of(Uf uf) {
        return BY_UF.get(uf);
    }

    /** All 27 state validators. */
    public static Collection<InscricaoEstadual> all() {
        return BY_UF.values();
    }

    /** Shortcut for {@code of(uf).validate(number)}. */
    public static String validate(String number, Uf uf) {
        return of(uf).validate(number);
    }

    /** Shortcut for {@code of(uf).isValid(number)}. */
    public static boolean isValid(String number, Uf uf) {
        return of(uf).isValid(number);
    }

    /** The federative unit this instance validates. */
    public Uf uf() {
        return uf;
    }

    @Override
    public Descriptor descriptor() {
        return descriptor;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " ./-").toUpperCase(Locale.ROOT);
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (n.isEmpty()) {
            throw new InvalidFormatException();
        }
        if (Strings.allSame(n, true)) {
            throw new InvalidFormatException(Message.of(InscricaoEstadual.class, "ie.repeated",
                    "A state registration consisting of a single repeated character is not valid."));
        }
        return rule.validate(n);
    }

    /**
     * The number written the way its own state writes it — {@code 20.089.514-1}
     * for Rio Grande do Norte, {@code 425/3755495} for Rio Grande do Sul.
     *
     * <p>Where a state has more than one length in circulation, each has its
     * own mask. Alagoas and Amapá write theirs as bare digits, so for them
     * this is the compact form; see {@link UfMasks}.</p>
     */
    @Override
    public String format(String number) {
        return UfMasks.apply(uf, validate(number));
    }

    /**
     * The masks this state writes its number with — one per length in
     * circulation. Empty for Alagoas and Amapá, which write bare digits.
     */
    @Override
    public List<Mask> masks() {
        return UfMasks.of(uf);
    }
}
