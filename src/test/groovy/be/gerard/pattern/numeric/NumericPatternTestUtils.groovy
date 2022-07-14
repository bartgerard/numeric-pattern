package be.gerard.pattern.numeric


import org.apache.commons.lang3.tuple.ImmutablePair
import org.apache.commons.lang3.tuple.Pair

import static org.apache.commons.lang3.Validate.notNull

class NumericPatternTestUtils {

    static List<Long> toLongValues(
            final Collection<Number> numbers
    ) {
        return numbers.stream()
                .map(Number::longValue)
                .toList()
    }

    static <T extends Number> Pair<T, T> pair(
            final T left,
            final T right
    ) {
        notNull(left)
        notNull(right)

        return ImmutablePair.of(left, right)
    }

}
