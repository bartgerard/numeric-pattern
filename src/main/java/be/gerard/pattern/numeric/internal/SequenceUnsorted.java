package be.gerard.pattern.numeric.internal;

import be.gerard.pattern.numeric.UnsortedNumericPattern;

import java.util.Collection;
import java.util.List;

import static org.apache.commons.lang3.Validate.notEmpty;

public record SequenceUnsorted<T extends Number>(
        List<T> sequence
) implements UnsortedNumericPattern<T> {

    public static <T extends Number> SequenceSorted<T> of(
            final Collection<T> sequence
    ) {
        notEmpty(sequence);

        return new SequenceSorted<>(List.copyOf(sequence));
    }

}
