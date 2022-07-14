package be.gerard.pattern.numeric;

public interface SortedNumericPattern<T extends Number> extends NumericPattern<T> {

    @Override
    default boolean isSorted() {
        return true;
    }

    boolean canReach(T number);

}
