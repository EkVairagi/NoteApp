package com.notes.customviews;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatButton;

/**
 * Created by root on 9/29/17.
 */

public class UbuntuRegularbutton extends AppCompatButton {
    public UbuntuRegularbutton(Context context) {
        super(context);
        init();
    }

    public UbuntuRegularbutton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public UbuntuRegularbutton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void init(){
        Typeface font = Typeface.createFromAsset(getContext().getAssets(), "ubuntu_regular.ttf");
        setTypeface(font);
    }
}
