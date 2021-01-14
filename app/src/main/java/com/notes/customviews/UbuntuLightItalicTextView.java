package com.notes.customviews;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatTextView;

/**
 * Created by root on 9/29/17.
 */

public class UbuntuLightItalicTextView extends AppCompatTextView {
    public UbuntuLightItalicTextView(Context context) {
        super(context);
        init();
    }

    public UbuntuLightItalicTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public UbuntuLightItalicTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void init(){
        Typeface font = Typeface.createFromAsset(getContext().getAssets(), "ubuntu_light_italic.ttf");
        setTypeface(font);
    }
}
