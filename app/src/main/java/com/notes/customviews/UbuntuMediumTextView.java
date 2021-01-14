package com.notes.customviews;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatTextView;

/**
 * Created by root on 9/29/17.
 */

public class UbuntuMediumTextView extends AppCompatTextView {
    public UbuntuMediumTextView(Context context) {
        super(context);
        init();
    }

    public UbuntuMediumTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public UbuntuMediumTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void init(){
        Typeface font = Typeface.createFromAsset(getContext().getAssets(), "ubuntu_medium.ttf");
        setTypeface(font);
    }
}
