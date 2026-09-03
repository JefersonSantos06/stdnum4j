import java.io.IOException;
import java.io.PrintStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Generates the imsi.dat registry consumed by the Imsi class.
 *
 * <p>Source: the mobile network code tables on Wikipedia, which mirror the
 * ITU list. One page gives the country codes and one page per ITU region
 * gives the networks within it. Pass the raw wikitext of each page, in any
 * order:</p>
 *
 * <p>Country names come out as the page's own section headings. The
 * reference keeps a hand-maintained table of about a hundred rewrites for
 * them; that table is judgement about presentation rather than about which
 * numbers exist, so it is not reproduced here. It affects what
 * {@code Imsi.info} reports, never what {@code validate} accepts.</p>
 */
public final class GenerateImsiDat implements Source {

    @Override
    public String id() {
        return "imsi";
    }

    @Override
    public String title() {
        return "Regenerate imsi.dat, the mobile country and network codes";
    }

    @Override
    public String output() {
        return "stdnum-international/src/main/resources/io/github/jefersonsantos06/stdnum/international/imsi.dat";
    }

    /**
     * The pages, in the order a repeated code is resolved: a later one
     * wins, so this order is part of the result and not a detail.
     */
    private static final List<String> PAGES = List.of(
            "Mobile_country_code",
            "Mobile_network_codes_in_ITU_region_2xx_(Europe)",
            "Mobile_network_codes_in_ITU_region_3xx_(North_America)",
            "Mobile_network_codes_in_ITU_region_4xx_(Asia)",
            "Mobile_network_codes_in_ITU_region_5xx_(Oceania)",
            "Mobile_network_codes_in_ITU_region_6xx_(Africa)",
            "Mobile_network_codes_in_ITU_region_7xx_(South_America)");

    @Override
    public List<Download> downloads(Map<String, String> seeds) {
        return PAGES.stream()
                .map(page -> new Download(
                        "https://en.wikipedia.org/w/index.php?title="
                                + URLEncoder.encode(page, StandardCharsets.UTF_8)
                                        .replace("+", "_") + "&action=raw",
                        page + ".wiki"))
                .sorted(Comparator.comparing(Download::file))
                .toList();
    }

    /** A section heading, which names the country and sometimes its code. */
    private static final Pattern HEADING = Pattern.compile(
            "^={2,4}\\s*(?<country>.*?)(\\s+-\\s+(?<cc>\\S{2}))?\\s*={2,4}$");
    /** A table row, once its cell separators have been made unambiguous. */
    private static final Pattern ROW = Pattern.compile(
            "^\\|\\s*(?<mcc>[0-9]+)"
                    + "\\s*\\\\\\\\\\s*(?<mnc>[0-9,-]+)"
                    + "(\\s*\\\\\\\\\\s*(?<brand>[^\\\\]*)"
                    + "(\\s*\\\\\\\\\\s*(?<operator>[^\\\\]*)"
                    + "(\\s*\\\\\\\\\\s*(?<status>[^\\\\]*)"
                    + "(\\s*\\\\\\\\\\s*(?<bands>[^\\\\]*)"
                    + ")?)?)?)?");
    private static final Pattern REFERENCE = Pattern.compile("<ref>.*?</ref>", Pattern.DOTALL);
    private static final Pattern TEMPLATE = Pattern.compile("\\{\\{.*?}}", Pattern.DOTALL);
    private static final Pattern URL = Pattern.compile("(?i)\\bhttps?://\\S+");

    @Override
    public void generate(Run run, PrintStream out) throws Exception {
        // mcc -> mnc -> properties, both kept in code order
        Map<String, Map<String, Map<String, String>>> operational = new TreeMap<>();
        Map<String, Map<String, Map<String, String>>> retired = new TreeMap<>();
        for (Path page : run.files()) {
            parse(Files.readString(page, StandardCharsets.UTF_8), operational, retired);
        }
        // a network that has been switched off is listed only where nothing
        // operational stands in its place
        retired.forEach((mcc, mncs) -> mncs.forEach((mnc, info) -> {
            Map<String, Map<String, String>> known =
                    operational.computeIfAbsent(mcc, k -> new TreeMap<>());
            if (known.get(mnc) == null && known.get(mnc.substring(0, Math.min(2, mnc.length()))) == null) {
                known.put(mnc, info);
            }
        }));
        if (operational.isEmpty()) {
            throw new IllegalStateException("no networks found: the page layout has changed");
        }

        out.println("# Mobile country and network codes: the first digits of an IMSI.");
        out.println("# Generated from the mobile network code tables on Wikipedia, which");
        out.println("# mirror the ITU list: https://en.wikipedia.org/wiki/Mobile_country_code");
        for (Map.Entry<String, Map<String, Map<String, String>>> mcc : operational.entrySet()) {
            out.println(mcc.getKey());
            List<String> codes = new ArrayList<>();
            for (Map.Entry<String, Map<String, String>> mnc : mcc.getValue().entrySet()) {
                if (mnc.getValue().isEmpty()) {
                    continue;
                }
                codes.add(mnc.getKey());
                StringBuilder sb = new StringBuilder(" ").append(mnc.getKey());
                new TreeMap<>(mnc.getValue()).forEach((key, value) -> {
                    if (!value.isEmpty()) {
                        sb.append(' ').append(key).append("=\"").append(value).append('"');
                    }
                });
                out.println(sb);
            }
            // a network code the tables do not list is still a network code,
            // so the whole width is left open under the country
            if (!codes.isEmpty()) {
                int width = codes.get(0).length();
                if (codes.stream().allMatch(c -> c.length() == width)) {
                    out.println(" " + "0".repeat(width) + "-" + "9".repeat(width));
                }
            }
        }
    }

    private static void parse(String page,
                              Map<String, Map<String, Map<String, String>>> operational,
                              Map<String, Map<String, Map<String, String>>> retired) {
        String country = "";
        String cc = "";
        for (String raw : page.split("\r?\n")) {
            String line = dashes(raw.strip());
            Matcher heading = HEADING.matcher(line);
            if (heading.matches()) {
                country = clean(heading.group("country"));
                cc = heading.group("cc") == null ? "" : heading.group("cc").toLowerCase();
                continue;
            }
            if (!line.contains("||")) {
                continue;
            }
            Matcher row = ROW.matcher(line.replace("||", "\\\\"));
            if (!row.lookingAt()) {
                continue;
            }
            Map<String, String> info = new LinkedHashMap<>();
            info.put("country", country);
            info.put("cc", cc);
            info.put("brand", clean(row.group("brand")));
            info.put("operator", clean(row.group("operator")));
            info.put("status", clean(row.group("status")));
            info.put("bands", clean(row.group("bands")));
            boolean off = "not operational".equalsIgnoreCase(info.get("status"));
            for (String mnc : expand(row.group("mnc"))) {
                (off ? retired : operational)
                        .computeIfAbsent(row.group("mcc"), k -> new TreeMap<>())
                        .computeIfAbsent(mnc, k -> new LinkedHashMap<>())
                        .putAll(info);
            }
        }
    }

    /** A comma-separated list of codes and ranges, as the codes themselves. */
    private static List<String> expand(String codes) {
        List<String> out = new ArrayList<>();
        for (String part : codes.split(",")) {
            int dash = part.indexOf('-');
            if (dash < 0) {
                out.add(part);
                continue;
            }
            String low = part.substring(0, dash);
            String high = part.substring(dash + 1);
            for (int i = Integer.parseInt(low); i <= Integer.parseInt(high); i++) {
                out.add(String.format("%0" + high.length() + "d", i));
            }
        }
        return out;
    }

    /**
     * The line with every dash written as an ASCII hyphen. The headings use
     * an en dash as often as a hyphen to set off the country code, and the
     * two have to read the same.
     */
    private static String dashes(String line) {
        return line.replace('‐', '-').replace('‑', '-').replace('‒', '-')
                .replace('–', '-').replace('—', '-').replace('―', '-')
                .replace('−', '-');
    }

    /** A cell as plain text: no references, templates, links or markup. */
    private static String clean(String value) {
        if (value == null) {
            return "";
        }
        String text = REFERENCE.matcher(value).replaceAll("");
        text = TEMPLATE.matcher(text).replaceAll("");
        text = URL.matcher(text).replaceAll("");
        text = text.replace("[", "").replace("]", "").replace("''", "").strip();
        // a wiki link shows the text after its last pipe
        text = text.substring(text.lastIndexOf('|') + 1);
        return text.replace("Unknown", "").replace("</sup>", "").replace("\"", "").strip();
    }
}
