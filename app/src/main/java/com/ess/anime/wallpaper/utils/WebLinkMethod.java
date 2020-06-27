package com.ess.anime.wallpaper.utils;

import android.text.Layout;
import android.text.NoCopySpan;
import android.text.Selection;
import android.text.Spannable;
import android.text.method.MovementMethod;
import android.text.method.ScrollingMovementMethod;
import android.text.style.URLSpan;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

/**
 * A movement method that traverses links in the text buffer and scrolls if necessary.
 * Supports clicking on links with DPad Center or Enter.
 */
public class WebLinkMethod extends ScrollingMovementMethod {
    private static final int CLICK = 1;
    private static final int UP = 2;
    private static final int DOWN = 3;

    @Override
    public boolean canSelectArbitrarily() {
        return true;
    }

    @Override
    protected boolean handleMovementKey(TextView widget, Spannable buffer, int keyCode,
                                        int movementMetaState, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER:
                if (KeyEvent.metaStateHasNoModifiers(movementMetaState)) {
                    if (event.getAction() == KeyEvent.ACTION_DOWN &&
                            event.getRepeatCount() == 0 && action(CLICK, widget, buffer)) {
                        return true;
                    }
                }
                break;
        }
        return super.handleMovementKey(widget, buffer, keyCode, movementMetaState, event);
    }

    @Override
    protected boolean up(TextView widget, Spannable buffer) {
        if (action(UP, widget, buffer)) {
            return true;
        }

        return super.up(widget, buffer);
    }

    @Override
    protected boolean down(TextView widget, Spannable buffer) {
        if (action(DOWN, widget, buffer)) {
            return true;
        }

        return super.down(widget, buffer);
    }

    @Override
    protected boolean left(TextView widget, Spannable buffer) {
        if (action(UP, widget, buffer)) {
            return true;
        }

        return super.left(widget, buffer);
    }

    @Override
    protected boolean right(TextView widget, Spannable buffer) {
        if (action(DOWN, widget, buffer)) {
            return true;
        }

        return super.right(widget, buffer);
    }

    private boolean action(int what, TextView widget, Spannable buffer) {
        Layout layout = widget.getLayout();

        int padding = widget.getTotalPaddingTop() +
                widget.getTotalPaddingBottom();
        int areaTop = widget.getScrollY();
        int areaBot = areaTop + widget.getHeight() - padding;

        int lineTop = layout.getLineForVertical(areaTop);
        int lineBot = layout.getLineForVertical(areaBot);

        int first = layout.getLineStart(lineTop);
        int last = layout.getLineEnd(lineBot);

        URLSpan[] candidates = buffer.getSpans(first, last, URLSpan.class);

        int a = Selection.getSelectionStart(buffer);
        int b = Selection.getSelectionEnd(buffer);

        int selStart = Math.min(a, b);
        int selEnd = Math.max(a, b);

        if (selStart < 0) {
            if (buffer.getSpanStart(FROM_BELOW) >= 0) {
                selStart = selEnd = buffer.length();
            }
        }

        if (selStart > last)
            selStart = selEnd = Integer.MAX_VALUE;
        if (selEnd < first)
            selStart = selEnd = -1;

        switch (what) {
            case CLICK:
                if (selStart == selEnd) {
                    return false;
                }

                URLSpan[] links = buffer.getSpans(selStart, selEnd, URLSpan.class);

                if (links.length != 1) {
                    return false;
                }

                URLSpan link = links[0];
                if (mHyperlinkListener != null) {
                    mHyperlinkListener.onLink(link.getURL());
                }
                break;

            case UP:
                int bestStart, bestEnd;

                bestStart = -1;
                bestEnd = -1;

                for (int i = 0; i < candidates.length; i++) {
                    int end = buffer.getSpanEnd(candidates[i]);

                    if (end < selEnd || selStart == selEnd) {
                        if (end > bestEnd) {
                            bestStart = buffer.getSpanStart(candidates[i]);
                            bestEnd = end;
                        }
                    }
                }

                if (bestStart >= 0) {
                    Selection.setSelection(buffer, bestEnd, bestStart);
                    return true;
                }

                break;

            case DOWN:
                bestStart = Integer.MAX_VALUE;
                bestEnd = Integer.MAX_VALUE;

                for (int i = 0; i < candidates.length; i++) {
                    int start = buffer.getSpanStart(candidates[i]);

                    if (start > selStart || selStart == selEnd) {
                        if (start < bestStart) {
                            bestStart = start;
                            bestEnd = buffer.getSpanEnd(candidates[i]);
                        }
                    }
                }

                if (bestEnd < Integer.MAX_VALUE) {
                    Selection.setSelection(buffer, bestStart, bestEnd);
                    return true;
                }

                break;
        }

        return false;
    }

    @Override
    public boolean onTouchEvent(TextView widget, Spannable buffer,
                                MotionEvent event) {
        int action = event.getAction();

        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_DOWN) {
            int x = (int) event.getX();
            int y = (int) event.getY();

            x -= widget.getTotalPaddingLeft();
            y -= widget.getTotalPaddingTop();

            x += widget.getScrollX();
            y += widget.getScrollY();

            Layout layout = widget.getLayout();
            int line = layout.getLineForVertical(y);
            int off = layout.getOffsetForHorizontal(line, x);

            URLSpan[] links = buffer.getSpans(off, off, URLSpan.class);

            if (links.length != 0) {
                URLSpan link = links[0];
                if (action == MotionEvent.ACTION_UP) {
                    if (mHyperlinkListener != null) {
                        mHyperlinkListener.onLink(link.getURL());
                    }
                } else if (action == MotionEvent.ACTION_DOWN) {
                    Selection.setSelection(buffer,
                            buffer.getSpanStart(link),
                            buffer.getSpanEnd(link));
                }
                return true;
            } else {
                Selection.removeSelection(buffer);
            }
        }

        return super.onTouchEvent(widget, buffer, event);
    }

    @Override
    public void initialize(TextView widget, Spannable text) {
        Selection.removeSelection(text);
        text.removeSpan(FROM_BELOW);
    }

    @Override
    public void onTakeFocus(TextView view, Spannable text, int dir) {
        Selection.removeSelection(text);

        if ((dir & View.FOCUS_BACKWARD) != 0) {
            text.setSpan(FROM_BELOW, 0, 0, Spannable.SPAN_POINT_POINT);
        } else {
            text.removeSpan(FROM_BELOW);
        }
    }

    private OnHyperlinkListener mHyperlinkListener;

    public MovementMethod setOnHyperlinkListener(OnHyperlinkListener hyperlinkListener) {
        mHyperlinkListener = hyperlinkListener;
        return this;
    }

    public interface OnHyperlinkListener {
        void onLink(String url);
    }

    public static WebLinkMethod getInstance() {
        if (sInstance == null)
            sInstance = new WebLinkMethod();

        return sInstance;
    }

    private static WebLinkMethod sInstance;
    private static Object FROM_BELOW = new NoCopySpan.Concrete();
}
