package com.example.mit_plateform.Utilities;

import android.content.Context;
import android.widget.ImageView;
import androidx.core.content.ContextCompat;
import com.example.mit_plateform.R;

public class NavigationUtils {
    public static void updateActiveIcon(Context context, ImageView iconView, boolean isActive) {
        int color = isActive ? R.color.red : R.color.white;
        iconView.setColorFilter(ContextCompat.getColor(context, color));
    }
}