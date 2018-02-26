package com.ess.anime.wallpaper.view;

import android.graphics.Rect;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class GridDividerItemDecoration extends RecyclerView.ItemDecoration {

    public static final int HORIZONTAL = LinearLayoutManager.HORIZONTAL;
    public static final int VERTICAL = LinearLayoutManager.VERTICAL;

    private int spaceLeft;
    private int spaceTop;
    private int spaceRight;
    private int spaceBottom;

    private int span;
    private int orientation;
    private boolean hasFinalSpace;

    public GridDividerItemDecoration(int span, int orientation, int space, boolean hasFinalSpace) {
        this(span, orientation, space, space, space, space, hasFinalSpace);
    }

    public GridDividerItemDecoration(int span, int orientation, int spaceHor, int spaceVer, boolean hasFinalSpace) {
        this(span, orientation, spaceHor, spaceVer, spaceHor, spaceVer, hasFinalSpace);
    }

    public GridDividerItemDecoration(int span, int orientation, int spaceLeft, int spaceTop, int spaceRight, int spaceBottom, boolean hasFinalSpace) {
        this.span = span;
        this.orientation = orientation;
        this.hasFinalSpace = hasFinalSpace;
        this.spaceLeft = spaceLeft;
        this.spaceTop = spaceTop;
        this.spaceRight = spaceRight;
        this.spaceBottom = spaceBottom;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        int totalLines;
        if (parent.getChildCount() % span == 0) {
            totalLines = parent.getAdapter().getItemCount() / span;
        } else {
            totalLines = parent.getAdapter().getItemCount() / span + 1;
        }
        int currentLine = parent.getChildAdapterPosition(view) / span + 1;

        if (orientation == VERTICAL) {
            drawVertical(outRect, currentLine, totalLines);
        } else if (orientation == HORIZONTAL) {
            drawHorizontal(outRect, currentLine, totalLines);
        }
    }

    private void drawVertical(Rect outRect, int currentLine, int totalLines) {
        outRect.left = spaceLeft;
        outRect.right = spaceRight;

        // Add top margin only for the first item to avoid double space between items
        if (currentLine == 1) {
            outRect.top = spaceTop;
        }

        //设置最后一行有没有bottom
        if (currentLine < totalLines) {
            outRect.bottom = spaceBottom;
        } else {
            if (hasFinalSpace) {
                outRect.bottom = spaceBottom;
            }
        }
    }

    private void drawHorizontal(Rect outRect, int currentLine, int totalLines) {
        outRect.top = spaceTop;
        outRect.bottom = spaceBottom;

        if (currentLine == 1) {
            outRect.left = spaceLeft;
        }

        if (currentLine < totalLines) {
            outRect.right = spaceRight;
        } else {
            if (hasFinalSpace) {
                outRect.right = spaceRight;
            }
        }
    }
}
