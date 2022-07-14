package be.gerard.pattern.numeric.internal;

import be.gerard.pattern.numeric.NumericPattern;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.IntStream;

import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.Validate.notEmpty;

public record UnsortedSequence<T extends Number>(
        List<T> sequence
) implements NumericPattern<T> {

    public static <T extends Number> SortedSequence<T> of(
            final Collection<T> sequence
    ) {
        notEmpty(sequence);

        return new SortedSequence<>(new ArrayList<>(sequence));
    }

    @Override
    public boolean isSorted() {
        return false;
    }

    @Override
    public List<? extends Pair<T, T>> findAllGaps() {
        if (sequence.size() <= 1) {
            return emptyList();
        }

        return IntStream.range(1, sequence.size())
                .filter(i -> Math.abs(sequence.get(i).longValue() - sequence.get(i - 1).longValue()) != 1)
                .mapToObj(i -> ImmutablePair.of(
                        sequence.get(i - 1),
                        sequence.get(i)
                ))
                .toList();
    }

}
