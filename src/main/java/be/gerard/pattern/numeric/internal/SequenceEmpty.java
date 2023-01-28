package be.gerard.pattern.numeric.internal;

import be.gerard.pattern.numeric.Fit;
import be.gerard.pattern.numeric.SortedNumericPattern;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Set;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;

public record SequenceEmpty<T extends Number>() implements SortedNumericPattern<T> {

    @Override
    public List<T> sequence() {
        return emptyList();
    }

    @Override
    public List<? extends Pair<T, T>> findAllGaps() {
        return emptyList();
    }

    @Override
    public boolean canReach(
            final T number
    ) {
        return false;
    }

    @Override
    public List<List<T>> splitDeviatingIncrements(
            final Number increment
    ) {
        return emptyList();
    }

    @Override
    public Set<Fit<T>> groupCommonIncrements(
            final Number maxIncrement
    ) {
        return emptySet();
    }

    @Override
    public Set<Fit<T>> groupCycles(
            final Number patternLength
    ) {
        return emptySet();
    }
}
