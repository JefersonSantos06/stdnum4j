import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
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
 * <p>The reference also records an ISO 3166-2 code and an English name for
 * each province. Neither is in this source and neither is what a number is
 * checked against, so neither is invented here.</p>
 */
public final class GenerateIdLocDat implements Source {

    @Override
    public String id() {
        return "id-loc";
    }

    @Override
    public String title() {
        return "Regenerate id-loc.dat, the Indonesian regions";
    }

    @Override
    public String output() {
        return "stdnum4j-apac/src/main/resources/io/github/jefersonsantos06/stdnum/apac/id-loc.dat";
    }

    private static final String BRIDGING = "https://sig.bps.go.id/rest-bridging/getwilayah";
    private static final Pattern PROVINCE_CODE = Pattern.compile("\"kode_bps\":\"([0-9]+)\"");

    @Override
    public List<Download> seeds() {
        return List.of(new Download(BRIDGING + "?level=provinsi", "provinsi.json"));
    }

    @Override
    public List<Download> downloads(Map<String, String> seeds) {
        List<Download> all = new ArrayList<>(seeds());
        Matcher code = PROVINCE_CODE.matcher(seeds.get("provinsi.json"));
        List<String> seen = new ArrayList<>();
        while (code.find()) {
            if (!seen.contains(code.group(1))) {
                seen.add(code.group(1));
                all.add(new Download(BRIDGING + "?level=kabupaten&parent=" + code.group(1),
                        "kab-" + code.group(1) + ".json"));
            }
        }
        return all;
    }

    /** One record of the service: the Kemendagri code and the name under it. */
    private static final Pattern RECORD = Pattern.compile(
            "\"kode_dagri\"\\s*:\\s*\"([0-9]{2})(?:\\.([0-9]{2}))?\"\\s*,\\s*"
                    + "\"nama_dagri\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"");

    @Override
    public void generate(Run run, PrintStream out) throws Exception {
        Map<String, String> provinces = new TreeMap<>();
        // province -> its regencies, both in code order
        Map<String, Map<String, String>> regencies = new TreeMap<>();
        for (Path source : run.files()) {
            String json = Files.readString(source, StandardCharsets.UTF_8);
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
            throw new IllegalStateException("no provinces found: the service layout has changed");
        }

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
