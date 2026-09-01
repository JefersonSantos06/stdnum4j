import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Reads the cells out of a spreadsheet, for the generators whose upstream
 * publishes its register as one.
 *
 * <p>An xlsx file is a zip of XML parts, both of which the JDK can read, so
 * this needs no dependency — which is what the generators here are held to.
 * Only what the generators need is implemented: the used range of a sheet,
 * as rows of strings, with shared strings resolved and empty cells kept in
 * place so a column index means the same thing on every row.</p>
 */
public final class Xlsx {

    private Xlsx() {
    }

    private static final Pattern ROW = Pattern.compile("<row[ >].*?</row>|<row[^>]*/>", Pattern.DOTALL);
    private static final Pattern CELL = Pattern.compile("<c ([^>]*?)/>|<c ([^>]*?)>(.*?)</c>", Pattern.DOTALL);
    private static final Pattern REFERENCE = Pattern.compile("r=\"([A-Z]+)[0-9]+\"");
    private static final Pattern TYPE = Pattern.compile("t=\"([^\"]*)\"");
    private static final Pattern VALUE = Pattern.compile("<v>(.*?)</v>", Pattern.DOTALL);
    private static final Pattern TEXT = Pattern.compile("<t[^>]*>(.*?)</t>", Pattern.DOTALL);
    private static final Pattern SHARED = Pattern.compile("<si>(.*?)</si>", Pattern.DOTALL);
    private static final Pattern SHEET = Pattern.compile(
            "<sheet[^>]*name=\"([^\"]*)\"[^>]*r:id=\"([^\"]*)\"[^>]*/>");
    private static final Pattern RELATIONSHIP = Pattern.compile(
            "<Relationship[^>]*Id=\"([^\"]*)\"[^>]*Target=\"([^\"]*)\"[^>]*/>");

    /** The rows of the first worksheet, as strings with empty cells kept. */
    public static List<List<String>> rows(Path file) throws IOException {
        return rows(file, null);
    }

    /** The names of the worksheets, in the order the workbook lists them. */
    public static List<String> sheetNames(Path file) throws IOException {
        return new ArrayList<>(sheetIndex(unzip(file)).keySet());
    }

    /**
     * The rows of one worksheet, as strings with empty cells kept.
     *
     * @param sheetName the sheet to read, or null for the first one
     */
    public static List<List<String>> rows(Path file, String sheetName) throws IOException {
        Map<String, byte[]> parts = unzip(file);
        List<String> shared = sharedStrings(parts);
        byte[] sheet = sheetName == null ? firstSheet(parts) : namedSheet(parts, sheetName);
        if (sheet == null) {
            throw new IOException("no worksheet " + (sheetName == null ? "" : sheetName)
                    + " in " + file);
        }
        List<List<String>> rows = new ArrayList<>();
        Matcher rowMatcher = ROW.matcher(new String(sheet, StandardCharsets.UTF_8));
        while (rowMatcher.find()) {
            rows.add(cells(rowMatcher.group(), shared));
        }
        return rows;
    }

    /** The cells of one row, indexed by their column so gaps stay gaps. */
    private static List<String> cells(String row, List<String> shared) {
        List<String> cells = new ArrayList<>();
        Matcher cellMatcher = CELL.matcher(row);
        while (cellMatcher.find()) {
            String attributes = cellMatcher.group(1) != null ? cellMatcher.group(1) : cellMatcher.group(2);
            String body = cellMatcher.group(3) == null ? "" : cellMatcher.group(3);
            int column = columnOf(attributes);
            while (cells.size() < column) {
                cells.add("");
            }
            cells.add(value(attributes, body, shared));
        }
        return cells;
    }

    /** The zero-based column a cell reference such as {@code BC12} names. */
    private static int columnOf(String attributes) {
        Matcher m = REFERENCE.matcher(attributes);
        if (!m.find()) {
            return -1;
        }
        int column = 0;
        for (char c : m.group(1).toCharArray()) {
            column = column * 26 + (c - 'A' + 1);
        }
        return column - 1;
    }

    /** One cell as text, resolving a shared-string or inline-string cell. */
    private static String value(String attributes, String body, List<String> shared) {
        Matcher type = TYPE.matcher(attributes);
        String kind = type.find() ? type.group(1) : "";
        if ("inlineStr".equals(kind)) {
            return unescape(joinText(body));
        }
        Matcher v = VALUE.matcher(body);
        if (!v.find()) {
            return "";
        }
        String raw = v.group(1);
        if ("s".equals(kind)) {
            int index = Integer.parseInt(raw.strip());
            return index < shared.size() ? shared.get(index) : "";
        }
        return unescape(raw);
    }

    /** The shared string table, which most text cells point into. */
    private static List<String> sharedStrings(Map<String, byte[]> parts) {
        List<String> strings = new ArrayList<>();
        byte[] part = parts.get("xl/sharedStrings.xml");
        if (part == null) {
            return strings;
        }
        Matcher m = SHARED.matcher(new String(part, StandardCharsets.UTF_8));
        while (m.find()) {
            strings.add(unescape(joinText(m.group(1))));
        }
        return strings;
    }

    /** A shared string can be split into runs; the text is their concatenation. */
    private static String joinText(String item) {
        StringBuilder sb = new StringBuilder();
        Matcher m = TEXT.matcher(item);
        while (m.find()) {
            sb.append(m.group(1));
        }
        return sb.toString();
    }

    /** The worksheet names, each mapped to the part that holds it. */
    private static Map<String, String> sheetIndex(Map<String, byte[]> parts) {
        Map<String, String> index = new LinkedHashMap<>();
        byte[] workbook = parts.get("xl/workbook.xml");
        byte[] rels = parts.get("xl/_rels/workbook.xml.rels");
        if (workbook == null || rels == null) {
            return index;
        }
        // the workbook names each sheet and points at it by relationship id
        Map<String, String> targets = new LinkedHashMap<>();
        Matcher rel = RELATIONSHIP.matcher(new String(rels, StandardCharsets.UTF_8));
        while (rel.find()) {
            targets.put(rel.group(1), rel.group(2));
        }
        Matcher sheet = SHEET.matcher(new String(workbook, StandardCharsets.UTF_8));
        while (sheet.find()) {
            String target = targets.get(sheet.group(2));
            if (target != null) {
                index.put(unescape(sheet.group(1)),
                        target.startsWith("/") ? target.substring(1) : "xl/" + target);
            }
        }
        return index;
    }

    private static byte[] namedSheet(Map<String, byte[]> parts, String sheetName) {
        String part = sheetIndex(parts).get(sheetName);
        return part == null ? null : parts.get(part);
    }

    private static byte[] firstSheet(Map<String, byte[]> parts) {
        return parts.entrySet().stream()
                .filter(e -> e.getKey().startsWith("xl/worksheets/sheet"))
                .sorted(Map.Entry.comparingByKey())
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
    }

    private static String unescape(String text) {
        return text.replace("&lt;", "<").replace("&gt;", ">")
                .replace("&quot;", "\"").replace("&apos;", "'")
                .replace("&amp;", "&");
    }

    private static Map<String, byte[]> unzip(Path file) throws IOException {
        Map<String, byte[]> parts = new LinkedHashMap<>();
        try (ZipInputStream zip = new ZipInputStream(Files.newInputStream(file))) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                if (!entry.isDirectory()) {
                    parts.put(entry.getName(), readAll(zip));
                }
            }
        }
        return parts;
    }

    private static byte[] readAll(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int read;
        while ((read = in.read(buffer)) > 0) {
            out.write(buffer, 0, read);
        }
        return out.toByteArray();
    }
}
