import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Generates the isbn.dat prefix database consumed by the Isbn class from the
 * official ISBN International range message.
 *
 * <p>Usage:</p>
 * <pre>
 *   curl -L -o RangeMessage.xml https://www.isbn-international.org/export_rangemessage.xml
 *   java tools/GenerateIsbnDat.java RangeMessage.xml \
 *       &gt; stdnum-international/src/main/resources/io/github/jefersonsantos06/stdnum/international/isbn.dat
 * </pre>
 *
 * <p>The output is a NumDb file: for each EAN.UCC prefix (978, 979) one line
 * per group-range rule, then one labelled line per registration group with
 * its publisher ranges as children. Rules with length 0 mark unassigned
 * ranges and are skipped.</p>
 */
public final class GenerateIsbnDat {

    private GenerateIsbnDat() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println("usage: java GenerateIsbnDat.java <RangeMessage.xml>");
            System.exit(2);
        }
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        Document doc = factory.newDocumentBuilder().parse(new File(args[0]));

        String serial = text(doc, "MessageSerialNumber");
        String date = text(doc, "MessageDate");
        System.out.println("# ISBN prefix and registration group ranges.");
        System.out.println("# Generated from RangeMessage.xml, downloaded from");
        System.out.println("# https://www.isbn-international.org/export_rangemessage.xml");
        System.out.println("# serial " + serial);
        System.out.println("# date " + date);

        // group ranges per EAN.UCC prefix, and publisher ranges per group
        Map<String, List<String>> groupRanges = new LinkedHashMap<>();
        NodeList prefixes = doc.getElementsByTagName("EAN.UCC");
        for (int i = 0; i < prefixes.getLength(); i++) {
            Element element = (Element) prefixes.item(i);
            groupRanges.put(text(element, "Prefix"), ranges(element));
        }
        Map<String, Map<String, List<String>>> groups = new LinkedHashMap<>();
        Map<String, String> agencies = new LinkedHashMap<>();
        NodeList groupNodes = doc.getElementsByTagName("Group");
        for (int i = 0; i < groupNodes.getLength(); i++) {
            Element element = (Element) groupNodes.item(i);
            String[] prefix = text(element, "Prefix").split("-");
            groups.computeIfAbsent(prefix[0], k -> new LinkedHashMap<>())
                    .put(prefix[1], ranges(element));
            agencies.put(prefix[0] + "-" + prefix[1], text(element, "Agency"));
        }

        for (Map.Entry<String, List<String>> prefix : groupRanges.entrySet()) {
            System.out.println(prefix.getKey());
            emitRanges(" ", prefix.getValue());
            Map<String, List<String>> prefixGroups =
                    groups.getOrDefault(prefix.getKey(), Map.of());
            for (Map.Entry<String, List<String>> group : prefixGroups.entrySet()) {
                String agency = agencies.get(prefix.getKey() + "-" + group.getKey());
                System.out.println(" " + group.getKey()
                        + " agency=\"" + agency.replace("\"", "'") + "\"");
                emitRanges("  ", group.getValue());
            }
        }
    }

    /** The applicable ranges of one element, truncated to the rule length. */
    private static List<String> ranges(Element element) {
        List<String> result = new ArrayList<>();
        NodeList rules = element.getElementsByTagName("Rule");
        for (int i = 0; i < rules.getLength(); i++) {
            Element rule = (Element) rules.item(i);
            int length = Integer.parseInt(text(rule, "Length"));
            if (length == 0) {
                continue; // unassigned range
            }
            String[] range = text(rule, "Range").split("-");
            String low = range[0].substring(0, length);
            String high = range[1].substring(0, length);
            result.add(low.equals(high) ? low + "-" + high : low + "-" + high);
        }
        return result;
    }

    private static void emitRanges(String indent, List<String> ranges) {
        for (int i = 0; i < ranges.size(); i += 8) {
            System.out.println(indent + String.join(",",
                    ranges.subList(i, Math.min(i + 8, ranges.size()))));
        }
    }

    private static String text(Document doc, String tag) {
        return doc.getElementsByTagName(tag).item(0).getTextContent().strip();
    }

    private static String text(Element element, String tag) {
        return element.getElementsByTagName(tag).item(0).getTextContent().strip();
    }
}
