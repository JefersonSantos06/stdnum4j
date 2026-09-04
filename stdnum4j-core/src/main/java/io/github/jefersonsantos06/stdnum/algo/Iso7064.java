package io.github.jefersonsantos06.stdnum.algo;

import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.ValidationException;

/**
 * The ISO/IEC 7064 check character systems with a single check character.
 *
 * <p>The five classic variants collapse into two implementations:</p>
 * <ul>
 *   <li>{@link PureSystem} covers MOD 11-2 and MOD 37-2 (and any "mod M, 2"
 *       system by alphabet);</li>
 *   <li>{@link HybridSystem} covers MOD 11,10 and MOD 37,36 (and any
 *       "mod M+1, M" system by alphabet).</li>
 * </ul>
 *
 * <p>MOD 97-10, the pure system with <em>two</em> check digits, has different
 * arithmetic and lives in {@link Mod97}.</p>
 *
 * <p>In both systems a valid number, including its check character, folds to
 * a checksum of 1.</p>
 */
public final class Iso7064 {

    private Iso7064() {
    }

    /** ISO 7064 MOD 11-2: decimal payload, check character may be {@code 'X'}. */
    public static final PureSystem MOD_11_2 =
            new PureSystem(11, "0123456789X");

    /** ISO 7064 MOD 37-2: alphanumeric, check character may be {@code '*'}. */
    public static final PureSystem MOD_37_2 =
            new PureSystem(37, "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ*");

    /** ISO 7064 MOD 11,10: decimal payload and check digit. */
    public static final HybridSystem MOD_11_10 =
            new HybridSystem("0123456789");

    /** ISO 7064 MOD 37,36: alphanumeric payload and check character. */
    public static final HybridSystem MOD_37_36 =
            new HybridSystem("0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ");

    /**
     * A pure system with radix 2: the checksum folds as
     * {@code check = (2 * check + value) mod M}.
     */
    public static final class PureSystem {

        private final int modulus;
        private final String alphabet;

        /**
         * @param modulus  the system modulus M (a prime in the ISO variants)
         * @param alphabet character-to-value mapping by position; must have
         *                 exactly {@code modulus} characters
         */
        public PureSystem(int modulus, String alphabet) {
            if (modulus < 2) {
                throw new IllegalArgumentException("modulus must be >= 2: " + modulus);
            }
            if (alphabet == null || alphabet.length() != modulus) {
                throw new IllegalArgumentException(
                        "alphabet must have exactly modulus (" + modulus + ") characters");
            }
            this.modulus = modulus;
            this.alphabet = alphabet;
        }

        /**
         * The checksum; valid numbers (including the check character) yield 1.
         *
         * @throws InvalidFormatException on {@code null}, empty input or
         *                                characters outside the alphabet
         */
        public int checksum(String number) {
            if (number == null || number.isEmpty()) {
                throw new InvalidFormatException();
            }
            int check = 0;
            for (int i = 0; i < number.length(); i++) {
                int value = alphabet.indexOf(number.charAt(i));
                if (value < 0) {
                    throw new InvalidFormatException();
                }
                check = (2 * check + value) % modulus;
            }
            return check;
        }

        /** The check character to append to {@code number} to make it valid. */
        public char calcCheckDigit(String number) {
            return alphabet.charAt(Math.floorMod(1 - 2 * checksum(number), modulus));
        }

        /**
         * Checks that the number (including its check character) folds to 1
         * and returns it unchanged.
         *
         * @throws InvalidChecksumException if the checksum does not match
         */
        public String validate(String number) {
            if (checksum(number) != 1) {
                throw new InvalidChecksumException();
            }
            return number;
        }

        /** Whether the number passes the check. Never throws. */
        public boolean isValid(String number) {
            try {
                validate(number);
                return true;
            } catch (ValidationException e) {
                return false;
            }
        }
    }

    /**
     * A hybrid system "mod M+1, M": intermediate sums are taken mod M
     * (with 0 promoted to M) and doubled mod M+1.
     */
    public static final class HybridSystem {

        private final int modulus;
        private final String alphabet;

        /**
         * @param alphabet character-to-value mapping by position; its length is
         *                 the system modulus M and must be even (as in both ISO
         *                 variants, M = 10 and M = 36)
         */
        public HybridSystem(String alphabet) {
            if (alphabet == null || alphabet.length() < 2) {
                throw new IllegalArgumentException("alphabet must have at least 2 characters");
            }
            if (alphabet.length() % 2 != 0) {
                throw new IllegalArgumentException(
                        "alphabet length (the modulus) must be even: " + alphabet.length());
            }
            this.modulus = alphabet.length();
            this.alphabet = alphabet;
        }

        /**
         * The checksum; valid numbers (including the check character) yield 1.
         *
         * @throws InvalidFormatException on {@code null}, empty input or
         *                                characters outside the alphabet
         */
        public int checksum(String number) {
            if (number == null || number.isEmpty()) {
                throw new InvalidFormatException();
            }
            // seeded with M/2 so the first doubling yields exactly M (M is even)
            int check = modulus / 2;
            for (int i = 0; i < number.length(); i++) {
                int value = alphabet.indexOf(number.charAt(i));
                if (value < 0) {
                    throw new InvalidFormatException();
                }
                int promoted = (check == 0) ? modulus : check;
                check = ((2 * promoted) % (modulus + 1) + value) % modulus;
            }
            return check;
        }

        /** The check character to append to {@code number} to make it valid. */
        public char calcCheckDigit(String number) {
            int checksum = checksum(number);
            int promoted = (checksum == 0) ? modulus : checksum;
            return alphabet.charAt(
                    Math.floorMod(1 - (2 * promoted) % (modulus + 1), modulus));
        }

        /**
         * Checks that the number (including its check character) folds to 1
         * and returns it unchanged.
         *
         * @throws InvalidChecksumException if the checksum does not match
         */
        public String validate(String number) {
            if (checksum(number) != 1) {
                throw new InvalidChecksumException();
            }
            return number;
        }

        /** Whether the number passes the check. Never throws. */
        public boolean isValid(String number) {
            try {
                validate(number);
                return true;
            } catch (ValidationException e) {
                return false;
            }
        }
    }
}
