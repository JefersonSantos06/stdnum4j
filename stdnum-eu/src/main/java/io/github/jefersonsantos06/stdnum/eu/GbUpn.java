package io.github.jefersonsantos06.stdnum.eu;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidComponentException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.util.Locale;
import java.util.Set;

/**
 * UPN, the number that follows a pupil through the English school system: a
 * leading check letter, the local authority that issued it, the school, the
 * year and a serial.
 *
 * <p>The alphabet leaves out I, O and S, which is why the check letter is a
 * mod 23 rather than a mod 26.</p>
 */
public final class GbUpn implements StdNum {

    public static final GbUpn INSTANCE = new GbUpn();

    private static final String ALPHABET = "ABCDEFGHJKLMNPQRTUVWXYZ0123456789";

    /** The local authorities that issue numbers. */
    private static final Set<Integer> AUTHORITIES = Set.of(
            201, 202, 203, 204, 205, 206, 207, 208, 209, 210, 211, 212, 213, 301,
            302, 303, 304, 305, 306, 307, 308, 309, 310, 311, 312, 313, 314, 315,
            316, 317, 318, 319, 320, 330, 331, 332, 333, 334, 335, 336, 340, 341,
            342, 343, 344, 350, 351, 352, 353, 354, 355, 356, 357, 358, 359, 370,
            371, 372, 373, 380, 381, 382, 383, 384, 390, 391, 392, 393, 394, 420,
            800, 801, 802, 803, 805, 806, 807, 808, 810, 811, 812, 813, 815, 816,
            821, 822, 823, 825, 826, 830, 831, 835, 836, 837, 840, 841, 845, 846,
            850, 851, 852, 855, 856, 857, 860, 861, 865, 866, 867, 868, 869, 870,
            871, 872, 873, 874, 876, 877, 878, 879, 880, 881, 882, 883, 884, 885,
            886, 887, 888, 889, 890, 891, 892, 893, 894, 895, 896, 908, 909, 916,
            919, 921, 925, 926, 928, 929, 931, 933, 935, 936, 937, 938);

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("gb.upn", "UPN")
                    .country("GB")
                    .title("Unique Pupil Number")
                    .description("Number following a pupil through the English school system:"
                            + " a check letter, a local authority, a school, a year and a serial.")
                    .tags(Tag.PERSON, Tag.EDUCATION)
                    .references("https://www.gov.uk/government/publications/"
                            + "unique-pupil-numbers")
                    .build();

    private GbUpn() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " ").toUpperCase(Locale.ROOT);
    }

    /** The leading check letter, from the twelve characters that follow it. */
    public static char calcCheckDigit(String base) {
        String n = INSTANCE.compact(base);
        int check = 0;
        for (int i = 0; i < n.length(); i++) {
            int value = ALPHABET.indexOf(n.charAt(i));
            if (value < 0) {
                throw new InvalidFormatException();
            }
            check += (i + 2) * value;
        }
        return ALPHABET.charAt(check % 23);
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (n.length() != 13) {
            throw new InvalidLengthException();
        }
        if (!Strings.isDigits(n.substring(1, 12)) || ALPHABET.indexOf(n.charAt(12)) < 0) {
            throw new InvalidFormatException();
        }
        if (!AUTHORITIES.contains(Integer.parseInt(n.substring(1, 4)))) {
            throw new InvalidComponentException("Not the code of a local authority.");
        }
        if (n.charAt(0) != calcCheckDigit(n.substring(1))) {
            throw new InvalidChecksumException();
        }
        return n;
    }
}
