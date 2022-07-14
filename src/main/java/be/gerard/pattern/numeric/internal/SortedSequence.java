package be.gerard.pattern.numeric.internal;

import be.gerard.pattern.numeric.NumericPattern;
import be.gerard.pattern.numeric.SortedNumericPattern;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collection;
import java.util.List;
import java.util.stream.IntStream;

import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.Validate.notEmpty;

public record SortedSequence<T extends Number>(
        List<T> sequence
) implements SortedNumericPattern<T> {

    public static <T extends Number> SortedSequence<T> of(
            final Collection<T> sequence
    ) {
        notEmpty(sequence);

        final List<T> sortedSequence = sequence.stream()
                .distinct()
                .sorted()
                .toList();

        return new SortedSequence<>(sortedSequence);
    }

    public T min() {
        return sequence.get(0);
    }

    public T max() {
        return sequence.get(sequence.size() - 1);
    }

    @Override
    public List<? extends Pair<T, T>> findAllGaps() {
        if (sequence.size() <= 1) {
            return emptyList();
        }

        return IntStream.range(1, sequence.size())
                .filter(i -> sequence.get(i).longValue() - sequence.get(i - 1).longValue() > 1)
                .mapToObj(i -> ImmutablePair.of(
                        sequence.get(i - 1),
                        sequence.get(i)
                ))
                .toList();
    }

    @Override
    public boolean canReach(
            final T number
    ) {
        if (sequence().contains(number)) {
            return true;
        }

        final NumericPattern<Long> shortestRepeatingCycle = shortestRepeatingCycle();
        final long cycleLength = shortestRepeatingCycle.sequence()
                .stream()
                .mapToLong(Long::longValue)
                .sum();

        if (cycleLength == 0) {
            return false;
        }

        final long offsetForReachableCycle = number.longValue() / cycleLength - 1;
        final long startOfRepetition = offsetForReachableCycle * cycleLength + min().longValue();

        return IntStream.rangeClosed(0, shortestRepeatingCycle.sequence().size())
                .mapToLong(i -> IntStream.range(0, i)
                        .mapToLong(j -> shortestRepeatingCycle.sequence().get(j))
                        .sum()
                )
                .anyMatch(sum -> startOfRepetition + sum == number.longValue());
    }

}
