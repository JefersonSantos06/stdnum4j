package io.github.jefersonsantos06.stdnum.numdb;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NumDbTest {

    private final NumDb db = NumDb.load(NumDbTest.class, "/numdb/test.dat");

    @Test
    void splitsThroughNestedRanges() {
        assertEquals(List.of("0", "100", "6"), db.split("01006"));
        assertEquals(List.of("9", "85"), db.split("985"));
    }

    @Test
    void unknownTailBecomesOnePart() {
        assertEquals(List.of("9", "40"), db.split("940"));
        assertEquals(List.of("777"), db.split("777"));
    }

    @Test
    void overlappingRangesAtSameLengthMergeProperties() {
        NumDb.Entry entry = db.info("02506").get(1);
        assertEquals("250", entry.part());
        assertEquals(Map.of("kind", "a", "extra", "b"), entry.properties());
    }

    @Test
    void shortestMatchWins() {
        // both 50-59 (length 2) and 5 (length 1) match; the shortest is used
        List<NumDb.Entry> info = db.info("5012");
        assertEquals("5", info.get(0).part());
        assertEquals(Map.of("narrow", "2"), info.get(0).properties());
    }

    @Test
    void deepNestingIsFollowed() {
        assertEquals(List.of("2", "333", "22"), db.split("233322"));
        assertEquals(Map.of("leaf", "ok"), db.info("233322").get(2).properties());
    }

    @Test
    void emptyNumberYieldsNoEntries() {
        assertTrue(db.info("").isEmpty());
    }

    @Test
    void resourcesAreCached() {
        assertSame(db, NumDb.load(NumDbTest.class, "/numdb/test.dat"));
    }

    @Test
    void missingResourceFailsFast() {
        assertThrows(IllegalArgumentException.class,
                () -> NumDb.load(NumDbTest.class, "/numdb/nope.dat"));
    }

    @Test
    void entriesListTheTopLevelAsWritten() {
        List<NumDb.Entry> entries = db.entries();
        assertEquals(List.of("0-2", "9", "50-59", "5"),
                entries.stream().map(NumDb.Entry::part).toList());
        assertEquals(Map.of("zone", "low"), entries.get(0).properties());
        assertEquals(Map.of("narrow", "2"), entries.get(3).properties());
    }
}
