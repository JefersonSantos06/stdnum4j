package io.github.jefersonsantos06.stdnum.br;

import io.github.jefersonsantos06.stdnum.StdNums;
import io.github.jefersonsantos06.stdnum.br.ie.InscricaoEstadual;
import io.github.jefersonsantos06.stdnum.spi.Check;
import io.github.jefersonsantos06.stdnum.spi.Messages;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.ValidationError;
import io.github.jefersonsantos06.stdnum.tck.Fixtures;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The Brazilian numbers say why they refused a number in Portuguese: this is
 * the module that carries its own translations, and the one where the person
 * reading the message is most likely to be Brazilian.
 */
class MessagesBrTest {

    private static final Messages PT = Messages.forLocale(Locale.forLanguageTag("pt-BR"));
    private static final Messages ENGLISH = Messages.forLocale(Locale.ROOT);

    /** One per package that carries a bundle. */
    private static final List<Class<?>> ANCHORS =
            List.of(Cpf.class, InscricaoEstadual.class);

    /**
     * Messages no invalid fixture reaches. Listing one is a decision, not an
     * oversight: the alternative is a fixture that provokes it.
     */
    private static final Set<String> UNREACHED = Set.of(
            "brcode.crc.short", "brcode.tlv.malformed", "brcode.tlv.overrun",
            "brcode.tlv.trailing", "codigo-barras.currency", "ie.al.company-type",
            "ie.prefix", "ie.to.registration-type");


    private final Map<Class<?>, Properties> bundles = new HashMap<>();

    private static Check.Invalid reject(StdNum number, String input) {
        Check check = number.check(input);
        assertInstanceOf(Check.Invalid.class, check, input + " should have been refused");
        return (Check.Invalid) check;
    }

    /** The translation file as it sits in the jar, read the way Messages reads it. */
    private Properties bundleOf(Class<?> anchor) {
        return bundles.computeIfAbsent(anchor, key -> {
            Properties properties = new Properties();
            try (InputStream in = key.getResourceAsStream("messages_pt.properties")) {
                assertTrue(in != null, () -> "sem bundle pt ao lado de " + key.getName());
                properties.load(new InputStreamReader(in, StandardCharsets.UTF_8));
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            return properties;
        });
    }

    @Test
    void aRefusalWithAReasonOfItsOwnIsSaidInPortuguese() {
        Check.Invalid invalid = reject(Cpf.INSTANCE, "111.111.111-11");
        assertEquals("Um CPF formado por um único dígito repetido não é válido.",
                PT.render(invalid));
        assertEquals("A CPF consisting of a single repeated digit is not valid.",
                ENGLISH.render(invalid));
        // and the English is still what a log gets, untouched
        assertEquals(ENGLISH.render(invalid), invalid.reason());
    }

    @Test
    void eachPackageCarriesItsOwnTranslations() {
        // br.ie is a package of its own, with a bundle of its own beside it
        Check.Invalid invalid = reject(StdNums.byId("br.ie.sp").orElseThrow(), "111111111");
        assertEquals("Uma inscrição estadual formada por um único caractere repetido"
                + " não é válida.", PT.render(invalid));
    }

    @Test
    void aRefusalWithNoReasonOfItsOwnFallsBackToTheSentenceForItsError() {
        // no code on this one: the four-sentence net answers, and answers in
        // Portuguese
        Check.Invalid invalid = reject(Cpf.INSTANCE, "390.533.447-06");
        assertEquals(ValidationError.INVALID_CHECKSUM, invalid.error());
        assertEquals("A soma de verificação ou o dígito verificador do número é inválido.",
                PT.render(invalid));
        assertEquals("The number's checksum or check digit is invalid.", ENGLISH.render(invalid));
    }

    @Test
    void theGenericSentenceIsGrammaticalWhateverTheNumberIsCalled() {
        // it names no number, which is why it can serve "Chave NF-e" (feminine)
        // and "Codigo de barras" (masculine) alike
        assertEquals("O número tem um comprimento inválido.",
                PT.render(reject(ChaveNfe.INSTANCE, "123")));
        assertEquals("O número tem um comprimento inválido.",
                PT.render(reject(CodigoBarras.INSTANCE, "123")));
        assertEquals("O número tem um comprimento inválido.",
                PT.render(reject(LinhaDigitavel.INSTANCE, "123")));
    }

    /**
     * Every reason a Brazilian number gives has a Portuguese sentence, and
     * every Portuguese sentence is a reason some number gives.
     *
     * <p>The first half asks the <em>bundle</em> whether the key is there, not
     * the renderer whether the sentence looks Portuguese: a missing key falls
     * through to {@code error.<NAME>}, which <em>is</em> translated, so
     * rendering can never reveal one.</p>
     *
     * <p>The second half is why {@link #UNREACHED} exists. The fixtures cannot
     * reach every message — some need a payload this module has no invalid
     * sample for — so a new key has to be either exercised or listed, and
     * either way somebody decided.</p>
     */
    @Test
    void theTranslationsAndTheMessagesAreTheSameSet() {
        Map<String, String> untranslated = new TreeMap<>();
        TreeSet<String> reached = new TreeSet<>();
        for (StdNum number : StdNums.byCountry("BR")) {
            String id = number.descriptor().id();
            for (String sample : Fixtures.load(getClass(), id + "-invalid")) {
                if (!(number.check(sample) instanceof Check.Invalid invalid)) {
                    continue;
                }
                String code = invalid.message().code();
                if (code == null) {
                    continue;
                }
                if (bundleOf(invalid.message().anchor()).containsKey(code)) {
                    reached.add(code);
                } else {
                    untranslated.put(code, invalid.reason());
                }
            }
        }
        assertTrue(untranslated.isEmpty(), () -> "sem tradução pt: " + untranslated);
        assertFalse(reached.isEmpty(), "nenhuma mensagem com código foi exercitada");

        TreeSet<String> orphans = new TreeSet<>(keys());
        orphans.removeAll(reached);
        orphans.removeAll(UNREACHED);
        assertTrue(orphans.isEmpty(),
                () -> "traduções que nenhum número usa, ou que ninguém decidiu"
                        + " como exercitar: " + orphans);

        TreeSet<String> stale = new TreeSet<>(UNREACHED);
        stale.retainAll(reached);
        assertTrue(stale.isEmpty(),
                () -> "listadas como não exercitadas, mas as fixtures as alcançam: " + stale);
    }

    /** A key left empty renders nothing at all, so no key may be left empty. */
    @Test
    void noTranslationIsBlank() {
        Map<String, String> blank = new TreeMap<>();
        for (Class<?> anchor : ANCHORS) {
            Properties bundle = bundleOf(anchor);
            assertFalse(bundle.isEmpty(), anchor.getName());
            bundle.forEach((key, value) -> {
                if (String.valueOf(value).isBlank()) {
                    blank.put(String.valueOf(key), anchor.getName());
                }
            });
        }
        assertTrue(blank.isEmpty(), () -> "traduções vazias: " + blank);
    }

    /** Every key in the two bundles. */
    private TreeSet<String> keys() {
        TreeSet<String> keys = new TreeSet<>();
        for (Class<?> anchor : ANCHORS) {
            bundleOf(anchor).keySet().forEach(k -> keys.add(String.valueOf(k)));
        }
        return keys;
    }
}
