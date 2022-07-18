package be.gerard.pattern.numeric;

import java.util.List;

import static java.util.Collections.indexOfSubList;
import static org.apache.commons.lang3.Validate.isTrue;
import static org.apache.commons.lang3.Validate.notEmpty;

public record Fit<T extends Number>(
        List<T> sequence,
        List<T> subsequence
) implements UnsortedNumericPattern<T> {

    public Fit {
        notEmpty(sequence);
        notEmpty(subsequence);

        isTrue(NumericPattern.findAllVariations(subsequence)
                .stream()
                .anyMatch(variation -> indexOfSubList(sequence, variation) >= 0)
        );
    }

    public static <T extends Number> Fit<T> of(
            final List<T> sequence,
            final List<T> subsequence
    ) {
        return new Fit<>(
                sequence,
                subsequence
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
