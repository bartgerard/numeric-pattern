package be.gerard.pattern.numeric

import org.apache.commons.lang3.tuple.Pair
import spock.lang.Specification
import spock.lang.Title

import static NumericPatternTestUtils.pair
import static NumericPatternTestUtils.toLongValues
import static be.gerard.pattern.numeric.NumericPatternTestUtils.range
import static be.gerard.pattern.numeric.NumericPatternTestUtils.range1
import static org.assertj.core.api.Assertions.assertThat

@Title("NumericPattern Tests")
class NumericPatternSpecification extends Specification {

    def "compare start and end subsequences"() {

        given:
        NumericPattern<Integer> pattern1 = NumericPattern.sorted(sequence1)
        NumericPattern<Integer> pattern2 = NumericPattern.sorted(sequence2)

        when:
        boolean hasSameStartSequence = pattern1.startsWithSameSubsequence(pattern2)
        boolean hasSameEndSequence = pattern1.endsWithSameSubsequence(pattern2)

        then:
        hasSameStartSequence == isSameStartSequence
        hasSameEndSequence == isSameEndSequence

        where:
        sequence1 | sequence2 | isSameStartSequence | isSameEndSequence | comment
        []        | []        | true                | true              | ""

        [1]       | [1]       | true                | true              | ""
        [1, 2]    | [1, 2]    | true                | true              | ""
        [1, 2, 3] | [1, 2, 3] | true                | true              | ""

        [1, 2, 3] | [1]       | true                | false             | ""
        [1, 2, 3] | [1, 2]    | true                | false             | ""

        [1, 2, 3] | [3]       | false               | true              | ""
        [1, 2, 3] | [2, 3]    | false               | true              | ""

        [1, 2, 3] | [2]       | false               | false             | ""

    }

    def "find all gaps for a sorted sequence"() {

        given:
        NumericPattern<Integer> pattern = NumericPattern.sorted(sequence)

        when:
        List<Pair<Integer, Integer>> gaps = pattern.findAllGaps();

        then:
        assertThat(gaps).containsExactlyElementsOf(expectedGaps)

        where:
        sequence           | expectedGaps             | comment
        []                 | []                       | ""
        [0]                | []                       | ""
        [1]                | []                       | ""

        [0, 1]             | []                       | ""
        [0, 1, 2]          | []                       | ""
        [0, 1, 2, 3]       | []                       | ""
        [0, 1, 2, 3, 4]    | []                       | ""
        [0, 1, 2, 3, 4, 5] | []                       | ""

        [0, 2]             | [pair(0, 2)]             | ""
        [0, 2, 3]          | [pair(0, 2)]             | ""
        [0, 2, 4]          | [pair(0, 2), pair(2, 4)] | ""
        [0, 2, 3, 5]       | [pair(0, 2), pair(3, 5)] | ""

    }

    def "find shortest repeating subsequence"() {

        when:
        List<Integer> shortestRepeatingSubsequence = NumericPattern.findShortestRepeatingSubsequence(sequence);

        then:
        assertThat(shortestRepeatingSubsequence).containsExactlyElementsOf(expectedShortestRepeatingSubsequence)

        where:
        sequence                           | expectedShortestRepeatingSubsequence | comment
        []                                 | []                                   | ""
        [0]                                | [0]                                  | ""
        [1]                                | [1]                                  | ""

        [0, 1]                             | [0, 1]                               | ""
        [0, 1, 0]                          | [0, 1]                               | ""
        [0, 1, 0, 1]                       | [0, 1]                               | ""
        [0, 1, 0, 1, 0]                    | [0, 1]                               | ""

        [0, 1, 0, 2]                       | [0, 1, 0, 2]                         | ""

        [1, 1, 2, 3, 5, 8, 13, 21, 34, 55] | [1, 1, 2, 3, 5, 8, 13, 21, 34, 55]   | "Fibonacci sequence"

    }

    def "find shortest repeating cycle"() {

        given:
        NumericPattern<Integer> pattern = NumericPattern.sorted(sequence)

        when:
        NumericPattern<Long> shortestRepeatingCycle = pattern.shortestRepeatingCycle();

        then:
        assertThat(shortestRepeatingCycle.sequence()).containsExactlyElementsOf(toLongValues(expectedShortestRepeatingCycle))

        where:
        sequence                        | expectedShortestRepeatingCycle | comment
        []                              | []                             | ""
        [0]                             | []                             | ""

        [0, 1]                          | [1]                            | ""
        [0, 1, 2]                       | [1]                            | ""
        [0, 1, 2, 3]                    | [1]                            | ""
        [0, 1, 2, 3, 4]                 | [1]                            | ""

        [2, 4]                          | [2]                            | ""
        [2, 4, 6]                       | [2]                            | ""

        [0, 1, 3]                       | [1, 2]                         | ""
        [0, 1, 3, 4]                    | [1, 2]                         | ""
        [0, 1, 3, 4, 6]                 | [1, 2]                         | ""
        [0, 1, 3, 4, 6, 7]              | [1, 2]                         | ""

        [1, 2, 3, 5, 8, 13, 21, 34, 55] | [1, 1, 2, 3, 5, 8, 13, 21]     | "Fibonacci sequence"

    }

    def "can reach number"() {

        given:
        SortedNumericPattern<Integer> pattern = NumericPattern.sorted(sequence)

        when:
        boolean result = pattern.canReach(number);

        then:
        result == expected

        where:
        sequence     | number | expected | comment
        [2, 4]       | 0      | true     | ""
        [2, 4, 6]    | 0      | true     | ""

        [2, 4]       | -2     | true     | ""
        [2, 4, 6]    | -2     | true     | ""

        [1, 3]       | 0      | false    | ""
        [1, 3, 5]    | 0      | false    | ""

        [1, 3]       | -1     | true     | ""
        [1, 3, 5]    | -1     | true     | ""

        [1, 2, 4]    | -1     | true     | ""
        [1, 2, 4, 5] | -1     | true     | ""

        [1, 2, 4, 7] | -1     | false    | ""

    }

    def "find all possible subsequences"() {

        given:
        NumericPattern<Integer> pattern = NumericPattern.unsorted(sequence)

        when:
        Set<List<Integer>> allPossibleSubsequences = pattern.findAllPossibleSubsequences();

        then:
        assertThat(allPossibleSubsequences).containsExactlyInAnyOrderElementsOf(expectedSubsequences)

        where:
        sequence  | expectedSubsequences                  | comment
        []        | []                                    | ""
        [1]       | [[1]]                                 | ""
        [1, 2]    | [[1], [2], [1, 2]]                    | ""
        [1, 2, 1] | [[1], [2], [1, 2], [2, 1], [1, 2, 1]] | ""

    }

    def "find all partial fits"() {

        when:
        Set<Fit<Integer>> partialFits = NumericPattern.findAllPartialFits(sequence);

        then:
        assertThat(partialFits).containsExactlyInAnyOrderElementsOf(expectedPartialFits)

        where:
        sequence     | expectedPartialFits                                                                  | comment
        []           | []                                                                                   | ""
        [1]          | []                                                                                   | ""
        [1, 1]       | [Fit.of([1, 1], [1])]                                                                | ""
        [1, 1, 1]    | [Fit.of([1, 1, 1], [1]), Fit.of([1, 1], [1])]                                        | ""
        [1, 2]       | []                                                                                   | ""
        [1, 2, 1]    | [Fit.of([1, 2, 1], [1, 2])]                                                          | ""
        [1, 2, 1, 2] | [Fit.of([1, 2, 1, 2], [1, 2]), Fit.of([1, 2, 1], [1, 2]), Fit.of([2, 1, 2], [2, 1])] | ""

    }

    def "filter repeating subsequences"() {

        when:
        Set<List<Number>> nonRepeatedSequences = NumericPattern.filterRepeatedSubsequences(new HashSet<List<Integer>>(sequences));

        then:
        assertThat(nonRepeatedSequences).containsExactlyInAnyOrderElementsOf(expectedNonRepeatedSequences)

        where:
        sequences                   | expectedNonRepeatedSequences | comment
        []                          | []                           | ""
        [[1]]                       | [[1]]                        | ""
        [[1, 1]]                    | [[1, 1]]                     | ""
        [[1, 2]]                    | [[1, 2]]                     | ""

        [[1], [1, 1]]               | [[1, 1]]                     | ""
        [[1], [1, 2]]               | [[1, 2]]                     | ""

        [[3], [1, 1]]               | [[1, 1], [3]]                | ""
        [[3], [1, 2]]               | [[1, 2], [3]]                | ""

        [[1, 2, 1], [1, 2]]         | [[1, 2, 1]]                  | ""
        [[1, 2, 1], [2, 1]]         | [[1, 2, 1]]                  | ""
        [[1, 2, 1], [1, 2], [2, 1]] | [[1, 2, 1]]                  | ""

    }

    def "find all non repeatable partial fits"() {

        when:
        Set<Fit<Integer>> partialFits = NumericPattern.findAllNonRepeatablePartialFits(sequence);

        then:
        assertThat(partialFits).containsExactlyInAnyOrderElementsOf(expectedPartialFits)

        where:
        sequence                 | expectedPartialFits                                          | comment
        []                       | []                                                           | ""
        [1]                      | []                                                           | ""
        [1, 1]                   | [Fit.of([1, 1], [1])]                                        | ""
        [1, 1, 1]                | [Fit.of([1, 1, 1], [1])]                                     | ""
        [1, 2]                   | []                                                           | ""
        [1, 2, 1]                | [Fit.of([1, 2, 1], [1, 2])]                                  | ""
        [1, 2, 1, 2]             | [Fit.of([1, 2, 1, 2], [1, 2])]                               | ""

        [1, 2, 1, 2, 1, 3]       | [Fit.of([1, 2, 1, 2, 1], [1, 2])]                            | ""
        [1, 2, 1, 2, 1, 3, 4]    | [Fit.of([1, 2, 1, 2, 1], [1, 2])]                            | ""
        [1, 2, 1, 2, 1, 3, 4, 3] | [Fit.of([1, 2, 1, 2, 1], [1, 2]), Fit.of([3, 4, 3], [3, 4])] | ""

    }

    def "find all variations"() {

        when:
        Set<List<Integer>> variations = NumericPattern.findAllVariations(sequence);

        then:
        assertThat(variations).containsExactlyInAnyOrderElementsOf(expectedVariations)

        where:
        sequence  | expectedVariations                | comment
        []        | []                                | ""
        [1]       | [[1]]                             | ""
        [1, 1]    | [[1, 1]]                          | ""
        [1, 2]    | [[1, 2], [2, 1]]                  | ""
        [1, 2, 1] | [[1, 2, 1], [2, 1, 1], [1, 1, 2]] | ""
        [1, 2, 3] | [[1, 2, 3], [2, 3, 1], [3, 1, 2]] | ""

    }

    def "find base variation"() {

        when:
        List<Integer> baseVariation = NumericPattern.findBaseVariation(sequence);

        then:
        assertThat(baseVariation).containsExactlyInAnyOrderElementsOf(expectedBaseVariation)

        where:
        sequence  | expectedBaseVariation | comment
        []        | []                    | ""
        [1]       | [1]                   | ""
        [1, 1]    | [1, 1]                | ""
        [1, 2]    | [1, 2]                | ""
        [1, 2, 1] | [1, 1, 2]             | ""
        [1, 2, 3] | [1, 2, 3]             | ""
        [2, 3, 1] | [1, 2, 3]             | ""
        [3, 1, 2] | [1, 2, 3]             | ""

    }

    def "find all best partial fits"() {

        when:
        Set<List<Integer>> partialFits = NumericPattern.findAllBestFittingSubsequences(sequence);

        then:
        assertThat(partialFits).containsExactlyInAnyOrderElementsOf(expectedPartialFits)

        where:
        sequence                                                                 | expectedPartialFits            | comment
        []                                                                       | []                             | ""
        [1]                                                                      | []                             | ""
        [1, 1]                                                                   | [[1]]                          | ""
        [1, 1, 1]                                                                | [[1]]                          | ""
        [1, 2]                                                                   | []                             | ""
        [1, 2, 1]                                                                | [[1, 2]]                       | ""
        [1, 2, 1, 2]                                                             | [[1, 2]]                       | ""

        [1, 2, 1, 2, 1, 3]                                                       | [[1, 2]]                       | ""
        [1, 2, 1, 2, 1, 3, 4]                                                    | [[1, 2]]                       | ""
        [1, 2, 1, 2, 1, 3, 4, 3]                                                 | [[1, 2]]                       | ""
        [1, 2, 1, 2, 1, 3, 4, 3, 4]                                              | [[1, 2]]                       | ""
        [1, 2, 1, 2, 1, 3, 4, 3, 4, 3]                                           | [[1, 2], [3, 4]]               | ""
        [1, 2, 1, 2, 1, 3, 4, 3, 4, 3, 4]                                        | [[3, 4]]                       | ""

        [1, 2, 1, 2, 1, 3, 4, 3, 4, 3, 1, 2]                                     | [[1, 2], [3, 4]]               | ""
        [1, 2, 1, 2, 1, 3, 4, 3, 4, 3, 1, 2, 1]                                  | [[1, 2], [3, 4]]               | ""

        [1, 2, 1, 1, 2, 1, 2, 1, 2, 2, 1, 2]                                     | [[1, 2]]                       | ""
        [1, 2, 1, 1, 2, 1, 1, 2, 1, 2, 1, 2, 2, 1, 2, 2, 1, 2]                   | [[1, 2], [1, 1, 2], [1, 2, 2]] | "accidentally there is a third pattern [1, 2] with the same compression factor"
        [1, 2, 1, 1, 2, 1, 1, 2, 1, 1, 2, 1, 2, 1, 2, 2, 1, 2, 2, 1, 2, 2, 1, 2] | [[1, 1, 2], [1, 2, 2]]         | ""
        [1, 2, 1, 1, 2, 1, 1, 2, 1, 0, 2, 1, 2, 2, 1, 2, 2, 1, 2]                | [[1, 1, 2], [1, 2, 2]]         | ""

    }

    def "find the repeatable subsequence with the longest fitting repetition"() {

        when:
        Optional<? extends Pair<List<Integer>, List<Integer>>> result = NumericPattern.findTheRepeatableSubsequenceWithTheLongestFittingRepetitionStartingFromLeft(
                sequence,
                repeatableSubsequences
        );

        then:
        assertThat(result).isEqualTo(expectedResult)

        where:
        sequence           | repeatableSubsequences | expectedResult                                      | comment
        []                 | []                     | Optional.empty()                                    | ""
        [1]                | [[1]]                  | Optional.of(Pair.of([1], [0]))                      | ""
        [1, 1]             | [[1]]                  | Optional.of(Pair.of([1], [0, 1]))                   | ""
        [1, 1, 1]          | [[1]]                  | Optional.of(Pair.of([1], [0, 1, 2]))                | ""

        [1, 2]             | [[1, 2, 1], [1, 2]]    | Optional.of(Pair.of([1, 2, 1], [0, 1]))             | ""
        [1, 2, 1]          | [[1, 2, 1], [1, 2]]    | Optional.of(Pair.of([1, 2, 1], [0, 1, 2]))          | ""
        [1, 2, 1]          | [[1, 2], [1, 2, 1]]    | Optional.of(Pair.of([1, 2, 1], [0, 1, 2]))          | ""

        [1, 2, 1]          | [[1, 1, 2]]            | Optional.of(Pair.of([1, 1, 2], [0, 1, 2]))          | ""
        [1, 2, 1, 1, 2, 1] | [[1, 1, 2]]            | Optional.of(Pair.of([1, 1, 2], [0, 1, 2, 3, 4, 5])) | ""

    }

    def "find all fitting repeatable subsequences with their repetitions"() {

        when:
        List<? extends Pair<List<Integer>, List<Integer>>> result = NumericPattern.findAllFittingRepeatableSubsequencesWithTheirRepetitions(
                sequence,
                repeatableSubsequences
        );

        then:
        assertThat(result).isEqualTo(expectedResult)

        where:
        sequence                             | repeatableSubsequences | expectedResult                                                                           | comment
        []                                   | []                     | []                                                                                       | ""
        [1]                                  | [[1]]                  | [Pair.of([1], [0])]                                                                      | ""
        [1, 1]                               | [[1]]                  | [Pair.of([1], [0, 1])]                                                                   | ""
        [1, 1, 1]                            | [[1]]                  | [Pair.of([1], [0, 1, 2])]                                                                | ""

        [1, 2]                               | [[1, 2, 1], [1, 2]]    | [Pair.of([1, 2, 1], [0, 1])]                                                             | ""
        [1, 2, 1]                            | [[1, 2, 1], [1, 2]]    | [Pair.of([1, 2, 1], [0, 1, 2])]                                                          | ""
        [1, 2, 1]                            | [[1, 2], [1, 2, 1]]    | [Pair.of([1, 2, 1], [0, 1, 2])]                                                          | ""

        [1, 2, 1]                            | [[1, 1, 2]]            | [Pair.of([1, 1, 2], [0, 1, 2])]                                                          | ""
        [1, 2, 1, 1, 2, 1]                   | [[1, 1, 2]]            | [Pair.of([1, 1, 2], [0, 1, 2, 3, 4, 5])]                                                 | ""

        [1, 2, 1, 1, 2, 1, 2, 2, 1]          | [[1, 1, 2], [1, 2, 2]] | [Pair.of([1, 1, 2], [0, 1, 2, 3, 4, 5]), Pair.of([1, 2, 2], [4, 5, 6, 7, 8])]            | ""
        [1, 2, 1, 1, 2, 1, 2, 2, 1, 2, 2, 1] | [[1, 1, 2], [1, 2, 2]] | [Pair.of([1, 1, 2], [0, 1, 2, 3, 4, 5]), Pair.of([1, 2, 2], [4, 5, 6, 7, 8, 9, 10, 11])] | ""

    }

    def "split by most likely pattern"() {

        when:
        Set<NumericRange<Integer>> split = NumericPattern.splitByMostLikelyPattern(sequence);

        then:
        assertThat(split).containsExactlyInAnyOrderElementsOf(expectedSplit)

        where:
        sequence                                                                 | expectedSplit                             | comment
        []                                                                       | []                                        | ""
        [1]                                                                      | [range1(0)]                               | ""
        [1, 1]                                                                   | [range(0, 1)]                             | ""
        [1, 1, 1]                                                                | [range(0, 2)]                             | ""
        [1, 2]                                                                   | [range(0, 1)]                             | ""
        [1, 2, 1]                                                                | [range(0, 2)]                             | ""
        [1, 2, 1, 2]                                                             | [range(0, 3)]                             | ""

        [1, 2, 1, 2, 1, 3]                                                       | [range(0, 4), range1(5)]                  | ""
        [1, 2, 1, 2, 1, 3, 4]                                                    | [range(0, 4), range(5, 6)]                | ""
        [1, 2, 1, 2, 1, 3, 4, 3]                                                 | [range(0, 4), range(5, 7)]                | ""
        [1, 2, 1, 2, 1, 3, 4, 3, 4]                                              | [range(0, 4), range(5, 8)]                | ""
        [1, 2, 1, 2, 1, 3, 4, 3, 4, 3]                                           | [range(0, 4), range(5, 9)]                | ""
        [1, 2, 1, 2, 1, 3, 4, 3, 4, 3, 4]                                        | [range(0, 4), range(5, 10)]               | ""

        [1, 2, 1, 2, 1, 3, 4, 3, 4, 3, 1, 2]                                     | [range(0, 4), range(5, 9), range(10, 11)] | ""
        [1, 2, 1, 2, 1, 3, 4, 3, 4, 3, 1, 2, 1]                                  | [range(0, 4), range(5, 9), range(10, 12)] | ""

        [1, 2, 1, 1, 2, 1, 2, 1, 2, 2, 1, 2]                                     | [range(0, 2), range(3, 8), range(9, 11)]  | "accidentally there is a third pattern [1, 2] with a better compression factor"
        [1, 2, 1, 1, 2, 1, 1, 2, 1, 2, 1, 2, 2, 1, 2, 2, 1, 2]                   | [range(0, 8), range(9, 17)]               | ""
        [1, 2, 1, 1, 2, 1, 1, 2, 1, 1, 2, 1, 2, 1, 2, 2, 1, 2, 2, 1, 2, 2, 1, 2] | [range(0, 11), range(12, 23)]             | ""
        [1, 2, 1, 1, 2, 1, 1, 2, 1, 0, 2, 1, 2, 2, 1, 2, 2, 1, 2]                | [range(0, 8), range1(9), range(10, 18)]   | ""

    }


}