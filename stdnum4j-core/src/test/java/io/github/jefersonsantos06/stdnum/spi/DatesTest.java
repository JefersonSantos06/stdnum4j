package io.github.jefersonsantos06.stdnum.spi;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DatesTest {

    @Test
    void aRealDateComesBack() {
        assertEquals(LocalDate.of(1956, 4, 27), Dates.birthDate(1956, 4, 27));
        assertEquals(LocalDate.of(2000, 2, 29), Dates.birthDate(2000, 2, 29));
    }

    @Test
    void aDayThatDoesNotExistIsRefused() {
        for (int[] wrong : new int[][] {{1900, 2, 29}, {1990, 4, 31}, {1990, 13, 1},
                                        {1990, 0, 1}, {1990, 1, 0}, {1990, 1, 32}}) {
            assertThrows(InvalidComponentException.class,
                    () -> Dates.birthDate(wrong[0], wrong[1], wrong[2]),
                    wrong[0] + "-" + wrong[1] + "-" + wrong[2]);
        }
    }

    @Test
    void theRejectionIsTheSharedOneAndSaysSoInPortuguese() {
        InvalidComponentException e = assertThrows(InvalidComponentException.class,
                () -> Dates.birthDate(1900, 2, 29));
        assertEquals(ValidationError.INVALID_COMPONENT, e.error());
        assertEquals("O número não contém uma data de nascimento válida.",
                Messages.forLocale(java.util.Locale.forLanguageTag("pt")).render(e));
    }
}
