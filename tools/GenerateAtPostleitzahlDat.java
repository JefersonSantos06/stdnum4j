import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Generates the at-postleitzahl.dat registry consumed by the AtPostleitzahl
 * class.
 *
 * <p>Source: the postcode table the Austrian regulator RTR publishes as JSON.
 * Only the codes marked addressable are emitted; the rest are post office box
 * and internal codes that no address carries.</p>
 *
 * <p>The file is small and flat, so it is read with a scanner over its
 * records rather than by pulling in a JSON library: this repository's
 * generators have no dependencies.</p>
 */
public final class GenerateAtPostleitzahlDat implements Source {

    @Override
    public String id() {
        return "at-postleitzahl";
    }

    @Override
    public String title() {
        return "Regenerate at-postleitzahl.dat, the Austrian postcodes";
    }

    @Override
    public String output() {
        return "stdnum4j-eu/src/main/resources/io/github/jefersonsantos06/stdnum/eu/at-postleitzahl.dat";
    }

    @Override
    public List<Download> downloads(Map<String, String> seeds) {
        return List.of(new Download(
                "https://data.rtr.at/api/v1/tables/plz.json",
                "plz.json"));
    }

    /** The nine Bundesländer, under the letters the table abbreviates them to. */
    private static final Map<String, String> REGIONS = new LinkedHashMap<>();

    static {
        REGIONS.put("B", "Burgenland");
        REGIONS.put("K", "Kärnten");
        REGIONS.put("N", "Niederösterreich");
        REGIONS.put("O", "Oberösterreich");
        REGIONS.put("Sa", "Salzburg");
        REGIONS.put("St", "Steiermark");
        REGIONS.put("T", "Tirol");
        REGIONS.put("V", "Vorarlberg");
        REGIONS.put("W", "Wien");
    }

    /** One record of the data array, taken as far as its closing brace. */
    private static final Pattern RECORD = Pattern.compile("\\{[^{}]*}");
    private static final Pattern VERSION_ID = Pattern.compile("\"id\"\\s*:\\s*([0-9]+)");
    private static final Pattern PUBLISHED = Pattern.compile("\"published\"\\s*:\\s*\"([^\"]*)\"");

    @Override
    public void generate(Run run, PrintStream out) throws Exception {
        String json = Files.readString(run.file("plz.json"), StandardCharsets.UTF_8);

        int dataAt = json.indexOf("\"data\"");
        if (dataAt < 0) {
            throw new IllegalStateException("no data array: the table layout has changed");
        }
        // the version object sits after the data array, so look for it there
        int versionAt = json.indexOf("\"version\"");
        String header = versionAt < 0 ? "" : json.substring(versionAt);
        String version = group(VERSION_ID, header);
        String published = group(PUBLISHED, header);
        if (version == null || published == null) {
            throw new IllegalStateException("no version stamp: the table layout has changed");
        }

        TreeSet<String> entries = new TreeSet<>();
        Matcher records = RECORD.matcher(json).region(dataAt, json.length());
        while (records.find()) {
            String entry = parseRecord(records.group());
            if (entry != null) {
                entries.add(entry);
            }
        }
        if (entries.isEmpty()) {
            throw new IllegalStateException("no addressable postcodes found: the table layout has changed");
        }

        out.println("# Austrian postcodes: the addressable ones, with the place and the");
        out.println("# Bundesland each belongs to.");
        out.println("# Generated from https://data.rtr.at/api/v1/tables/plz.json");
        out.println("# version " + version + " published " + published);
        entries.forEach(out::println);
    }

    /** One postcode record, or null when the code is not an addressable one. */
    private static String parseRecord(String record) {
        if (!"Ja".equals(string(record, "adressierbar"))) {
            return null;
        }
        String code = group(Pattern.compile("\"plz\"\\s*:\\s*\"?([0-9]+)\"?"), record);
        String place = string(record, "ort");
        String region = REGIONS.get(string(record, "bundesland"));
        if (code == null || place == null || region == null) {
            return null;
        }
        return code + " location=\"" + place.replace("\"", "") + "\" region=\"" + region + "\"";
    }

    private static String string(String record, String field) {
        return group(Pattern.compile("\"" + field + "\"\\s*:\\s*\"([^\"]*)\""), record);
    }

    private static String group(Pattern pattern, String text) {
        Matcher m = pattern.matcher(text);
        return m.find() ? m.group(1) : null;
    }
}
