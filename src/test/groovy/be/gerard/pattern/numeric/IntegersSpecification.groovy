package be.gerard.pattern.numeric


import spock.lang.Specification
import spock.lang.Title

@Title("Integers")
class IntegersSpecification extends Specification {

    def "calculate the least common multiple"() {

        when:
        int result = Integers.leastCommonMultiple(numbers)

        then:
        result == expectedLeastCommonMultiple

        where:
        numbers   | expectedLeastCommonMultiple | comment
        [2, 3]    | 6                           | ""
        [3, 2]    | 6                           | ""
        [6, 2, 3] | 6                           | ""
        [6, 3]    | 6                           | ""

        [4, 3, 2] | 12                          | ""
        [45, 9]   | 45                          | ""

    }

}