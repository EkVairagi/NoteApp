package com.notes.customviews;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatTextView;

/**
 * Created by root on 9/29/17.
 */

public class UbuntuRegularTextView extends AppCompatTextView {
    public UbuntuRegularTextView(Context context) {
        super(context);
        init();
    }

    public UbuntuRegularTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public UbuntuRegularTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void init(){
        Typeface font = Typeface.createFromAsset(getContext().getAssets(), "ubuntu_regular.ttf");
        setTypeface(font);
    }
}
