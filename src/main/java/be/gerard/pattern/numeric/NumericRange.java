package be.gerard.pattern.numeric;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.IntStream;

import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.Validate.isTrue;
import static org.apache.commons.lang3.Validate.notNull;

public record NumericRange<T extends Number>(
        T start,
        T end
) {

    public NumericRange {
        notNull(start);
        notNull(end);

        isTrue(start.longValue() <= end.longValue());
    }

    public static <T extends Number> NumericRange<T> of(
            T start,
            T end
    ) {
        return new NumericRange<>(start, end);
    }

    public static <T extends Number> List<NumericRange<T>> groupSubsequentNumbers(
            final Collection<T> numbers
    ) {
        final List<T> sortedNumbers = numbers.stream()
                .distinct()
                .sorted()
                .toList();

        if (sortedNumbers.isEmpty()) {
            return emptyList();
        } else if (sortedNumbers.size() == 1) {
            final T soloNumber = sortedNumbers.get(0);
            return List.of(NumericRange.of(
                    soloNumber,
                    soloNumber
            ));
        }

        final int[] nonConsecutiveIndices = IntStream.range(1, sortedNumbers.size())
                .filter(i -> sortedNumbers.get(i - 1).longValue() + 1 != sortedNumbers.get(i).longValue())
                .toArray();

        final int[] dateRangeBorders = IntStream.concat(
                        IntStream.of(0, sortedNumbers.size()),
                        Arrays.stream(nonConsecutiveIndices)
                )
                .sorted()
                .toArray();

        return IntStream.range(1, dateRangeBorders.length)
                .mapToObj(i -> NumericRange.of(
                        sortedNumbers.get(dateRangeBorders[i - 1]),
                        sortedNumbers.get(dateRangeBorders[i] - 1)
                ))
                .toList();
    }

}
