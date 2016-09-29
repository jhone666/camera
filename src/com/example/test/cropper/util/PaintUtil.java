package com.example.test.cropper.util;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.TypedValue;

public class PaintUtil {


    private static final int DEFAULT_CORNER_COLOR = Color.WHITE;
    private static final String SEMI_TRANSPARENT = "#AA52ce90";
    private static final String DEFAULT_BACKGROUND_COLOR_ID = "#B0000000";
    private static final float DEFAULT_LINE_THICKNESS_DP = 2;
    private static final float DEFAULT_CORNER_THICKNESS_DP = 1;
    private static final float DEFAULT_GUIDELINE_THICKNESS_PX = 1;


    public static Paint newBorderPaint(Context context) {

        final float lineThicknessPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                                                                DEFAULT_LINE_THICKNESS_DP,
                                                                context.getResources().getDisplayMetrics());

        final Paint borderPaint = new Paint();
        borderPaint.setColor(Color.parseColor("#FFC700"));
        borderPaint.setStrokeWidth(lineThicknessPx);
        borderPaint.setStyle(Paint.Style.STROKE);

        return borderPaint;
    }

    public static Paint newGuidelinePaint() {

        final Paint paint = new Paint();
        paint.setColor(Color.parseColor("#AAFFFFFF"));
        paint.setStrokeWidth(DEFAULT_GUIDELINE_THICKNESS_PX);

        return paint;
    }

    public static Paint newBackgroundPaint(Context context) {

        final Paint paint = new Paint();
        paint.setColor(Color.parseColor(DEFAULT_BACKGROUND_COLOR_ID));

        return paint;
    }

    public static Paint outSideCiclePaint(Context context) {

        final float lineThicknessPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                                                                DEFAULT_CORNER_THICKNESS_DP,
                                                                context.getResources().getDisplayMetrics());

        final Paint cornerPaint = new Paint();
        cornerPaint.setColor(Color.parseColor("#80FFC700"));
        cornerPaint.setStrokeWidth(lineThicknessPx);
        cornerPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        return cornerPaint;
    }

    public static Paint inSideCiclePaint(Context context) {

        final float lineThicknessPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                                                                DEFAULT_CORNER_THICKNESS_DP,
                                                                context.getResources().getDisplayMetrics());

        final Paint cornerPaint = new Paint();
        cornerPaint.setColor(Color.parseColor("#FFFFFF"));
        cornerPaint.setStrokeWidth(lineThicknessPx);
        cornerPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        return cornerPaint;
    }
    public static float getCornerThickness() {
        return DEFAULT_CORNER_THICKNESS_DP;
    }

    public static float getLineThickness() {
        return DEFAULT_LINE_THICKNESS_DP;
    }

}
