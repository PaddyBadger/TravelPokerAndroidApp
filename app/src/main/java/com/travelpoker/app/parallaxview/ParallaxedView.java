package com.travelpoker.app.parallaxview;

import android.view.View;

import java.lang.ref.WeakReference;

/**
 * Created by patriciaestridge on 6/6/14.
 */
public abstract class ParallaxedView {
    protected WeakReference<View> view;
    protected int lastOffset;

    abstract protected void translatePreICS(View view, float offset);

    public ParallaxedView(View view) {
        this.lastOffset = 0;
        this.view = new WeakReference<View>(view);
    }

    public boolean is(View v) {
        return (v != null && view != null && view.get() != null && view.get().equals(v));
    }

    public void setOffset(float offset) {
        View view = this.view.get();

        view.setTranslationY(offset);

    }

    public void setView(View view) {
        this.view = new WeakReference<View>(view);
    }
}
