package be.gerard.pattern.numeric;

import be.gerard.pattern.numeric.internal.EmptySequence;
import be.gerard.pattern.numeric.internal.SortedSequence;
import be.gerard.pattern.numeric.internal.UnsortedSequence;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.Collections.indexOfSubList;
import static java.util.Collections.singleton;
import static java.util.Comparator.comparing;
import static java.util.Comparator.comparingInt;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toUnmodifiableSet;

public interface NumericPattern<T extends Number> {

    static <T extends Number> SortedNumericPattern<T> sorted(
            final Collection<T> sequence
    ) {
        if (sequence.isEmpty()) {
            return empty();
        }

        return SortedSequence.of(sequence);
    }

    static <T extends Number> SortedNumericPattern<T> empty() {
        return new EmptySequence<>();
    }

    static <T extends Number> NumericPattern<T> unsorted(
            final Collection<T> sequence
    ) {
        if (sequence.isEmpty()) {
            return empty();
        }

        return UnsortedSequence.of(sequence);
    }

    static <T extends Number> Set<Fit.Sequential<T>> findAllPartialFits(
            @Unsorted final List<T> sequence
    ) {
        final Set<List<T>> allPossibleSubsequences = findAllPossibleSubsequences(sequence);

        return allPossibleSubsequences.stream()
                .map(subsequence -> Fit.sequential(
                        subsequence,
                        findShortestRepeatingSubsequence(subsequence)
                ))
                .filter(Fit.Sequential::isPartialFit)
                .collect(toUnmodifiableSet());
    }

    static <T extends Number> Set<Fit.Sequential<T>> findAllNonRepeatablePartialFits(
            @Unsorted final List<T> sequence
    ) {
        final Set<List<T>> allPossibleSubsequences = findAllPossibleSubsequences(sequence);

        final Map<List<T>, Set<List<T>>> longestSequencesByRepeatingSubsequence = allPossibleSubsequences.stream()
                .collect(groupingBy(
                        subsequence -> findBaseVariation(findShortestRepeatingSubsequence(subsequence)),
                        collectingAndThen(
                                toUnmodifiableSet(),
                                NumericPattern::filterRepeatedSubsequences
                        )
                ));

        return longestSequencesByRepeatingSubsequence.entrySet()
                .stream()
                .flatMap(entry -> entry.getValue()
                        .stream()
                        .map(longestSequence -> Fit.sequential(
                                longestSequence,
                                entry.getKey()
                        ))
                )
                .filter(Fit.Sequential::isPartialFit)
                .collect(toUnmodifiableSet());
    }

    static <T extends Number> Set<List<T>> findAllBestFittingSubsequences(
            @Unsorted final List<T> sequence
    ) {
        final Set<Fit.Sequential<T>> allNonRepeatablePartialFits = findAllNonRepeatablePartialFits(sequence);

        final Map<Double, Set<List<T>>> subsequencesByScore = allNonRepeatablePartialFits.stream()
                .collect(groupingBy(
                        Fit.Sequential::compressionFactor,
                        mapping(Fit.Sequential::subsequence, toUnmodifiableSet())
                ));

        return subsequencesByScore.entrySet()
                .stream()
                .max(Map.Entry.comparingByKey())
                .stream()
                .findFirst()
                .map(Map.Entry::getValue)
                .orElseGet(Collections::emptySet);
    }

    static <T extends Number> Set<NumericRange<Integer>> splitByMostLikelyPattern(
            @Unsorted final List<T> sequence
    ) {
        if (sequence.isEmpty()) {
            return emptySet();
        }

        final Set<List<T>> allBestFittingSubsequences = findAllBestFittingSubsequences(sequence);

        if (allBestFittingSubsequences.isEmpty()) {
            return singleton(NumericRange.of(0, sequence.size() - 1));
        }

        final List<? extends Pair<List<T>, List<Integer>>> bestFittingPairs = findAllFittingRepeatableSubsequencesWithTheirRepetitions(
                sequence,
                allBestFittingSubsequences
        );

        final List<NumericRange<Integer>> ranges = new ArrayList<>();
        final List<? extends Pair<List<T>, List<Integer>>> remainingPairs = new ArrayList<>(bestFittingPairs);
        final List<Integer> remainingIndices = IntStream.range(0, sequence.size())
                .boxed()
                .collect(toList());

        while (!remainingPairs.isEmpty()) {
            final Pair<List<T>, List<Integer>> pair = Collections.max(
                    remainingPairs,
                    comparing(remainingPair -> remainingPair.getRight()
                            .stream()
                            .filter(remainingIndices::contains)
                            .count()
                    )
            );
            remainingPairs.remove(pair);

            final List<Integer> matchingIndices = pair.getRight()
                    .stream()
                    .filter(remainingIndices::contains)
                    .toList();

            remainingIndices.removeAll(matchingIndices);

            ranges.addAll(NumericRange.groupSubsequentNumbers(matchingIndices));
        }

        final List<NumericRange<Integer>> unhandledRanges = NumericRange.groupSubsequentNumbers(remainingIndices);

        final List<NumericRange<Integer>> additionalRanges = unhandledRanges.stream()
                .flatMap(range -> splitByMostLikelyPattern(sequence.subList(range.start(), range.end() + 1))
                        .stream()
                        .map(refinedSplit -> NumericRange.of(
                                range.start() + refinedSplit.start(),
                                range.start() + refinedSplit.end()
                        ))
                )
                .toList();

        return Stream.concat(
                        ranges.stream(),
                        additionalRanges.stream()
                )
                .collect(toUnmodifiableSet());
    }

    static <T extends Number> Optional<? extends Pair<List<T>, List<Integer>>> findTheRepeatableSubsequenceWithTheLongestFittingRepetitionStartingFromLeft(
            @Unsorted final List<T> sequence,
            final Collection<List<T>> possibleRepeatableSubsequences
    ) {
        return possibleRepeatableSubsequences.stream()
                .flatMap(repeatableSubSequence -> IntStream.range(0, repeatableSubSequence.size())
                        .mapToObj(shift -> IntStream.range(0, sequence.size())
                                .takeWhile(index -> Objects.equals(sequence.get(index), repeatableSubSequence.get((index + shift) % repeatableSubSequence.size())))
                                .boxed()
                                .toList()
                        )
                        .filter(not(List::isEmpty))
                        .max(comparingInt(List::size))
                        .map(longestFittingSequence -> ImmutablePair.of(repeatableSubSequence, longestFittingSequence))
                        .stream()
                )
                .max(Comparator.<Pair<List<T>, List<Integer>>, List<Integer>>comparing(Pair::getRight, comparingInt(List::size))
                        .thenComparing(Pair::getLeft, comparingInt(List::size))
                );
    }

    static <T extends Number> List<? extends Pair<List<T>, List<Integer>>> findAllFittingRepeatableSubsequencesWithTheirRepetitions(
            @Unsorted final List<T> sequence,
            final Collection<List<T>> possibleRepeatableSubsequences
    ) {
        final List<? extends Pair<List<T>, List<Integer>>> longestFittingPairsForAllSubsequences = IntStream.range(0, sequence.size())
                .mapToObj(fromIndex -> {
                    final List<T> subsequence = sequence.subList(fromIndex, sequence.size());

                    return findTheRepeatableSubsequenceWithTheLongestFittingRepetitionStartingFromLeft(subsequence, possibleRepeatableSubsequences)
                            .map(fit -> ImmutablePair.of(
                                    fit.getLeft(),
                                    fit.getRight()
                                            .stream()
                                            .map(i -> fromIndex + i)
                                            .toList()
                            ));
                })
                .flatMap(Optional::stream)
                .toList();

        return longestFittingPairsForAllSubsequences.stream()
                .filter(pair1 -> longestFittingPairsForAllSubsequences.stream()
                        .filter(pair2 -> pair1.getRight().size() < pair2.getRight().size())
                        .noneMatch(pair2 -> indexOfSubList(pair2.getRight(), pair1.getRight()) > 0)
                )
                .toList();
    }

    static <T extends Number> Set<List<T>> filterRepeatedSubsequences(
            @Unsorted final Collection<List<T>> sequences
    ) {
        return sequences.stream()
                .filter(sequence1 -> sequences.stream()
                        .filter(sequence2 -> sequence2.size() > sequence1.size())
                        .noneMatch(sequence2 -> indexOfSubList(sequence2, sequence1) >= 0)
                )
                .collect(toUnmodifiableSet());
    }

    static <T extends Number> Set<List<T>> findAllVariations(
            @Unsorted final List<T> sequence
    ) {
        return IntStream.range(0, sequence.size())
                .mapToObj(i -> IntStream.range(0, sequence.size())
                        .mapToObj(j -> sequence.get((i + j) % sequence.size()))
                        .toList()
                )
                .collect(toUnmodifiableSet());
    }

    static <T extends Number> List<T> findBaseVariation(
            @Unsorted final List<T> sequence
    ) {
        final Set<List<T>> allVariations = findAllVariations(sequence);

        return allVariations.stream()
                .min((s1, s2) -> {
                    for (int i = 0; i < Math.min(s1.size(), s2.size()); i++) {
                        final int comparison = Long.compare(s1.get(i).longValue(), s2.get(i).longValue());

                        if (comparison != 0) {
                            return comparison;
                        }
                    }

                    return 0;
                })
                .orElseGet(Collections::emptyList);
    }

    static <T extends Number> Set<List<T>> findAllPossibleSubsequences(
            @Unsorted final List<T> sequence
    ) {
        return IntStream.range(0, sequence.size())
                .boxed()
                .flatMap(i -> IntStream.range(i, sequence.size())
                        .mapToObj(j -> sequence.subList(i, j + 1))
                )
                .collect(toUnmodifiableSet());
    }

    static <T extends Number> Stream<List<T>> findAllRepeatingSubsequences(
            @Unsorted final List<T> sequence
    ) {
        return IntStream.range(0, sequence.size())
                .mapToObj(i -> sequence.subList(0, i + 1))
                .filter(subsequence -> IntStream.range(0, sequence.size())
                        .allMatch(i -> Objects.equals(
                                sequence.get(i),
                                subsequence.get(i % subsequence.size())
                        ))
                );
    }

    static <T extends Number> List<T> findShortestRepeatingSubsequence(
            @Unsorted final List<T> sequence
    ) {
        return findAllRepeatingSubsequences(sequence)
                .findFirst()
                .orElseGet(Collections::emptyList);
    }

    default Set<List<T>> findAllPossibleSubsequences() {
        return findAllPossibleSubsequences(sequence());
    }

    List<T> sequence();

    boolean isSorted();

    List<? extends Pair<T, T>> findAllGaps();

    default int size() {
        return sequence().size();
    }

    default T first() {
        return sequence().get(0);
    }

    default T last() {
        return sequence().get(size() - 1);
    }

    default List<Long> intervals() {
        if (size() <= 1) {
            return emptyList();
        }

        return IntStream.range(1, size())
                .mapToObj(i -> sequence().get(i).longValue() - sequence().get(i - 1).longValue())
                .toList();
    }

    default Set<Long> findDistinctCombinatorialIncrements(
            final Number maxLength
    ) {
        if (size() <= 1) {
            return emptySet();
        }

        return IntStream.range(0, size() - 1)
                .boxed()
                .flatMap(i -> IntStream.range(i + 1, size())
                        .mapToObj(j -> sequence().get(j).longValue() - sequence().get(i).longValue())
                        .takeWhile(j -> j <= maxLength.longValue())
                )
                .collect(toUnmodifiableSet());
    }

    //boolean canBeCombinedWith(
    //        final NumericPattern<T> other
    //);

    default boolean startsWithSameSubsequence(
            final NumericPattern<T> other
    ) {
        return IntStream.range(0, Math.min(this.size(), other.size()))
                .allMatch(i -> Objects.equals(
                        this.sequence().get(i),
                        other.sequence().get(i)
                ));
    }

    default boolean endsWithSameSubsequence(
            final NumericPattern<T> other
    ) {
        return IntStream.rangeClosed(1, Math.min(this.size(), other.size()))
                .allMatch(i -> Objects.equals(
                        this.sequence().get(this.size() - i),
                        other.sequence().get(other.size() - i)
                ));
    }

    default NumericPattern<Long> deltas() {
        if (size() <= 1) {
            return empty();
        }

        final List<Long> interSequence = IntStream.range(1, size())
                .mapToObj(i -> sequence().get(i).longValue() - sequence().get(i - 1).longValue())
                .toList();

        return unsorted(interSequence);
    }

    default Stream<NumericPattern<Long>> allRepeatingCycles() {
        final List<Long> deltas = deltas().sequence();
        return findAllRepeatingSubsequences(deltas)
                .map(NumericPattern::unsorted);
    }

    default NumericPattern<Long> shortestRepeatingCycle() {
        return allRepeatingCycles()
                .findFirst()
                .orElseGet(NumericPattern::empty);
    }

}
