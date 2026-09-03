package io.github.jefersonsantos06.stdnum.all;

import io.github.jefersonsantos06.stdnum.StdNums;
import io.github.jefersonsantos06.stdnum.postal.PostalCode;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A country has one postal code type. The generic ones come from the data
 * file, the hand-written ones from the regional modules, and the two must
 * not meet: this is the tripwire that names the country to add to
 * {@code PostalCode.HAND_WRITTEN} when a new hand-written type appears.
 */
class PostalTypesTest {

    @Test
    void onePostalCodeTypePerCountry() {
        Map<String, List<String>> byCountry = new TreeMap<>();
        for (StdNum number : StdNums.byTag(Tag.POSTAL)) {
            String country = number.descriptor().country().orElse("(none)");
            byCountry.computeIfAbsent(country, k -> new java.util.ArrayList<>())
                    .add(number.descriptor().id());
        }
        byCountry.forEach((country, ids) ->
                assertEquals(1, ids.size(), country + " has more than one postal code type: "
                        + ids + " — add it to PostalCode.HAND_WRITTEN"));
    }

    @Test
    void aHandWrittenTypeKeepsTheGenericOneOut() {
        for (StdNum number : StdNums.byTag(Tag.POSTAL)) {
            if (!(number instanceof PostalCode)) {
                String country = number.descriptor().country().orElseThrow();
                assertTrue(PostalCode.of(country).isEmpty(),
                        country + " is validated by " + number.getClass().getSimpleName()
                                + "; add it to PostalCode.HAND_WRITTEN");
            }
        }
    }

    @Test
    void theGenericTypesAreRegistered() {
        assertEquals(PostalCode.all().size(),
                StdNums.byTag(Tag.POSTAL).stream().filter(n -> n instanceof PostalCode).count());
        assertTrue(StdNums.byId("br.postal_code").isPresent());
        assertTrue(StdNums.byId("nl.postcode").isPresent());
        assertTrue(StdNums.byId("nl.postal_code").isEmpty());
    }
}
