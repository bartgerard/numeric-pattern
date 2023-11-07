package be.gerard.pattern.numeric;

import java.util.List;
import java.util.stream.IntStream;

import static java.util.Collections.indexOfSubList;
import static org.apache.commons.lang3.Validate.isTrue;
import static org.apache.commons.lang3.Validate.notEmpty;

public sealed interface Fit<T extends Number> permits Fit.Sequential, Fit.Incremental, Fit.None {

    static <T extends Number> Sequential<T> sequential(
            @Unsorted final List<T> sequence,
            @Unsorted final List<T> subsequence
    ) {
        return new Sequential<>(
                sequence,
                subsequence
        );
    }

    static <T extends Number> Incremental<T> incremental(
            @Sorted final List<T> sequence,
            final long increment
    ) {
        return new Incremental<>(
                sequence,
                increment
        );
    }

    static <T extends Number> None<T> none(
            @Sorted final List<T> sequence
    ) {
        return new None<>(
                sequence
        );
    }

    List<T> sequence();

    record Sequential<T extends Number>(
            @Unsorted List<T> sequence,
            @Unsorted List<T> subsequence
    ) implements Fit<T> {

        public Sequential {
            notEmpty(sequence);
            notEmpty(subsequence);

            isTrue(NumericPattern.findAllVariations(subsequence)
                    .stream()
                    .anyMatch(variation -> indexOfSubList(sequence, variation) >= 0)
            );
        }

        /**
         * @return A compression factor of 2, means that the sequence requires repeating the subsequence twice.
         */
        double compressionFactor() {
            return (double) sequence.size() / subsequence.size();
        }

        boolean isPartialFit() {
            return sequence.size() > subsequence.size(); // otherwise, it's not a PARTIAl fit.
        }

    }

    record Incremental<T extends Number>(
            @Sorted List<T> sequence,
            long increment
    ) implements Fit<T> {

        public Incremental {
            notEmpty(sequence);

            isTrue(IntStream.range(1, sequence.size())
                    .allMatch(i -> sequence.get(i).longValue() - sequence.get(i - 1).longValue() == increment)
            );
            isTrue(increment > 0);
        }

    }

    record None<T extends Number>(
            @Unsorted List<T> sequence
    ) implements Fit<T> {

        public None {
            notEmpty(sequence);
        }

    }

}
