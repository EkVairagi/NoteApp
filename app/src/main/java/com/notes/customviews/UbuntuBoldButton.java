package com.notes.customviews;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatButton;

public class UbuntuBoldButton extends AppCompatButton
{

    public UbuntuBoldButton(Context context) {
        super(context);
        init();
    }

    public UbuntuBoldButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public UbuntuBoldButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init()
    {
        Typeface font= Typeface.createFromAsset(getContext().getAssets(),
                "ubuntu_bold.ttf");



        setTypeface(font);
    }

}
