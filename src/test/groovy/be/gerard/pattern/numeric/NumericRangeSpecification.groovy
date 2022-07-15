package be.gerard.pattern.numeric


import spock.lang.Specification
import spock.lang.Title

import static be.gerard.pattern.numeric.NumericPatternTestUtils.range
import static be.gerard.pattern.numeric.NumericPatternTestUtils.range1
import static org.assertj.core.api.Assertions.assertThat

@Title("NumericRange Tests")
class NumericRangeSpecification extends Specification {

    def "group subsequent numbers"() {

        when:
        final List<NumericRange<Integer>> ranges = NumericRange.groupSubsequentNumbers(numbers)

        then:
        assertThat(ranges).containsExactlyElementsOf(expectedRanges)

        where:
        numbers         | expectedRanges             | comment
        []              | []                         | ""
        [1]             | [range1(1)]                | ""
        [1, 2]          | [range(1, 2)]              | ""
        [1, 3]          | [range1(1), range1(3)]     | ""
        [1, 2, 3]       | [range(1, 3)]              | ""
        [1, 2, 3, 5]    | [range(1, 3), range1(5)]   | ""
        [1, 2, 3, 5, 6] | [range(1, 3), range(5, 6)] | ""

    }

}