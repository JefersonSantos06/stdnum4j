package io.github.jefersonsantos06.stdnum.br.ie;

import io.github.jefersonsantos06.stdnum.br.Uf;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * How each federative unit writes its state registration.
 *
 * <p>A mask is a template in which {@code '#'} takes the next character of the
 * compact number and every other character is a literal, so
 * {@code "##.###.###-#"} turns {@code 200895141} into {@code 20.089.514-1}.
 * The literals are always among {@code . - /}, which
 * {@link InscricaoEstadual#compact(String)} strips again — a formatted number
 * validates and round-trips like any other.</p>
 *
 * <p>A state with more than one length has one mask per length, chosen by
 * counting the {@code '#'}: Pernambuco writes nine digits as
 * {@code 1908093-02} and the legacy fourteen as {@code 18.1.001.0000004-9}.
 * Alagoas and Amapá are absent on purpose — SINTEGRA documents their numbers
 * as nine bare digits, and a number with no mask is its own presentation.</p>
 *
 * <p>The masks come from the "Roteiro de Crítica" page of each state, whose
 * worked example doubles as the presentation. The Federal District is the one
 * state with no page; it follows the market convention, as its check digit
 * rule does.</p>
 */
final class UfMasks {

    private UfMasks() {
    }

    private static final Map<Uf, List<String>> MASKS = masks();

    private static Map<Uf, List<String>> masks() {
        Map<Uf, List<String>> m = new EnumMap<>(Uf.class);
        m.put(Uf.AC, List.of("##.###.###/###-##"));
        m.put(Uf.AM, List.of("##.###.###-#"));
        m.put(Uf.BA, List.of("######-##", "#######-##"));
        m.put(Uf.CE, List.of("########-#"));
        m.put(Uf.DF, List.of("###########-##"));
        m.put(Uf.ES, List.of("########-#"));
        m.put(Uf.GO, List.of("##.###.###-#"));
        m.put(Uf.MA, List.of("########-#"));
        m.put(Uf.MT, List.of("##########-#"));
        m.put(Uf.MS, List.of("########-#"));
        m.put(Uf.MG, List.of("###.###.###/####"));
        m.put(Uf.PA, List.of("##-######-#"));
        m.put(Uf.PB, List.of("########-#"));
        m.put(Uf.PR, List.of("###.#####-##"));
        m.put(Uf.PE, List.of("#######-##", "##.#.###.#######-#"));
        m.put(Uf.PI, List.of("########-#"));
        m.put(Uf.RJ, List.of("##.###.##-#"));
        m.put(Uf.RN, List.of("##.###.###-#", "##.#.###.###-#"));
        m.put(Uf.RS, List.of("###/#######"));
        m.put(Uf.RO, List.of("###.#####-#", "#############-#"));
        m.put(Uf.RR, List.of("########-#"));
        m.put(Uf.SC, List.of("###.###.###"));
        // the rural producer's number opens with a P, which the first # takes
        m.put(Uf.SP, List.of("###.###.###.###", "#-########.#/###"));
        m.put(Uf.SE, List.of("########-#"));
        m.put(Uf.TO, List.of("########-#", "##########-#"));
        return m;
    }

    /**
     * The compact number written the way its state writes it, or unchanged
     * when the state has no mask for a number of that length.
     */
    static String apply(Uf uf, String compact) {
        for (String mask : MASKS.getOrDefault(uf, List.of())) {
            if (slots(mask) == compact.length()) {
                return fill(mask, compact);
            }
        }
        return compact;
    }

    private static int slots(String mask) {
        int slots = 0;
        for (int i = 0; i < mask.length(); i++) {
            if (mask.charAt(i) == '#') {
                slots++;
            }
        }
        return slots;
    }

    private static String fill(String mask, String compact) {
        StringBuilder out = new StringBuilder(mask.length());
        int at = 0;
        for (int i = 0; i < mask.length(); i++) {
            char c = mask.charAt(i);
            out.append(c == '#' ? compact.charAt(at++) : c);
        }
        return out.toString();
    }
}
