package com.santhosh.smarttheatre;

import android.content.Context;
import android.support.design.widget.FloatingActionButton;
import android.util.AttributeSet;
import android.widget.Checkable;
import android.widget.ImageButton;

/**
 * Created by santhosh-3366 on 16/03/17.
 */

public class FABToggle extends FloatingActionButton /*implements Checkable */{

    private static final int[] CHECKED_STATE_SET = { android.R.attr.state_checked };

    private boolean isChecked = false;
    private int minOffset;

    public FABToggle(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setOffset(int offset) {
        if (offset != getTranslationY()) {
            offset = Math.max(minOffset, offset);
            setTranslationY(offset);
        }
    }

    public void setMinOffset(int minOffset) {
        this.minOffset = minOffset;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean isChecked) {
        if (this.isChecked != isChecked) {
            this.isChecked = isChecked;
            refreshDrawableState();
        }
    }

    public void toggle() {
        setChecked(!isChecked);
    }

    @Override
    public int[] onCreateDrawableState(int extraSpace) {
        final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
        if (isChecked()) {
            mergeDrawableStates(drawableState, CHECKED_STATE_SET);
        }
        return drawableState;
    }
}
