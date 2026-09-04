package io.github.jefersonsantos06.stdnum.spi;

import java.util.Collection;

/**
 * Service provider interface that contributes {@link StdNum} instances to the
 * registry.
 *
 * <p>Each artifact registers <em>one</em> provider (not one per number type)
 * in {@code META-INF/services/io.github.jefersonsantos06.stdnum.spi.StdNumProvider},
 * returning all its number types from {@link #numbers()}. This keeps
 * {@code ServiceLoader} discovery cheap and works identically on the
 * classpath, the module path and in native images.</p>
 */
public interface StdNumProvider {

    /** All number types contributed by this artifact. */
    Collection<StdNum> numbers();
}
