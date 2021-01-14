package com.notes.customviews;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.EditText;

/**
 * Created by root on 9/29/17.
 */

public class UbuntuRegularEdittext extends EditText {
    public UbuntuRegularEdittext(Context context) {
        super(context);
        init();
    }

    public UbuntuRegularEdittext(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public UbuntuRegularEdittext(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void init(){
        Typeface font = Typeface.createFromAsset(getContext().getAssets(), "ubuntu_regular.ttf");
        setTypeface(font);
    }

/*    public void setError(CharSequence error, Drawable icon)
    {
        setCompoundDrawables(null, null,    icon, null);
    }*/
}
