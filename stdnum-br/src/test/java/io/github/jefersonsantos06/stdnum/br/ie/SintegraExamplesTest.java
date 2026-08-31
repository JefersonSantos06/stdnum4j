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
            Map.entry(Uf.AC, List.of("0100482300112")),
            Map.entry(Uf.AL, List.of("240000048")),
            Map.entry(Uf.AP, List.of("030123459")),
            Map.entry(Uf.AM, List.of("041173988")),
            Map.entry(Uf.BA, List.of("12345663", "100000306", "160000005")),
            Map.entry(Uf.CE, List.of("060000015")),
            Map.entry(Uf.DF, List.of("0730000100109")),
            Map.entry(Uf.ES, List.of("999999990")),
            Map.entry(Uf.GO, List.of("109876547")),
            Map.entry(Uf.MA, List.of("120000385")),
            Map.entry(Uf.MG, List.of("0623079040081")),
            Map.entry(Uf.MS, List.of("280000006", "500000000")),
            Map.entry(Uf.MT, List.of("00130000019")),
            Map.entry(Uf.PA, List.of("159999995", "750000023")),
            Map.entry(Uf.PB, List.of("060000015")),
            Map.entry(Uf.PE, List.of("032141840", "18100100000049")),
            Map.entry(Uf.PI, List.of("012345679")),
            Map.entry(Uf.PR, List.of("1234567850")),
            Map.entry(Uf.RJ, List.of("12345674")),
            Map.entry(Uf.RN, List.of("200400401", "2000400400")),
            Map.entry(Uf.RO, List.of("101625213", "00000000625213")),
            Map.entry(Uf.RR, List.of("240061536", "240066281", "240017556",
                    "240034290", "240013603", "240082668", "240073562",
                    "240054674", "240041455", "240013407")),
            Map.entry(Uf.RS, List.of("2243658792")),
            Map.entry(Uf.SC, List.of("251040852")),
            Map.entry(Uf.SE, List.of("271234563")),
            Map.entry(Uf.SP, List.of("110042490114", "P011004243002")),
            Map.entry(Uf.TO, List.of("29010227836")));

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
                            assertEquals(sample, ie.validate(sample));
                            assertTrue(InscricaoEstadual.isValid(sample, entry.getKey()));
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
