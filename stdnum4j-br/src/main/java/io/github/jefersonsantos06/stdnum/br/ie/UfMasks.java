package io.github.jefersonsantos06.stdnum.br.ie;

import io.github.jefersonsantos06.stdnum.br.Uf;
import io.github.jefersonsantos06.stdnum.text.Mask;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * How each federative unit writes its state registration.
 *
 * <p>A {@link Mask} is a template in which {@code '#'} takes the next
 * character of the compact number and every other character is a literal, so
 * {@code "##.###.###-#"} turns {@code 200895141} into {@code 20.089.514-1}.
 * The literals are always among {@code . - /}, which
 * {@link InscricaoEstadual#compact(String)} strips again — a formatted number
 * validates and round-trips like any other.</p>
 *
 * <p>A state with more than one length has one mask per length: Pernambuco
 * writes nine digits as {@code 1908093-02} and the legacy fourteen as
 * {@code 18.1.001.0000004-9}. Alagoas and Amapá are absent on purpose —
 * SINTEGRA documents their numbers as nine bare digits, and a number with no
 * mask is its own presentation.</p>
 *
 * <p>The masks come from the "Roteiro de Crítica" page of each state, whose
 * worked example doubles as the presentation. The Federal District is the one
 * state with no page; it follows the market convention, as its check digit
 * rule does.</p>
 */
final class UfMasks {

    private UfMasks() {
    }

    private static final Map<Uf, List<Mask>> MASKS = masks();

    private static Map<Uf, List<Mask>> masks() {
        Map<Uf, List<Mask>> m = new EnumMap<>(Uf.class);
        put(m, Uf.AC, "##.###.###/###-##");
        put(m, Uf.AM, "##.###.###-#");
        put(m, Uf.BA, "######-##", "#######-##");
        put(m, Uf.CE, "########-#");
        put(m, Uf.DF, "###########-##");
        put(m, Uf.ES, "########-#");
        put(m, Uf.GO, "##.###.###-#");
        put(m, Uf.MA, "########-#");
        put(m, Uf.MT, "##########-#");
        put(m, Uf.MS, "########-#");
        put(m, Uf.MG, "###.###.###/####");
        put(m, Uf.PA, "##-######-#");
        put(m, Uf.PB, "########-#");
        put(m, Uf.PR, "###.#####-##");
        put(m, Uf.PE, "#######-##", "##.#.###.#######-#");
        put(m, Uf.PI, "########-#");
        put(m, Uf.RJ, "##.###.##-#");
        put(m, Uf.RN, "##.###.###-#", "##.#.###.###-#");
        put(m, Uf.RS, "###/#######");
        put(m, Uf.RO, "###.#####-#", "#############-#");
        put(m, Uf.RR, "########-#");
        put(m, Uf.SC, "###.###.###");
        // the rural producer's number opens with a P, which the first # takes
        put(m, Uf.SP, "###.###.###.###", "#-########.#/###");
        put(m, Uf.SE, "########-#");
        put(m, Uf.TO, "########-#", "##########-#");
        return m;
    }

    private static void put(Map<Uf, List<Mask>> m, Uf uf, String... templates) {
        m.put(uf, List.of(templates).stream().map(Mask::of).toList());
    }

    /**
     * The compact number written the way its state writes it, or unchanged
     * when the state has no mask for a number of that length.
     */
    static String apply(Uf uf, String compact) {
        return Mask.apply(MASKS.getOrDefault(uf, List.of()), compact);
    }
}
