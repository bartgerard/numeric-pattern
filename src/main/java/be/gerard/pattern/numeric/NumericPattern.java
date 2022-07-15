package be.gerard.pattern.numeric;

import be.gerard.pattern.numeric.internal.EmptySequence;
import be.gerard.pattern.numeric.internal.SortedSequence;
import be.gerard.pattern.numeric.internal.UnsortedSequence;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.Collections.emptySet;
import static java.util.Collections.indexOfSubList;
import static java.util.Collections.singleton;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toUnmodifiableMap;
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

    static <T extends Number> Set<Fit<T>> findAllPartialFits(
            final List<T> sequence
    ) {
        final Set<List<T>> allPossibleSubsequences = findAllPossibleSubsequences(sequence);

        return allPossibleSubsequences.stream()
                .map(subsequence -> Fit.of(
                        subsequence,
                        findShortestRepeatingSubsequence(subsequence)
                ))
                .filter(Fit::isPartialFit)
                .collect(toUnmodifiableSet());
    }

    static <T extends Number> Set<Fit<T>> findAllNonRepeatablePartialFits(
            final List<T> sequence
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
                        .map(longestSequence -> Fit.of(
                                longestSequence,
                                entry.getKey()
                        ))
                )
                .filter(Fit::isPartialFit)
                .collect(toUnmodifiableSet());
    }

    static <T extends Number> Set<List<T>> findAllBestFittingSubsequences(
            final List<T> sequence
    ) {
        final Set<Fit<T>> allNonRepeatablePartialFits = findAllNonRepeatablePartialFits(sequence);

        final Map<Double, Set<List<T>>> subsequencesByScore = allNonRepeatablePartialFits.stream()
                .collect(groupingBy(
                        Fit::compressionFactor,
                        mapping(Fit::subsequence, toUnmodifiableSet())
                ));

        return subsequencesByScore.entrySet()
                .stream()
                .max(Map.Entry.comparingByKey())
                .stream()
                .findFirst()
                .map(Map.Entry::getValue)
                .orElseGet(Collections::emptySet);
    }

    static <T extends Number> Set<NumericRange<Integer>> split(
            final List<T> sequence
    ) {
        if (sequence.isEmpty()) {
            return emptySet();
        }

        final Set<List<T>> allBestFittingSubsequences = findAllBestFittingSubsequences(sequence);

        if (allBestFittingSubsequences.isEmpty()) {
            return singleton(NumericRange.of(0, sequence.size() - 1));
        }

        final Map<List<T>, Set<Integer>> indicesByBestFittingSequence = allBestFittingSubsequences.stream()
                .collect(toUnmodifiableMap(
                        Function.identity(),
                        bestFit -> {
                            final Set<List<T>> allVariations = findAllVariations(bestFit);

                            return IntStream.rangeClosed(0, sequence.size() - bestFit.size())
                                    .filter(i -> allVariations.contains(sequence.subList(i, i + bestFit.size())))
                                    .flatMap(i -> IntStream.range(i, i + bestFit.size()))
                                    .boxed()
                                    .collect(toUnmodifiableSet());
                        }
                ));

        final Set<Integer> allHandledIndices = indicesByBestFittingSequence.values()
                .stream()
                .flatMap(Set::stream)
                .collect(toUnmodifiableSet());

        final List<NumericRange<Integer>> unhandledRanges = IntStream.range(0, sequence.size())
                .boxed()
                .filter(not(allHandledIndices::contains))
                .collect(collectingAndThen(
                        toUnmodifiableSet(),
                        NumericRange::groupSubsequentNumbers
                ));

        final List<NumericRange<Integer>> sets = unhandledRanges.stream()
                .flatMap(range -> split(sequence.subList(range.start(), range.end() + 1))
                        .stream()
                        .map(refinedSplit -> NumericRange.of(
                                range.start() + refinedSplit.start(),
                                range.start() + refinedSplit.end()
                        ))
                )
                .toList();

        final List<NumericRange<Integer>> handled = indicesByBestFittingSequence.values()
                .stream()
                .map(NumericRange::groupSubsequentNumbers)
                .flatMap(List::stream)
                .toList();

        return Stream.concat(
                        handled.stream(),
                        sets.stream()
                )
                .collect(toUnmodifiableSet());
    }

    static <T extends Number> Set<List<T>> filterRepeatedSubsequences(
            final Collection<List<T>> sequences
    ) {
        return sequences.stream()
                .filter(sequence1 -> sequences.stream()
                        .filter(sequence2 -> sequence2.size() > sequence1.size())
                        .noneMatch(sequence2 -> indexOfSubList(sequence2, sequence1) >= 0)
                )
                .collect(toUnmodifiableSet());
    }

    static <T extends Number> Set<List<T>> findAllVariations(
            final List<T> sequence
    ) {
        return IntStream.range(0, sequence.size())
                .mapToObj(i -> IntStream.range(0, sequence.size())
                        .mapToObj(j -> sequence.get((i + j) % sequence.size()))
                        .toList()
                )
                .collect(toUnmodifiableSet());
    }

    static <T extends Number> List<T> findBaseVariation(
            final List<T> sequence
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
            final List<T> sequence
    ) {
        return IntStream.range(0, sequence.size())
                .boxed()
                .flatMap(i -> IntStream.range(i, sequence.size())
                        .mapToObj(j -> sequence.subList(i, j + 1))
                )
                .collect(toUnmodifiableSet());
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

    default Set<List<T>> findAllPossibleSubsequences() {
        return findAllPossibleSubsequences(sequence());
    }

    List<T> sequence();

    boolean isSorted();

    List<? extends Pair<T, T>> findAllGaps();

    //boolean canBeCombinedWith(
    //        final NumericPattern<T> other
    //);

    default boolean startsWithSameSubsequence(
            final NumericPattern<T> other
    ) {
        return IntStream.range(0, Math.min(this.sequence().size(), other.sequence().size()))
                .allMatch(i -> Objects.equals(
                        this.sequence().get(i),
                        other.sequence().get(i)
                ));
    }

    default boolean endsWithSameSubsequence(
            final NumericPattern<T> other
    ) {
        return IntStream.rangeClosed(1, Math.min(this.sequence().size(), other.sequence().size()))
                .allMatch(i -> Objects.equals(
                        this.sequence().get(this.sequence().size() - i),
                        other.sequence().get(other.sequence().size() - i)
                ));
    }

    default NumericPattern<Long> deltas() {
        if (sequence().size() <= 1) {
            return empty();
        }

        final List<Long> interSequence = IntStream.range(1, sequence().size())
                .mapToObj(i -> sequence().get(i).longValue() - sequence().get(i - 1).longValue())
                .toList();

        return unsorted(interSequence);
    }

    default NumericPattern<Long> shortestRepeatingCycle() {
        final List<Long> deltas = deltas().sequence();
        final List<Long> subsequence = findShortestRepeatingSubsequence(deltas);

        return unsorted(subsequence);
    }

}
