/*
 * Copyright (C) 2016 Álinson Santos Xavier <isoron@gmail.com>
 *
 * This file is part of Loop Habit Tracker.
 *
 * Loop Habit Tracker is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Loop Habit Tracker is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.isoron.uhabits.activities.habits.list.views;

import android.content.*;
import android.graphics.*;
import android.support.annotation.*;
import android.text.*;
import android.util.*;

import org.isoron.androidbase.utils.*;
import org.isoron.uhabits.*;
import org.isoron.uhabits.activities.common.views.*;
import org.isoron.uhabits.core.preferences.*;
import org.isoron.uhabits.core.utils.*;

import java.util.*;

import static org.isoron.androidbase.utils.InterfaceUtils.*;

public class HeaderView extends ScrollableChart
    implements Preferences.Listener, MidnightTimer.MidnightListener
{

    private int buttonCount;

    @Nullable
    private Preferences prefs;

    @Nullable
    private MidnightTimer midnightTimer;

    private TextPaint paint;

    private RectF rect;

    public HeaderView(@NonNull Context context,
                      @NonNull Preferences prefs,
                      @NonNull MidnightTimer midnightTimer)
    {
        super(context);
        this.prefs = prefs;
        this.midnightTimer = midnightTimer;
        init();
    }

    public HeaderView(Context context, @Nullable AttributeSet attrs)
    {
        super(context, attrs);

        Context appContext = context.getApplicationContext();
        if (appContext instanceof HabitsApplication)
        {
            HabitsApplication app = (HabitsApplication) appContext;
            prefs = app.getComponent().getPreferences();
            midnightTimer = app.getComponent().getMidnightTimer();
        }

        init();
    }

    @Override
    public void atMidnight()
    {
        post(() -> invalidate());
    }

    @Override
    public void onCheckmarkSequenceChanged()
    {
        updateDirection();
        postInvalidate();
    }

    public void setButtonCount(int buttonCount)
    {
        this.buttonCount = buttonCount;
        postInvalidate();
    }

    @Override
    protected void onAttachedToWindow()
    {
        updateDirection();
        super.onAttachedToWindow();
        if (prefs != null) prefs.addListener(this);
        if (midnightTimer != null) midnightTimer.addListener(this);
    }

    @Override
    protected void onDetachedFromWindow()
    {
        if (midnightTimer != null) midnightTimer.removeListener(this);
        if (prefs != null) prefs.removeListener(this);
        super.onDetachedFromWindow();
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);

        GregorianCalendar day = DateUtils.getStartOfTodayCalendar();
        float width = getDimension(getContext(), R.dimen.checkmarkWidth);
        float height = getDimension(getContext(), R.dimen.checkmarkHeight);
        boolean reverse = shouldReverseCheckmarks();
        boolean isRtl = InterfaceUtils.isLayoutRtl(this);

        day.add(GregorianCalendar.DAY_OF_MONTH, -getDataOffset());
        float em = paint.measureText("m");

        for (int i = 0; i < buttonCount; i++)
        {
            rect.set(0, 0, width, height);
            rect.offset(canvas.getWidth(), 0);

            if (reverse) rect.offset(-(i + 1) * width, 0);
            else rect.offset((i - buttonCount) * width, 0);

            if (isRtl) rect.set(canvas.getWidth() - rect.right, rect.top,
                canvas.getWidth() - rect.left, rect.bottom);

            String text = DateUtils.formatHeaderDate(day).toUpperCase();
            String[] lines = text.split("\n");

            int y1 = (int) (rect.centerY() - 0.25 * em);
            int y2 = (int) (rect.centerY() + 1.25 * em);

            canvas.drawText(lines[0], rect.centerX(), y1, paint);
            canvas.drawText(lines[1], rect.centerX(), y2, paint);
            day.add(GregorianCalendar.DAY_OF_MONTH, -1);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = (int) getDimension(getContext(), R.dimen.checkmarkHeight);
        setMeasuredDimension(width, height);
    }

    private void init()
    {
        setScrollerBucketSize(
            (int) getDimension(getContext(), R.dimen.checkmarkWidth));

        StyledResources sr = new StyledResources(getContext());
        paint = new TextPaint();
        paint.setColor(Color.BLACK);
        paint.setAntiAlias(true);
        paint.setTextSize(getDimension(getContext(), R.dimen.tinyTextSize));
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTypeface(Typeface.DEFAULT_BOLD);
        paint.setColor(sr.getColor(R.attr.mediumContrastTextColor));

        rect = new RectF();

        if (isInEditMode()) setButtonCount(5);
    }

    private boolean shouldReverseCheckmarks()
    {
        if (prefs == null) return false;
        return prefs.isCheckmarkSequenceReversed();
    }

    private void updateDirection()
    {
        int direction = -1;
        if (shouldReverseCheckmarks()) direction *= -1;
        if (InterfaceUtils.isLayoutRtl(this)) direction *= -1;
        setDirection(direction);
    }
}
