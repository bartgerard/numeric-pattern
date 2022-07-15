package be.gerard.pattern.numeric.internal;

import be.gerard.pattern.numeric.SortedNumericPattern;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

import static java.util.Collections.emptyList;

public record EmptySequence<T extends Number>() implements SortedNumericPattern<T> {

    @Override
    public List<? extends Pair<T, T>> findAllGaps() {
        return emptyList();
    }

    @Override
    public List<T> sequence() {
        return emptyList();
    }

    @Override
    public boolean canReach(
            final T number
    ) {
        return false;
    }

}
