package be.gerard.pattern.numeric;

import be.gerard.pattern.numeric.internal.EmptySequence;
import be.gerard.pattern.numeric.internal.SortedSequence;
import be.gerard.pattern.numeric.internal.UnsortedSequence;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

public interface NumericPattern<T extends Number> {

    List<T> sequence();

    boolean isSorted();

    static <T extends Number> SortedNumericPattern<T> empty() {
        return new EmptySequence<>();
    }

    static <T extends Number> SortedNumericPattern<T> sorted(
            final Collection<T> sequence
    ) {
        if (sequence.isEmpty()) {
            return empty();
        }

        return SortedSequence.of(sequence);
    }

    static <T extends Number> NumericPattern<T> unsorted(
            final Collection<T> sequence
    ) {
        if (sequence.isEmpty()) {
            return empty();
        }

        return UnsortedSequence.of(sequence);
    }

    List<? extends Pair<T, T>> findAllGaps();

    default boolean hasSameStartSequenceAs(
            final NumericPattern<T> other
    ) {
        return IntStream.range(0, Math.min(this.sequence().size(), other.sequence().size()))
                .allMatch(i -> Objects.equals(
                        this.sequence().get(i),
                        other.sequence().get(i)
                ));
    }

    default boolean hasSameEndSequenceAs(
            final NumericPattern<T> other
    ) {
        return IntStream.rangeClosed(1, Math.min(this.sequence().size(), other.sequence().size()))
                .allMatch(i -> Objects.equals(
                        this.sequence().get(this.sequence().size() - i),
                        other.sequence().get(other.sequence().size() - i)
                ));
    }

    //boolean canBeCombinedWith(
    //        final NumericPattern<T> other
    //);

    default NumericPattern<Long> deltas() {
        if (sequence().size() <= 1) {
            return empty();
        }

        final List<Long> interSequence = IntStream.range(1, sequence().size())
                .mapToObj(i -> sequence().get(i).longValue() - sequence().get(i - 1).longValue())
                .toList();

        return unsorted(interSequence);
    }

    static <T extends Number> List<T> findShortestRepeatingSubsequence(
            final List<T> sequence
    ) {
        return IntStream.range(0, sequence.size())
                .mapToObj(i -> sequence.subList(0, i + 1))
                .filter(subsequence -> IntStream.range(0, sequence.size())
                        .allMatch(i -> Objects.equals(
                                sequence.get(i),
                                subsequence.get(i % subsequence.size())
                        ))
                )
                .findFirst()
                .orElseGet(Collections::emptyList);
    }

    default NumericPattern<Long> shortestRepeatingCycle() {
        final List<Long> deltas = deltas().sequence();
        final List<Long> subsequence = findShortestRepeatingSubsequence(deltas);

        return unsorted(subsequence);
    }

}
