package io.github.jefersonsantos06.stdnum.postal;

import io.github.jefersonsantos06.stdnum.numdb.NumDb;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

/**
 * The examples the metadata publishes for every region, run through every
 * type: each must validate, and each must be what {@code format} writes.
 * Since the masks were derived from these very examples, this is the proof
 * that the derivation round-trips — and the place a regeneration that changes
 * a shape shows up.
 */
class PostalCodeExamplesTest {

    /** What the generator wrote, less the countries with a hand-written type. */
    private static final Set<String> HAND_WRITTEN = Set.of("AT", "ES", "NL", "SE");

    @Test
    void everyRegionTheDataDescribesIsHere() {
        Set<String> inData = NumDb.load(PostalCode.class, "postal-codes.dat").entries().stream()
                .map(NumDb.Entry::part)
                .collect(Collectors.toSet());
        assertEquals(182, inData.size(), "regions with a postal code in the data file");
        assertTrue(inData.containsAll(HAND_WRITTEN),
                "a hand-written country is missing from the data: the exclusion is stale");
        assertEquals(inData.size() - HAND_WRITTEN.size(), PostalCode.all().size());
    }

    @TestFactory
    Stream<DynamicTest> examplesValidateAndFormatBackToThemselves() {
        return PostalCode.all().stream()
                .flatMap(code -> code.examples().stream()
                        .map(example -> dynamicTest(code.country() + ": " + example, () -> {
                            String compact = code.validate(example);
                            assertEquals(compact, code.compact(example));
                            assertEquals(compact, code.validate(compact), "idempotent");
                            assertEquals(example, code.format(example));
                            assertEquals(example, code.format(compact));
                        })));
    }

    @TestFactory
    Stream<DynamicTest> aPrefixIsStrippedOnlyWhenThePatternDoesNotWantIt() {
        Map<String, List<String>> cases = Map.ofEntries(
                Map.entry("LU", List.of("L-4750", "4750")),
                Map.entry("AZ", List.of("AZ 1000", "1000")),
                Map.entry("PR", List.of("PR 00930", "00930")),
                Map.entry("AI", List.of("AI-2640", "2640")),
                Map.entry("OM", List.of("PC 133", "133")),
                Map.entry("CH", List.of("CH-8001", "8001")),
                Map.entry("GB", List.of("GB-M34 4AB", "M344AB")),
                // the pattern itself opens with these letters, so they stay
                Map.entry("MT", List.of("MTP 1234", "MTP1234")),
                Map.entry("LV", List.of("LV-1073", "LV1073")),
                Map.entry("KY", List.of("KY1-1100", "KY11100")),
                Map.entry("AD", List.of("AD100", "AD100")));
        return cases.entrySet().stream().map(entry ->
                dynamicTest(entry.getKey() + ": " + entry.getValue().get(0), () -> {
                    PostalCode code = PostalCode.of(entry.getKey()).orElseThrow();
                    assertEquals(entry.getValue().get(1), code.validate(entry.getValue().get(0)));
                }));
    }

    @Test
    void separatorsAndCaseDoNotMatter() {
        assertEquals("1540023", PostalCode.of("JP").orElseThrow().validate("154 0023"));
        assertEquals("SW1A1AA", PostalCode.of("GB").orElseThrow().validate("sw1a1aa"));
        assertEquals("H2B2Y5", PostalCode.of("CA").orElseThrow().validate("h2b 2y5"));
    }

    @Test
    void aShapeTheExamplesDoNotShowIsWrittenCompact() {
        assertEquals("LIMA1", PostalCode.of("PE").orElseThrow().format("LIMA 1"));
        assertEquals("NXR123", PostalCode.of("MT").orElseThrow().format("NXR 123"));
        assertEquals("BFPO1234", PostalCode.of("GB").orElseThrow().format("BFPO 1234"));
    }

    @Test
    void oneExampleStandsForTheWholeFamily() {
        assertEquals("D02 X285", PostalCode.of("IE").orElseThrow().format("d02x285"));
        assertEquals("E1 4AB", PostalCode.of("GB").orElseThrow().format("e14ab"));
    }

    @Test
    void countriesWithoutAGenericTypeAreEmpty() {
        for (String country : HAND_WRITTEN) {
            assertTrue(PostalCode.of(country).isEmpty(), country);
            assertTrue(PostalCode.of(country.toLowerCase()).isEmpty(), country);
        }
        assertTrue(PostalCode.of("XX").isEmpty());
        assertTrue(PostalCode.of("HK").isEmpty(), "Hong Kong has no postal codes");
        assertTrue(PostalCode.of(null).isEmpty());
        assertTrue(PostalCode.of("").isEmpty());
    }

    @Test
    void descriptorsNameTheCountryAndTheKindOfCode() {
        assertEquals("br.postal_code", PostalCode.of("BR").orElseThrow().descriptor().id());
        assertEquals("Brazil postal code", PostalCode.of("BR").orElseThrow().descriptor().title());
        assertEquals("ZIP code", PostalCode.of("US").orElseThrow().descriptor().shortName());
        assertEquals("Ireland Eircode", PostalCode.of("IE").orElseThrow().descriptor().title());
        assertEquals("PIN code", PostalCode.of("IN").orElseThrow().descriptor().shortName());
    }
}
