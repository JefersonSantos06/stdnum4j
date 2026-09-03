package io.github.jefersonsantos06.stdnum.br.ie;

import io.github.jefersonsantos06.stdnum.br.Uf;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

/**
 * One worked example per federative unit, taken from the official SINTEGRA
 * "Roteiro de Crítica" pages where the page provides one, and derived by hand
 * from the published routine otherwise (AM, DF, MS, RJ and the extra BA
 * mod 11 case). Each valid example is also mutated in its final check digit
 * and must then be rejected.
 */
class SintegraExamplesTest {

    private static final Map<Uf, List<String>> VALID = Map.ofEntries(
            Map.entry(Uf.AC, List.of("0100482300112", "01.115.999/927-37", "01.449.112/091-56", "0168205482665", "0168679304857")),
            Map.entry(Uf.AL, List.of("240000048", "248523589", "248311239")),
            Map.entry(Uf.AP, List.of("030123459", "036122939", "037812203", "038351943", "038362147")),
            Map.entry(Uf.AM, List.of("041173988", "65.015.106-2", "69.084.610-0", "513307273", "976311518")),
            Map.entry(Uf.BA, List.of("12345663", "100000306", "160000005", "7062180-60", "6336966-47", "904299287", "025946301")),
            Map.entry(Uf.CE, List.of("060000015", "01734268-6", "59822022-4", "142477362", "781360056")),
            Map.entry(Uf.DF, List.of("0730000100109", "07536791001-43", "07107410001-66", "0723142200194", "0780056600138")),
            Map.entry(Uf.ES, List.of("999999990", "34641712-0", "67997828-3", "812258827", "088419770")),
            // GO opens with 10, 11 or 20-29; a number opening with 15 has the
            // check digit of nothing SINTEGRA recognises, however well it sums
            Map.entry(Uf.GO, List.of("109876547", "10.181.638-3", "200662392")),
            Map.entry(Uf.MA, List.of("120000385", "12351999-3", "12797058-4", "121823067", "125231610")),
            Map.entry(Uf.MG, List.of("0623079040081", "537.671.922/8316", "361.293.345/0160", "8160960163855", "6209835215359")),
            Map.entry(Uf.MS, List.of("280000006", "500000000", "28611358-9", "28360212-0", "287152960", "286265621")),
            Map.entry(Uf.MT, List.of("00130000019", "4171981010-5", "3713793428-8", "34806232700", "39259348860")),
            Map.entry(Uf.PA, List.of("159999995", "750000023", "15-489565-2", "15-294079-0", "150715137", "153085380")),
            Map.entry(Uf.PB, List.of("060000015", "79106576-6", "54252693-0", "125501099", "135734746")),
            Map.entry(Uf.PE, List.of("032141840", "18100100000049", "1908093-02", "9125205-90", "699594804", "192651234")),
            Map.entry(Uf.PI, List.of("012345679", "15799149-0", "05957083-0", "147163005", "655933280")),
            Map.entry(Uf.PR, List.of("1234567850", "321.51544-40", "065.92036-00", "4365157745", "2443109075")),
            Map.entry(Uf.RJ, List.of("12345674", "00.013.07-2", "74.214.62-2", "76861285", "08735107")),
            Map.entry(Uf.RN, List.of("200400401", "2000400400", "20.089.514-1", "20.132.614-0", "200657968", "201870428")),
            Map.entry(Uf.RO, List.of("101625213", "00000000625213", "2913625811294-3", "3474874966751-1", "74965553754641", "45682737939549")),
            Map.entry(Uf.RR, List.of("240061536", "240066281", "240017556", "240034290", "240013603", "240082668", "240073562", "240054674", "240041455", "240013407", "24092744-5", "24260172-3", "247734463", "240226848")),
            Map.entry(Uf.RS, List.of("2243658792", "425/3755495", "027/5936333", "7360570604", "5048808297")),
            Map.entry(Uf.SC, List.of("251040852", "905.173.724", "285.457.527", "099743469", "735858969")),
            Map.entry(Uf.SE, List.of("271234563", "24311651-9", "41110358-0", "389508403", "629461961")),
            Map.entry(Uf.SP, List.of("110042490114", "P011004243002", "391.746.397.128", "887.877.680.358", "653726943049", "786567065593")),
            Map.entry(Uf.TO, List.of("29010227836", "7403166987-0", "74166987-0", "2803786981-4", "28786981-4", "68032534218", "682534218", "58031203950", "581203950"))
    );

    @Test
    void allTwentySevenStatesAreCovered() {
        assertEquals(Uf.values().length, VALID.size());
    }

    @TestFactory
    Stream<DynamicTest> officialExamplesValidate() {
        return VALID.entrySet().stream()
                .flatMap(entry -> entry.getValue().stream()
                        .map(sample -> dynamicTest(entry.getKey() + ": " + sample, () -> {
                            InscricaoEstadual ie = InscricaoEstadual.of(entry.getKey());
                            String compact = ie.compact(sample);
                            assertEquals(compact, ie.validate(sample));
                            assertTrue(InscricaoEstadual.isValid(sample, entry.getKey()));
                            if (!sample.equals(compact)) {
                                // a sample written with separators is the state's
                                // own presentation, so format must give it back
                                assertEquals(sample, ie.format(sample));
                            }
                        })));
    }

    @TestFactory
    Stream<DynamicTest> mutatedCheckDigitsAreRejected() {
        return VALID.entrySet().stream()
                .flatMap(entry -> entry.getValue().stream()
                        // rural SP numbers end in a filler, not the check digit
                        .filter(sample -> !sample.startsWith("P"))
                        .map(sample -> dynamicTest(entry.getKey() + ": ~" + sample, () -> {
                            char last = sample.charAt(sample.length() - 1);
                            char mutated = (char) ('0' + (last - '0' + 1) % 10);
                            String bad = sample.substring(0, sample.length() - 1) + mutated;
                            assertFalse(InscricaoEstadual.isValid(bad, entry.getKey()),
                                    "mutation " + bad + " must be invalid");
                        })));
    }

    @Test
    void aNumberValidInOneStateIsNotAssumedValidInAnother() {
        // the SP example fails everywhere else at length or digit checks
        for (Uf uf : Uf.values()) {
            if (uf != Uf.SP) {
                assertFalse(InscricaoEstadual.isValid("110042490114", uf),
                        "SP number leaked into " + uf);
            }
        }
    }
}
