package com.achunt.weboslauncher;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * A FrameLayout that never measures taller than a fraction of the screen height,
 * regardless of how tall its wrap_content children want to be. Used for the
 * notification panel so it can grow/shrink freely with content but never
 * swallow more than half the screen.
 */
public class MaxHeightFrameLayout extends FrameLayout {

    private static final float MAX_HEIGHT_FRACTION = 0.5f;

    public MaxHeightFrameLayout(Context context) {
        super(context);
    }

    public MaxHeightFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int maxHeight = (int) (getResources().getDisplayMetrics().heightPixels * MAX_HEIGHT_FRACTION);
        int mode = MeasureSpec.getMode(heightMeasureSpec);
        int size = MeasureSpec.getSize(heightMeasureSpec);

        int cappedHeightSpec;
        if (mode == MeasureSpec.EXACTLY) {
            // An explicit height was requested (our own height-animator stepping through a
            // pixel value each frame) — honor it exactly, only clamping if it exceeds the cap.
            // Silently falling back to AT_MOST here would make the FrameLayout re-wrap its
            // current children instead, which defeats the animation entirely: the panel would
            // just snap to whatever its content currently needs rather than tracking the
            // animated value.
            cappedHeightSpec = MeasureSpec.makeMeasureSpec(Math.min(size, maxHeight), MeasureSpec.EXACTLY);
        } else {
            int cap = (mode == MeasureSpec.AT_MOST) ? Math.min(size, maxHeight) : maxHeight;
            cappedHeightSpec = MeasureSpec.makeMeasureSpec(cap, MeasureSpec.AT_MOST);
        }
        super.onMeasure(widthMeasureSpec, cappedHeightSpec);
    }
}
