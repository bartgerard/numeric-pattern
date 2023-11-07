package be.gerard.pattern.numeric;

import java.util.Collection;
import java.util.stream.IntStream;

import static org.apache.commons.lang3.Validate.isTrue;
import static org.apache.commons.lang3.Validate.notEmpty;

public final class Integers {

    private Integers() {
        // no-op
    }

    static int leastCommonMultiple(
            final Collection<Integer> numbers
    ) {
        notEmpty(numbers);
        isTrue(numbers.stream().allMatch(n -> n.longValue() > 0));

        final int firstNumber = numbers.stream()
                .mapToInt(Number::intValue)
                .findFirst()
                .orElseThrow();

        return IntStream.iterate(firstNumber, i -> i + firstNumber)
                .filter(i -> numbers.stream().allMatch(number -> i % number == 0))
                .findFirst()
                .orElse(0);
    }

}
