package be.gerard.pattern.numeric.internal;

import be.gerard.pattern.numeric.Fit;
import be.gerard.pattern.numeric.NumericPattern;
import be.gerard.pattern.numeric.SortedNumericPattern;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.partitioningBy;
import static java.util.stream.Collectors.toUnmodifiableList;
import static java.util.stream.Collectors.toUnmodifiableSet;
import static org.apache.commons.lang3.Validate.notEmpty;

public record SequenceSorted<T extends Number>(
        List<T> sequence
) implements SortedNumericPattern<T> {

    public static <T extends Number> SequenceSorted<T> of(
            final Collection<T> sequence
    ) {
        notEmpty(sequence);

        final List<T> sortedSequence = sequence.stream()
                .distinct()
                .sorted()
                .toList();

        return new SequenceSorted<>(sortedSequence);
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

        return IntStream.rangeClosed(0, shortestRepeatingCycle.size())
                .mapToLong(i -> IntStream.range(0, i)
                        .mapToLong(j -> shortestRepeatingCycle.sequence().get(j))
                        .sum()
                )
                .anyMatch(sum -> startOfRepetition + sum == number.longValue());
    }

    @Override
    public List<List<T>> splitDeviatingIncrements(
            final Number increment
    ) {
        final int[] innerDeviatingIndices = IntStream.range(1, sequence.size())
                .filter(i -> sequence.get(i).longValue() - sequence.get(i - 1).longValue() != increment.longValue())
                .toArray();

        final int[] allDeviatingIndices = IntStream.concat(
                        IntStream.of(0, sequence.size()),
                        Arrays.stream(innerDeviatingIndices)
                )
                .sorted()
                .toArray();

        return IntStream.range(1, allDeviatingIndices.length)
                .mapToObj(i -> sequence.subList(
                        allDeviatingIndices[i - 1],
                        allDeviatingIndices[i]
                ))
                .toList();
    }

    @Override
    public Set<Fit<T>> groupCommonIncrements(final Number maxIncrement) {
        final Set<Long> increments = findDistinctCombinatorialIncrements(
                maxIncrement
        );

        if (increments.isEmpty()) {
            return Set.of(Fit.none(sequence));
        }

        final long smallestIncrement = Collections.min(increments); // Prefer simple patterns

        final Map<Long, SortedNumericPattern<T>> indicesByCommonRemainder = sequence.stream()
                .collect(groupingBy(
                        index -> index.longValue() % smallestIncrement,
                        collectingAndThen(
                                toUnmodifiableSet(),
                                NumericPattern::sorted
                        )
                ));

        final List<List<T>> groups = indicesByCommonRemainder.values()
                .stream()
                .map(pattern -> pattern.splitDeviatingIncrements(smallestIncrement))
                .flatMap(Collection::stream)
                .toList();

        final Map<Boolean, List<List<T>>> groupedByCompliance = groups.stream()
                .collect(partitioningBy(
                        group -> group.size() > 1,
                        toUnmodifiableList()
                ));

        if (groupedByCompliance.get(true).isEmpty()) {
            return Set.of(Fit.none(sequence));
        }

        final Set<Fit<T>> fits = groupedByCompliance.get(true)
                .stream()
                .map(group -> Fit.incremental(
                        group,
                        smallestIncrement
                ))
                .collect(toUnmodifiableSet());

        if (groupedByCompliance.get(false).isEmpty()) {
            return fits;
        }

        final List<T> deviations = groupedByCompliance.get(false)
                .stream()
                .map(group -> group.get(0))
                .toList();

        return Stream.concat(
                        fits.stream(),
                        NumericPattern.sorted(deviations)
                                .groupCommonIncrements(maxIncrement)
                                .stream()
                )
                .collect(toUnmodifiableSet());
    }

    @Override
    public Set<Fit<T>> groupCycles(
            final Number patternLength
    ) {
        return LongStream.range(1, patternLength.longValue() / 2 + 1)
                .filter(cycleLength -> patternLength.longValue() % cycleLength == 0)
                .boxed()
                .map(cycleLength -> groupByCycleLength(
                        patternLength,
                        cycleLength
                ))
                .filter(not(Set::isEmpty))
                .findFirst()
                .orElseGet(() -> Set.of(Fit.none(sequence)));
    }

    private Set<Fit<T>> groupByCycleLength(
            final Number patternLength,
            final Long cycleLength
    ) {
        final Map<Long, List<T>> groups = sequence.stream()
                .collect(groupingBy(
                        i -> i.longValue() % cycleLength,
                        toUnmodifiableList()
                ));
        final Map<Boolean, List<List<T>>> sequencesByCycleWorthiness = groups.values()
                .stream()
                .collect(partitioningBy(
                        group -> group.size() * cycleLength == patternLength.longValue()
                ));

        if (sequencesByCycleWorthiness.get(true).isEmpty()) {
            return Collections.emptySet();
        }

        return Stream.concat(
                        sequencesByCycleWorthiness.get(true)
                                .stream()
                                .map(group -> Fit.incremental(
                                        group,
                                        cycleLength
                                )),
                        sequencesByCycleWorthiness.get(false)
                                .stream()
                                .flatMap(List::stream)
                                .collect(collectingAndThen(
                                        toUnmodifiableList(),
                                        NumericPattern::sorted
                                ))
                                .groupCycles(patternLength)
                                .stream()
                )
                .collect(toUnmodifiableSet());
    }

    public T min() {
        return sequence.get(0);
    }

}
