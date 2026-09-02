package io.github.jefersonsantos06.stdnum.spi;

import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The rendering rules. The {@code xx}, {@code xx-YY} and {@code zz} locales
 * exist only as fixtures under {@code src/test/resources}, beside this
 * package; {@code pt-BR} is the translation the library really ships.
 */
class MessagesTest {

    private static final Locale XX = Locale.forLanguageTag("xx");
    private static final Locale XX_YY = Locale.forLanguageTag("xx-YY");
    private static final Locale ZZ = Locale.forLanguageTag("zz");
    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");

    private static final String ENGLISH = "The number is wrong.";

    private static Message coded(Object... args) {
        return Message.of(ValidationError.class, "test.code", ENGLISH, args);
    }

    @Test
    void aCodeWithATranslationIsSaidInTheChosenLanguage() {
        assertEquals("xx: geral {0}",
                Messages.forLocale(XX).render(ValidationError.INVALID_FORMAT, coded()));
    }

    @Test
    void theCountryFileOverlaysTheLanguageFileRatherThanReplacingIt() {
        Messages messages = Messages.forLocale(XX_YY);
        // test.code is overridden by messages_xx_YY.properties
        assertEquals("xx-YY: especifico 7",
                messages.render(ValidationError.INVALID_FORMAT, coded(7)));
        // and everything it does not mention still comes from messages_xx
        assertEquals("xx: 1 e depois 2, e de novo 1",
                messages.render(ValidationError.INVALID_FORMAT,
                        Message.of(ValidationError.class, "test.args", ENGLISH, 1, 2)));
    }

    @Test
    void anUntranslatedCodeFallsBackToTheSentenceForItsError() {
        // no such key anywhere, so the four-sentence safety net answers
        Message unknown = Message.of(ValidationError.class, "no.such.code", ENGLISH);
        assertEquals("xx: formato",
                Messages.forLocale(XX).render(ValidationError.INVALID_FORMAT, unknown));
        // and that net is what the shipped pt-BR translation is for
        assertEquals("A soma de verificação ou o dígito verificador do número é inválido.",
                Messages.forLocale(PT_BR).render(ValidationError.INVALID_CHECKSUM, unknown));
    }

    @Test
    void aMessageWithNoCodeAtAllStillGetsTheSentenceForItsError() {
        assertEquals("Uma das partes do número é inválida ou desconhecida.",
                Messages.forLocale(PT_BR)
                        .render(ValidationError.INVALID_COMPONENT, Message.plain(ENGLISH)));
    }

    @Test
    void aLanguageWithNoTranslationAtAllFallsBackToEnglish() {
        assertEquals(ENGLISH,
                Messages.forLocale(Locale.JAPANESE)
                        .render(ValidationError.INVALID_FORMAT, coded()));
        assertEquals(ENGLISH,
                Messages.forLocale(Locale.ROOT).render(ValidationError.INVALID_FORMAT, coded()));
    }

    @Test
    void theLocaleOfTheMachineIsNeverConsulted() {
        Locale previous = Locale.getDefault();
        try {
            Locale.setDefault(PT_BR);
            // the JVM is Brazilian; the caller asked for none, and gets none
            assertEquals(ENGLISH, Messages.forLocale(Locale.ROOT)
                    .render(ValidationError.INVALID_FORMAT, coded()));
            assertEquals(ENGLISH, Messages.forLocale(Locale.JAPANESE)
                    .render(ValidationError.INVALID_FORMAT, coded()));
        } finally {
            Locale.setDefault(previous);
        }
    }

    @Test
    void aTranslationThatDoesNotParseIsTreatedAsAbsent() {
        // messages_zz.properties holds a Windows path whose backslash-u
        // opens an escape that is not one, and Properties.load says so by
        // throwing IllegalArgumentException. (That escape cannot appear in
        // this comment: javac expands unicode escapes inside comments too.)
        assertThrows(IllegalArgumentException.class, () -> {
            java.util.Properties properties = new java.util.Properties();
            try (var in = ValidationError.class.getResourceAsStream("messages_zz.properties")) {
                properties.load(new java.io.InputStreamReader(in,
                        java.nio.charset.StandardCharsets.UTF_8));
            }
        });
        // and none of that reaches the caller
        assertEquals(ENGLISH, Messages.forLocale(ZZ)
                .render(ValidationError.INVALID_FORMAT, coded()));
    }

    @Test
    void argumentsAreFilledIntoWhicheverSentenceWins() {
        assertEquals("xx: geral 42",
                Messages.forLocale(XX).render(ValidationError.INVALID_FORMAT, coded(42)));
        // the English fallback carries them too
        assertEquals("The block AB is unknown.",
                Messages.forLocale(Locale.ROOT).render(ValidationError.INVALID_COMPONENT,
                        Message.of(ValidationError.class, "no.such.code",
                                "The block {0} is unknown.", "AB")));
    }

    @Test
    void anApostropheSurvives() {
        // java.text.MessageFormat would eat both of these: a lone quote is its
        // escape character, and it swallows the placeholder that follows
        assertEquals("The number's checksum is invalid.",
                Messages.forLocale(Locale.ROOT).render(ValidationError.INVALID_CHECKSUM,
                        Message.plain("The number's checksum is invalid.")));
        assertEquals("L'identifiant AB n'est pas valide.",
                Messages.forLocale(Locale.ROOT).render(ValidationError.INVALID_COMPONENT,
                        Message.of(ValidationError.class, "no.such.code",
                                "L'identifiant {0} n'est pas valide.", "AB")));
    }

    @Test
    void abraceThatOpensNoIndexIsJustABrace() {
        assertEquals("xx: {} {x} {99} literais",
                Messages.forLocale(XX).render(ValidationError.INVALID_FORMAT,
                        Message.of(ValidationError.class, "test.braces", ENGLISH, "unused")));
    }

    @Test
    void renderingNeverThrows() {
        Messages messages = Messages.forLocale(PT_BR);
        for (ValidationError error : ValidationError.values()) {
            assertDoesNotThrow(() -> messages.render(error, Message.plain("")));
            assertDoesNotThrow(() -> messages.render(error,
                    Message.of(ValidationError.class, "no.such.code", "{0}{1}{2}")));
        }
        assertThrows(NullPointerException.class, () -> messages.render(null, coded()));
        assertThrows(NullPointerException.class,
                () -> messages.render(ValidationError.INVALID_FORMAT, (Message) null));
    }

    @Test
    void theSameFailureIsSaidDifferentlyToDifferentCallers() {
        InvalidChecksumException failure = new InvalidChecksumException();
        assertEquals("The number's checksum or check digit is invalid.", failure.getMessage());
        assertEquals("A soma de verificação ou o dígito verificador do número é inválido.",
                Messages.forLocale(PT_BR).render(failure));
        assertEquals(failure.getMessage(), Messages.forLocale(Locale.ROOT).render(failure));
    }

    @Test
    void aRejectionRendersTheSameWayAnExceptionDoes() {
        Check check = new StdNum() {
            @Override
            public Descriptor descriptor() {
                return Descriptor.of("test", "Test").title("Test").build();
            }

            @Override
            public String compact(String number) {
                return number;
            }

            @Override
            public String validate(String number) {
                throw new InvalidComponentException(coded("AB"));
            }
        }.check("anything");

        Check.Invalid invalid = (Check.Invalid) check;
        assertEquals(ValidationError.INVALID_COMPONENT, invalid.error());
        assertEquals(ENGLISH, invalid.reason());
        assertEquals("xx: geral AB", Messages.forLocale(XX).render(invalid));
    }

    @Test
    void aMessageIsTheSameWhicheverWayItReachesTheCaller() {
        InvalidFormatException failure = new InvalidFormatException(coded("Z"));
        assertEquals("xx: geral Z", Messages.forLocale(XX).render(failure));
        assertEquals("xx: geral Z", Messages.forLocale(XX)
                .render(failure.error(), failure.message()));
        assertSame(failure.message(), failure.message());
    }

    @Test
    void aPlainMessageCostsNothingToRenderInEnglish() {
        Message plain = Message.plain(ENGLISH);
        assertSame(ENGLISH, plain.text());
        assertTrue(plain.args().isEmpty());
        assertEquals("", Message.plain(null).text());
    }

    @Test
    void everyErrorHasASentenceInEveryTranslationTheLibraryShips() {
        // the four-sentence net is what makes a partial translation useful;
        // a fifth ValidationError with no sentence would silently render
        // English while the suite stayed green
        Messages pt = Messages.forLocale(PT_BR);
        for (ValidationError error : ValidationError.values()) {
            String said = pt.render(error, Message.plain(ENGLISH));
            assertNotEquals(ENGLISH, said, () -> "sem error." + error.name() + " em pt");
            assertFalse(said.isBlank(), error.name());
        }
    }

    @Test
    void aTranslationLeftEmptyCountsAsNoTranslation() {
        // "key =" with nothing after it is what a half-written translation
        // looks like, and it is the one kind of breakage that parses
        Messages xx = Messages.forLocale(XX);
        assertEquals(ENGLISH, xx.render(ValidationError.INVALID_CHECKSUM,
                Message.of(ValidationError.class, "test.blank", ENGLISH)));
        assertEquals(ENGLISH, xx.render(ValidationError.INVALID_CHECKSUM,
                Message.of(ValidationError.class, "test.spaces", ENGLISH)));
        // and an empty error.<NAME> would fall through the same way
        assertEquals("xx: formato", xx.render(ValidationError.INVALID_FORMAT,
                Message.of(ValidationError.class, "test.blank", ENGLISH)));
    }

    @Test
    void aLocaleThatNamesAPathIsNotALocale() {
        // a Locale can be built out of anything, and it ends up in a resource
        // path; new Locale(...) does not validate what forLanguageTag would
        @SuppressWarnings("deprecation")
        Locale hostile = new Locale("xx/../../secret");
        assertEquals(ENGLISH, Messages.forLocale(hostile)
                .render(ValidationError.INVALID_FORMAT, coded()));
        @SuppressWarnings("deprecation")
        Locale oddCountry = new Locale("xx", "../..");
        assertEquals("xx: geral {0}", Messages.forLocale(oddCountry)
                .render(ValidationError.INVALID_FORMAT, coded()));
    }

    @Test
    void aMessageBuiltFromAStringCarriesNoLuggage() {
        // three quarters of all failures take this path: no code, no anchor,
        // nothing that could ever be translated by one, so nothing is built
        InvalidFormatException failure = new InvalidFormatException("plain");
        assertEquals("plain", failure.getMessage());
        assertNull(failure.message().anchor());
        assertNull(failure.message().code());
        // and a null message is still exactly what it was before
        assertNull(new InvalidFormatException((String) null).getMessage());
    }

    @Test
    void theLocaleIsPartOfWhatAMessagesIs() {
        assertEquals(PT_BR, Messages.forLocale(PT_BR).locale());
        assertNotEquals(Messages.forLocale(PT_BR).toString(),
                Messages.forLocale(Locale.ROOT).toString());
        assertThrows(NullPointerException.class, () -> Messages.forLocale(null));
    }
}
