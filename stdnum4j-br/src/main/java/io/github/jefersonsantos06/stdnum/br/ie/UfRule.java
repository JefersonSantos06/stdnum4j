package io.github.jefersonsantos06.stdnum.br.ie;

/**
 * Validation rule of one federative unit. Receives the already compacted,
 * upper-cased number; returns it when valid, throws a
 * {@code ValidationException} subtype otherwise.
 */
@FunctionalInterface
interface UfRule {

    String validate(String number);
}
