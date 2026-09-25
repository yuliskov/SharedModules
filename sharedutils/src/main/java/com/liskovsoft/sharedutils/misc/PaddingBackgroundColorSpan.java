package com.liskovsoft.sharedutils.misc;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.Layout;
import android.text.style.LineBackgroundSpan;

// https://gist.github.com/tokudu/601320d9edb978bcbc31
public class PaddingBackgroundColorSpan implements LineBackgroundSpan {
    private final int mBackgroundColor;
    private final int mPadding; // in pixels
    private final Rect mBgRect;
    private final Layout.Alignment mAlignment;

    public PaddingBackgroundColorSpan(int backgroundColor) {
        this(backgroundColor, 10, Layout.Alignment.ALIGN_CENTER);
    }

    public PaddingBackgroundColorSpan(int backgroundColor, int padding) {
        this(backgroundColor, padding, Layout.Alignment.ALIGN_CENTER);
    }

    public PaddingBackgroundColorSpan(int backgroundColor, int padding, Layout.Alignment alignment) {
        super();
        mBackgroundColor = backgroundColor;
        mPadding = padding;
        mAlignment = alignment != null ? alignment : Layout.Alignment.ALIGN_CENTER;
        // Precreate rect for performance
        mBgRect = new Rect();
    }

    @Override
    public void drawBackground(Canvas c, Paint p, int left, int right, int top, int baseline, int bottom, CharSequence text, int start, int end, int lnum) {
        // Strip trailing whitespaces (e.g. \n or spaces) so background doesn't protrude on the right
        int actualEnd = end;
        while (actualEnd > start && Character.isWhitespace(text.charAt(actualEnd - 1))) {
            actualEnd--;
        }
        if (actualEnd <= start) {
            return;
        }

        final int textWidth = Math.round(p.measureText(text, start, actualEnd));
        final int paintColor = p.getColor();
        final int leftCalc;

        if (mAlignment == Layout.Alignment.ALIGN_NORMAL) {
            leftCalc = left;
        } else if (mAlignment == Layout.Alignment.ALIGN_OPPOSITE) {
            leftCalc = right - textWidth;
        } else {
            leftCalc = (right - left - textWidth) / 2; // assume that the text is centered
        }

        // Draw the background
        mBgRect.set(leftCalc - mPadding * 2,
                top - (lnum == 0 ? mPadding / 2 : - (mPadding / 2)),
                leftCalc + textWidth + mPadding * 2,
                bottom + mPadding / 2);
        p.setColor(mBackgroundColor);
        c.drawRect(mBgRect, p);
        p.setColor(paintColor);
    }
}
