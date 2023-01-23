package be.gerard.pattern.numeric;

import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

@Sorted
public interface SortedNumericPattern<T extends Number> extends NumericPattern<T> {

    static <T extends Number> boolean isSorted(
            final List<T> sequence
    ) {
        return IntStream.range(1, sequence.size())
                .allMatch(i -> sequence.get(i - 1).longValue() < sequence.get(i).longValue());
    }

    @Override
    default boolean isSorted() {
        return true;
    }

    boolean canReach(T number);

    List<List<T>> splitDeviatingIncrements(Number increment);

    Set<Fit<T>> groupCommonIncrements(Number maxIncrement);

    Set<Fit<T>> groupCycles(Number patternLength);
}
