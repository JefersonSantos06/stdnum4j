package io.github.jefersonsantos06.stdnum.br;

/** Shared check for numbers made of a single repeated character. */
final class Repeats {

    private Repeats() {
    }

    static boolean allSame(String s) {
        for (int i = 1; i < s.length(); i++) {
            if (s.charAt(i) != s.charAt(0)) {
                return false;
            }
        }
        return !s.isEmpty();
    }
}
