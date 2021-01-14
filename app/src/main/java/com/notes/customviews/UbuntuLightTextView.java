package com.notes.customviews;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatTextView;

/**
 * Created by root on 9/29/17.
 */

public class UbuntuLightTextView extends AppCompatTextView {
    public UbuntuLightTextView(Context context) {
        super(context);
        init();
    }

    public UbuntuLightTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public UbuntuLightTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void init(){
        Typeface font = Typeface.createFromAsset(getContext().getAssets(), "ubuntu_light.ttf");
        setTypeface(font);
    }
}
