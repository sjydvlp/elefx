package com.sjydvlp.elefx.component.skeleton;

/**
 * Delays Skeleton visibility transitions to avoid a distracting flash for quick loads.
 * Leading delays showing the placeholder; trailing delays hiding it.
 */
public final class EleFXSkeletonThrottle {

    public static final EleFXSkeletonThrottle NONE = new EleFXSkeletonThrottle(0, 0, false);

    private final long leading;

    private final long trailing;

    private final boolean initialLoading;

    /** Equivalent to Element Plus's numeric {@code throttle} value. */
    public EleFXSkeletonThrottle(long leading) {
        this(leading, 0, false);
    }

    public EleFXSkeletonThrottle(long leading, long trailing) {
        this(leading, trailing, false);
    }

    public EleFXSkeletonThrottle(long leading, long trailing, boolean initialLoading) {
        if (leading < 0 || trailing < 0) {
            throw new IllegalArgumentException("throttle delays must not be negative");
        }
        this.leading = leading;
        this.trailing = trailing;
        this.initialLoading = initialLoading;
    }

    public long getLeading() {
        return leading;
    }

    public long getTrailing() {
        return trailing;
    }

    /** Whether an initially loading Skeleton bypasses its leading delay. */
    public boolean isInitialLoading() {
        return initialLoading;
    }
}
