package be.gerard.pattern.numeric.internal;

import be.gerard.pattern.numeric.UnsortedNumericPattern;

import java.util.Collection;
import java.util.List;

import static org.apache.commons.lang3.Validate.notEmpty;

public record UnsortedSequence<T extends Number>(
        List<T> sequence
) implements UnsortedNumericPattern<T> {

    public static <T extends Number> SortedSequence<T> of(
            final Collection<T> sequence
    ) {
        notEmpty(sequence);

        return new SortedSequence<>(List.copyOf(sequence));
    }

}
