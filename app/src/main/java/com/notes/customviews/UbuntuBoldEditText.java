package com.notes.customviews;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatEditText;

public class UbuntuBoldEditText extends AppCompatEditText {
    public UbuntuBoldEditText(Context context) {
        super(context);
        init();
    }

    public UbuntuBoldEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public UbuntuBoldEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void init(){
        Typeface font = Typeface.createFromAsset(getContext().getAssets(), "ubuntu_bold.ttf");
        setTypeface(font);
    }

}
