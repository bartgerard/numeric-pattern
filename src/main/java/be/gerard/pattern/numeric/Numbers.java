package be.gerard.pattern.numeric;

public final class Numbers {

    private Numbers() {
        // no-op
    }

    static int difference(
            final int minuend,
            final int subtrahend
    ) {
        return minuend - subtrahend; // = difference
    }

    static long difference(
            final long minuend,
            final long subtrahend
    ) {
        return minuend - subtrahend; // = difference
    }

}
