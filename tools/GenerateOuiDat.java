import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/**
 * Generates the oui.dat registry consumed by the Mac class.
 *
 * <p>Source: the three MAC address block registries the IEEE Registration
 * Authority publishes as CSV. MA-L assigns the first 24 bits of an address,
 * MA-M the first 28 and MA-S the first 36, so a medium or small block is
 * always a subdivision of a large one and is written nested under it.</p>
 *
 * <p>A block held by the Registration Authority itself, or registered
 * privately, names no manufacturer and is left out: those are the parents of
 * the subdivided blocks, and the addresses under them are only assigned one
 * level down.</p>
 *
 * <p>Consecutive assignments to one organisation are joined into a range, so
 * that a company holding a run of blocks costs one entry rather than
 * hundreds.</p>
 *
 * <p>Usage:</p>
 * <pre>
 *   curl -L -o oui.csv    https://standards-oui.ieee.org/oui/oui.csv
 *   curl -L -o mam.csv    https://standards-oui.ieee.org/oui28/mam.csv
 *   curl -L -o oui36.csv  https://standards-oui.ieee.org/oui36/oui36.csv
 *   java tools/GenerateOuiDat.java oui.csv mam.csv oui36.csv \
 *       &gt; stdnum-international/src/main/resources/io/github/jefersonsantos06/stdnum/international/oui.dat
 * </pre>
 */
public final class GenerateOuiDat {

    /** Registrations under these names are the registry's own, not a maker's. */
    private static final List<String> UNASSIGNED =
            List.of("IEEE Registration Authority", "Private");

    private GenerateOuiDat() {
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 3) {
            System.err.println("usage: java GenerateOuiDat.java <oui.csv> <mam.csv> <oui36.csv>");
            System.exit(2);
        }

        // MA-L: the whole 24-bit block belongs to one organisation
        Map<String, List<String>> byOrganisation = new TreeMap<>();
        for (String[] row : rows(Path.of(args[0]))) {
            byOrganisation.computeIfAbsent(row[1], k -> new ArrayList<>()).add(row[0]);
        }

        // MA-M and MA-S: a subdivision of a 24-bit block, keyed by that block
        Map<String, Map<String, String>> nested = new TreeMap<>();
        for (String path : new String[] {args[1], args[2]}) {
            for (String[] row : rows(Path.of(path))) {
                nested.computeIfAbsent(row[0].substring(0, 6), k -> new TreeMap<>())
                        .put(row[0].substring(6), row[1]);
            }
        }

        if (byOrganisation.isEmpty() || nested.isEmpty()) {
            System.err.println("no assignments found: the CSV layout has changed");
            System.exit(1);
        }

        PrintStream out = new PrintStream(System.out, true, StandardCharsets.UTF_8);
        out.println("# IEEE MAC address block registry: the block each manufacturer holds.");
        out.println("# Generated from the registries published at");
        out.println("#   https://standards-oui.ieee.org/oui/oui.csv      (MA-L, 24 bits)");
        out.println("#   https://standards-oui.ieee.org/oui28/mam.csv    (MA-M, 28 bits)");
        out.println("#   https://standards-oui.ieee.org/oui36/oui36.csv  (MA-S, 36 bits)");

        // the whole blocks, ordered by the first one each organisation holds
        Map<String, String> whole = new TreeMap<>();
        for (Map.Entry<String, List<String>> entry : byOrganisation.entrySet()) {
            List<String> blocks = new ArrayList<>(entry.getValue());
            blocks.sort(null);
            whole.put(join(blocks), entry.getKey());
        }
        whole.forEach((blocks, organisation) ->
                out.println(blocks + " o=\"" + escape(organisation) + "\""));

        // and the blocks that are subdivided further
        nested.forEach((parent, children) -> {
            out.println(parent);
            children.forEach((suffix, organisation) ->
                    out.println(" " + suffix + " o=\"" + escape(organisation) + "\""));
        });
    }

    /** The assignment and organisation of every row that names a manufacturer. */
    private static List<String[]> rows(Path path) throws IOException {
        List<String[]> rows = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String header = reader.readLine();
            if (header == null || !header.startsWith("Registry,Assignment,Organization Name")) {
                System.err.println("unexpected CSV heading in " + path + ": " + header);
                System.exit(1);
            }
            String line;
            StringBuilder record = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                // a quoted field may hold a newline, so a record is only
                // complete once its quotes balance
                record.append(record.isEmpty() ? "" : "\n").append(line);
                if (quotes(record) % 2 != 0) {
                    continue;
                }
                List<String> fields = fields(record.toString());
                record.setLength(0);
                if (fields.size() < 3) {
                    continue;
                }
                String assignment = fields.get(1).strip().toUpperCase(Locale.ROOT);
                String organisation = fields.get(2).strip();
                if (assignment.isEmpty() || organisation.isEmpty()
                        || UNASSIGNED.contains(organisation)) {
                    continue;
                }
                rows.add(new String[] {assignment, organisation});
            }
        }
        return rows;
    }

    private static int quotes(CharSequence text) {
        int count = 0;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '"') {
                count++;
            }
        }
        return count;
    }

    /** One CSV record split on its commas, honouring quoted fields. */
    private static List<String> fields(String record) {
        List<String> fields = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < record.length(); i++) {
            char c = record.charAt(i);
            if (quoted) {
                if (c == '"') {
                    if (i + 1 < record.length() && record.charAt(i + 1) == '"') {
                        field.append('"');
                        i++;
                    } else {
                        quoted = false;
                    }
                } else {
                    field.append(c);
                }
            } else if (c == '"') {
                quoted = true;
            } else if (c == ',') {
                fields.add(field.toString());
                field.setLength(0);
            } else {
                field.append(c);
            }
        }
        fields.add(field.toString());
        return fields;
    }

    /** Consecutive blocks written as a range, the rest comma-separated. */
    private static String join(List<String> blocks) {
        int width = blocks.get(0).length();
        StringBuilder sb = new StringBuilder();
        long first = -1;
        long previous = -1;
        for (String block : blocks) {
            long value = Long.parseLong(block, 16);
            if (previous >= 0 && value == previous + 1) {
                if (previous > first) {
                    // extend the range already open
                    sb.setLength(sb.length() - width - 1);
                }
                sb.append('-').append(hex(value, width));
            } else {
                if (sb.length() > 0) {
                    sb.append(',');
                }
                sb.append(hex(value, width));
                first = value;
            }
            previous = value;
        }
        return sb.toString();
    }

    private static String hex(long value, int width) {
        return String.format("%0" + width + "X", value);
    }

    /** A value as the database writes it: a quote is escaped, not dropped. */
    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
