package com.ess.anime.wallpaper.ui.view;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

public class GeneralRecyclerView extends RecyclerView {

    public GeneralRecyclerView(@NonNull Context context) {
        super(context);
    }

    public GeneralRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public GeneralRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void clearItemDecorations() {
        for (int i = getItemDecorationCount() - 1; i >= 0; i--) {
            removeItemDecorationAt(i);
        }
    }
}
