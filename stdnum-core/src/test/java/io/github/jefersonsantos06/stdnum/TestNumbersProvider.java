package io.github.jefersonsantos06.stdnum;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidComponentException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.StdNumProvider;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.util.Collection;
import java.util.List;

/**
 * Registers two synthetic number types (country XX) so the registry can be
 * exercised without depending on any real country module.
 */
public final class TestNumbersProvider implements StdNumProvider {

    @Override
    public Collection<StdNum> numbers() {
        return List.of(AlphaNumber.INSTANCE, BetaNumber.INSTANCE);
    }

    /** Four digits, no checksum. */
    static final class AlphaNumber implements StdNum {

        static final AlphaNumber INSTANCE = new AlphaNumber();

        private static final Descriptor DESCRIPTOR =
                Descriptor.of("xx.alpha", "ALPHA")
                        .country("XX")
                        .title("Synthetic alpha number")
                        .tags(Tag.TAX)
                        .build();

        private AlphaNumber() {
        }

        @Override
        public Descriptor descriptor() {
            return DESCRIPTOR;
        }

        @Override
        public String compact(String number) {
            return Strings.compact(number, " ");
        }

        @Override
        public String validate(String number) {
            String n = compact(number);
            if (!Strings.isDigits(n)) {
                throw new InvalidFormatException();
            }
            if (n.length() != 4) {
                throw new InvalidLengthException();
            }
            return n;
        }
    }

    /** Six digits starting with 9, no checksum. */
    static final class BetaNumber implements StdNum {

        static final BetaNumber INSTANCE = new BetaNumber();

        private static final Descriptor DESCRIPTOR =
                Descriptor.of("xx.beta", "BETA")
                        .country("XX")
                        .title("Synthetic beta number")
                        .tags(Tag.COMPANY)
                        .build();

        private BetaNumber() {
        }

        @Override
        public Descriptor descriptor() {
            return DESCRIPTOR;
        }

        @Override
        public String compact(String number) {
            return Strings.compact(number, " ");
        }

        @Override
        public String validate(String number) {
            String n = compact(number);
            if (!Strings.isDigits(n)) {
                throw new InvalidFormatException();
            }
            if (n.length() != 6) {
                throw new InvalidLengthException();
            }
            if (n.charAt(0) != '9') {
                throw new InvalidComponentException();
            }
            return n;
        }
    }
}
