import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Generates the id-loc.dat registry consumed by the IdNik class.
 *
 * <p>Source: the bridging service of Badan Pusat Statistik, Indonesia's
 * statistics agency, which publishes both its own codes and the Kemendagri
 * ones. A NIK carries the Kemendagri code, which is the one taken here.</p>
 *
 * <p>Pass the province list and one regency list per province; the level of
 * each is told from the shape of the codes it holds:</p>
 *
 * <pre>
 *   base=https://sig.bps.go.id/rest-bridging/getwilayah
 *   curl -L -o provinsi.json "$base?level=provinsi"
 *   for p in $(grep -o '"kode_bps":"[0-9]*"' provinsi.json | cut -d'"' -f4); do
 *     curl -L -o "kab-$p.json" "$base?level=kabupaten&amp;parent=$p"
 *   done
 *   java tools/GenerateIdLocDat.java provinsi.json kab-*.json \
 *       &gt; stdnum-apac/src/main/resources/io/github/jefersonsantos06/stdnum/apac/id-loc.dat
 * </pre>
 *
 * <p>The reference also records an ISO 3166-2 code and an English name for
 * each province. Neither is in this source and neither is what a number is
 * checked against, so neither is invented here.</p>
 */
public final class GenerateIdLocDat {

    private GenerateIdLocDat() {
    }

    /** One record of the service: the Kemendagri code and the name under it. */
    private static final Pattern RECORD = Pattern.compile(
            "\"kode_dagri\"\\s*:\\s*\"([0-9]{2})(?:\\.([0-9]{2}))?\"\\s*,\\s*"
                    + "\"nama_dagri\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"");

    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            System.err.println("usage: java GenerateIdLocDat.java <provinsi.json> <kab-*.json>...");
            System.exit(2);
        }
        Map<String, String> provinces = new TreeMap<>();
        // province -> its regencies, both in code order
        Map<String, Map<String, String>> regencies = new TreeMap<>();
        for (String arg : args) {
            String json = Files.readString(Path.of(arg), StandardCharsets.UTF_8);
            Matcher record = RECORD.matcher(json);
            while (record.find()) {
                String province = record.group(1);
                String regency = record.group(2);
                String name = clean(record.group(3));
                if (regency == null) {
                    provinces.putIfAbsent(province, name);
                } else {
                    regencies.computeIfAbsent(province, k -> new TreeMap<>())
                            .putIfAbsent(regency, name);
                }
            }
        }
        if (provinces.isEmpty()) {
            System.err.println("no provinces found: the service layout has changed");
            System.exit(1);
        }

        PrintStream out = new PrintStream(System.out, true, StandardCharsets.UTF_8);
        out.println("# Indonesian administrative region codes: the province a NIK opens with,");
        out.println("# and the regency or city within it.");
        out.println("# Generated from the bridging service of Badan Pusat Statistik at");
        out.println("# https://sig.bps.go.id/rest-bridging/getwilayah, taking the Kemendagri");
        out.println("# codes, which are the ones a NIK carries.");
        for (Map.Entry<String, String> province : provinces.entrySet()) {
            out.println(province.getKey() + " name_id=\"" + province.getValue() + "\"");
            regencies.getOrDefault(province.getKey(), new TreeMap<>()).forEach((code, name) ->
                    out.println("  " + code + " name_id=\"" + name + "\""));
        }
    }

    /**
     * A name as the register writes it, without the KAB. or KOTA. prefix the
     * service puts in front of every regency and city.
     */
    private static String clean(String name) {
        String text = name.replace("\\/", "/").replace("\\\"", "").replace("\"", "");
        text = text.replaceAll("^(KAB|KOTA)\\.?\\s+", "");
        return text.replaceAll("\\s+", " ").strip();
    }
}
