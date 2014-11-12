package com.mpcremcon.fonts;

import android.content.Context;
import android.graphics.Typeface;

import com.mpcremcon.R;

/**
 * Created by Oleh Chaplya on 11.11.2014.
 */
public class CustomFont {

    static Typeface font;

    public static void initFont(Context c) {
        font = Typeface.createFromAsset(c.getAssets(), "fonts/Montserrat-Regular.ttf");
    }

    public static Typeface getFont() {
        return font;
    }

}
