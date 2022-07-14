package be.gerard.pattern.numeric


import org.apache.commons.lang3.tuple.Pair
import spock.lang.Specification
import spock.lang.Title

import static NumericPatternTestUtils.pair
import static NumericPatternTestUtils.toLongValues
import static org.assertj.core.api.Assertions.assertThat

@Title("NumericPattern Tests")
class NumericPatternSpecification extends Specification {

    def "compare start and end subsequences"() {

        given:
        NumericPattern<Integer> pattern1 = NumericPattern.sorted(sequence1)
        NumericPattern<Integer> pattern2 = NumericPattern.sorted(sequence2)

        when:
        boolean hasSameStartSequence = pattern1.hasSameStartSequenceAs(pattern2)
        boolean hasSameEndSequence = pattern1.hasSameEndSequenceAs(pattern2)

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
        final List<Pair<Integer, Integer>> gaps = pattern.findAllGaps();

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
        final List<Integer> shortestRepeatingSubsequence = NumericPattern.findShortestRepeatingSubsequence(sequence);

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


}