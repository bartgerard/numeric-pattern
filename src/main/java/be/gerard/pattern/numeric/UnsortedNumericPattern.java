package be.gerard.pattern.numeric;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.stream.IntStream;

import static java.util.Collections.emptyList;

@Unsorted
public interface UnsortedNumericPattern<T extends Number> extends NumericPattern<T> {

    @Override
    default boolean isSorted() {
        return false;
    }

    @Override
    default List<? extends Pair<T, T>> findAllGaps() {
        if (size() <= 1) {
            return emptyList();
        }

        return IntStream.range(1, size())
                .filter(i -> Math.abs(sequence().get(i).longValue() - sequence().get(i - 1).longValue()) != 1)
                .mapToObj(i -> ImmutablePair.of(
                        sequence().get(i - 1),
                        sequence().get(i)
                ))
                .toList();
    }

}
