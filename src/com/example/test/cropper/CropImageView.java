package com.example.test.cropper;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.ExifInterface;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.example.test.CropperImage;
import com.example.test.R;
import com.example.test.cropper.cropwindow.CropOverlayView;
import com.example.test.cropper.cropwindow.edge.Edge;
import com.example.test.cropper.util.ImageViewUtil;

public class CropImageView extends FrameLayout {
    private static final Rect EMPTY_RECT = new Rect();

    public static final int DEFAULT_GUIDELINES = 1;
    public static final boolean DEFAULT_FIXED_ASPECT_RATIO = false;
    public static final int DEFAULT_ASPECT_RATIO_X = 1;
    public static final int DEFAULT_ASPECT_RATIO_Y = 1;

    private static final int DEFAULT_IMAGE_RESOURCE = 0;

    private static final String DEGREES_ROTATED = "DEGREES_ROTATED";

    private ImageView mImageView;
    private CropOverlayView mCropOverlayView;

    private Bitmap mBitmap;
    private int mDegreesRotated = 0;

    private int mLayoutWidth;
    private int mLayoutHeight;

    private int mGuidelines = DEFAULT_GUIDELINES;
    private boolean mFixAspectRatio = DEFAULT_FIXED_ASPECT_RATIO;
    private int mAspectRatioX = DEFAULT_ASPECT_RATIO_X;
    private int mAspectRatioY = DEFAULT_ASPECT_RATIO_Y;
    private int mImageResource = DEFAULT_IMAGE_RESOURCE;


    public CropImageView(Context context) {
        super(context);
        init(context);
    }

    public CropImageView(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.CropImageView, 0, 0);

        try {
            mGuidelines = ta.getInteger(R.styleable.CropImageView_guidelines, DEFAULT_GUIDELINES);
            mFixAspectRatio = ta.getBoolean(R.styleable.CropImageView_fixAspectRatio,
                                            DEFAULT_FIXED_ASPECT_RATIO);
            mAspectRatioX = ta.getInteger(R.styleable.CropImageView_aspectRatioX, DEFAULT_ASPECT_RATIO_X);
            mAspectRatioY = ta.getInteger(R.styleable.CropImageView_aspectRatioY, DEFAULT_ASPECT_RATIO_Y);
            mImageResource = ta.getResourceId(R.styleable.CropImageView_imageResource, DEFAULT_IMAGE_RESOURCE);
        } finally {
            ta.recycle();
        }

        init(context);
    }


    @Override
    public Parcelable onSaveInstanceState() {

        final Bundle bundle = new Bundle();

        bundle.putParcelable("instanceState", super.onSaveInstanceState());
        bundle.putInt(DEGREES_ROTATED, mDegreesRotated);

        return bundle;

    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {

        if (state instanceof Bundle) {

            final Bundle bundle = (Bundle) state;

            if (mBitmap != null) {
                mDegreesRotated = bundle.getInt(DEGREES_ROTATED);
                int tempDegrees = mDegreesRotated;
                rotateImage(mDegreesRotated);
                mDegreesRotated = tempDegrees;
            }

            super.onRestoreInstanceState(bundle.getParcelable("instanceState"));

        } else {
            super.onRestoreInstanceState(state);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {

        if (mBitmap != null) {
            final Rect bitmapRect = ImageViewUtil.getBitmapRectCenterInside(mBitmap, this);
            mCropOverlayView.setBitmapRect(bitmapRect);
        } else {
            mCropOverlayView.setBitmapRect(EMPTY_RECT);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        if (mBitmap != null) {

            super.onMeasure(widthMeasureSpec, heightMeasureSpec);

            if (heightSize == 0)
                heightSize = mBitmap.getHeight();

            int desiredWidth;
            int desiredHeight;

            double viewToBitmapWidthRatio = Double.POSITIVE_INFINITY;
            double viewToBitmapHeightRatio = Double.POSITIVE_INFINITY;

            if (widthSize < mBitmap.getWidth()) {
                viewToBitmapWidthRatio = (double) widthSize / (double) mBitmap.getWidth();
            }
            if (heightSize < mBitmap.getHeight()) {
                viewToBitmapHeightRatio = (double) heightSize / (double) mBitmap.getHeight();
            }

            if (viewToBitmapWidthRatio != Double.POSITIVE_INFINITY || viewToBitmapHeightRatio != Double.POSITIVE_INFINITY) {
                if (viewToBitmapWidthRatio <= viewToBitmapHeightRatio) {
                    desiredWidth = widthSize;
                    desiredHeight = (int) (mBitmap.getHeight() * viewToBitmapWidthRatio);
                } else {
                    desiredHeight = heightSize;
                    desiredWidth = (int) (mBitmap.getWidth() * viewToBitmapHeightRatio);
                }
            }

            else {
                desiredWidth = mBitmap.getWidth();
                desiredHeight = mBitmap.getHeight();
            }

            int width = getOnMeasureSpec(widthMode, widthSize, desiredWidth);
            int height = getOnMeasureSpec(heightMode, heightSize, desiredHeight);

            mLayoutWidth = width;
            mLayoutHeight = height;

            final Rect bitmapRect = ImageViewUtil.getBitmapRectCenterInside(mBitmap.getWidth(),
                                                                            mBitmap.getHeight(),
                                                                            mLayoutWidth,
                                                                            mLayoutHeight);
            mCropOverlayView.setBitmapRect(bitmapRect);

            setMeasuredDimension(mLayoutWidth, mLayoutHeight);

        } else {

            mCropOverlayView.setBitmapRect(EMPTY_RECT);
            setMeasuredDimension(widthSize, heightSize);
        }
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {

        super.onLayout(changed, l, t, r, b);

        if (mLayoutWidth > 0 && mLayoutHeight > 0) {
            final ViewGroup.LayoutParams origparams = this.getLayoutParams();
            origparams.width = mLayoutWidth;
            origparams.height = mLayoutHeight;
            setLayoutParams(origparams);
        }
    }


    public int getImageResource() {
        return mImageResource;
    }

    public void setImageBitmap(Bitmap bitmap) {

        mBitmap = bitmap;
        mImageView.setImageBitmap(mBitmap);

        if (mCropOverlayView != null) {
            mCropOverlayView.resetCropOverlayView();
        }
    }

    public void setImageBitmap(Bitmap bitmap, ExifInterface exif) {

        if (bitmap == null) {
            return;
        }

        if (exif == null) {
            setImageBitmap(bitmap);
            return;
        }

        final Matrix matrix = new Matrix();
        final int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
        int rotate = -1;

        Log.e("rotate", "rotate--->"+rotate);
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_270:
                rotate = 270;
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                rotate = 180;
                break;
            case ExifInterface.ORIENTATION_ROTATE_90:
                rotate = 90;
                break;
        }

        if (rotate == -1) {
            setImageBitmap(bitmap);
        } else {
            matrix.postRotate(rotate);
            final Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap,
                                                             0,
                                                             0,
                                                             bitmap.getWidth(),
                                                             bitmap.getHeight(),
                                                             matrix,
                                                             true);
            setImageBitmap(rotatedBitmap);
            bitmap.recycle();
        }
    }

    public void setImageResource(int resId) {
        if (resId != 0) {
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), resId);
            setImageBitmap(bitmap);
        }
    }

    public CropperImage getCroppedImage() {

        final Rect displayedImageRect = ImageViewUtil.getBitmapRectCenterInside(mBitmap, mImageView);

        final float actualImageWidth = mBitmap.getWidth();
        final float displayedImageWidth = displayedImageRect.width();
        final float scaleFactorWidth = actualImageWidth / displayedImageWidth;

        final float actualImageHeight = mBitmap.getHeight();
        final float displayedImageHeight = displayedImageRect.height();
        final float scaleFactorHeight = actualImageHeight / displayedImageHeight;

        final float cropWindowX = Edge.LEFT.getCoordinate() - displayedImageRect.left;
        final float cropWindowY = Edge.TOP.getCoordinate() - displayedImageRect.top;
        final float cropWindowWidth = Edge.getWidth();
        final float cropWindowHeight = Edge.getHeight();

        final float actualCropX = cropWindowX * scaleFactorWidth;
        final float actualCropY = cropWindowY * scaleFactorHeight;
        final float actualCropWidth = cropWindowWidth * scaleFactorWidth;
        final float actualCropHeight = cropWindowHeight * scaleFactorHeight;

        final Bitmap croppedBitmap = Bitmap.createBitmap(mBitmap,
                                                         (int) actualCropX,
                                                         (int) actualCropY,
                                                         (int) actualCropWidth,
                                                         (int) actualCropHeight);
        CropperImage cropperImage = new CropperImage();
        cropperImage.setBitmap(croppedBitmap);
        cropperImage.setX(Edge.LEFT.getCoordinate());
        cropperImage.setY(Edge.TOP.getCoordinate());
        cropperImage.setWidth(cropWindowWidth);
        cropperImage.setHeight(cropWindowHeight);
        return cropperImage;
    }

    public RectF getActualCropRect() {

        final Rect displayedImageRect = ImageViewUtil.getBitmapRectCenterInside(mBitmap, mImageView);

        final float actualImageWidth = mBitmap.getWidth();
        final float displayedImageWidth = displayedImageRect.width();
        final float scaleFactorWidth = actualImageWidth / displayedImageWidth;

        final float actualImageHeight = mBitmap.getHeight();
        final float displayedImageHeight = displayedImageRect.height();
        final float scaleFactorHeight = actualImageHeight / displayedImageHeight;

        final float displayedCropLeft = Edge.LEFT.getCoordinate() - displayedImageRect.left;
        final float displayedCropTop = Edge.TOP.getCoordinate() - displayedImageRect.top;
        final float displayedCropWidth = Edge.getWidth();
        final float displayedCropHeight = Edge.getHeight();

        float actualCropLeft = displayedCropLeft * scaleFactorWidth;
        float actualCropTop = displayedCropTop * scaleFactorHeight;
        float actualCropRight = actualCropLeft + displayedCropWidth * scaleFactorWidth;
        float actualCropBottom = actualCropTop + displayedCropHeight * scaleFactorHeight;

        actualCropLeft = Math.max(0f, actualCropLeft);
        actualCropTop = Math.max(0f, actualCropTop);
        actualCropRight = Math.min(mBitmap.getWidth(), actualCropRight);
        actualCropBottom = Math.min(mBitmap.getHeight(), actualCropBottom);

        final RectF actualCropRect = new RectF(actualCropLeft,
                                               actualCropTop,
                                               actualCropRight,
                                               actualCropBottom);

        return actualCropRect;
    }

    public void setFixedAspectRatio(boolean fixAspectRatio) {
        mCropOverlayView.setFixedAspectRatio(fixAspectRatio);
    }

    public void setGuidelines(int guidelines) {
        mCropOverlayView.setGuidelines(guidelines);
    }

    public void setAspectRatio(int aspectRatioX, int aspectRatioY) {
        mAspectRatioX = aspectRatioX;
        mCropOverlayView.setAspectRatioX(mAspectRatioX);

        mAspectRatioY = aspectRatioY;
        mCropOverlayView.setAspectRatioY(mAspectRatioY);
    }

    public void rotateImage(int degrees) {

        Matrix matrix = new Matrix();
        matrix.postRotate(degrees);
        mBitmap = Bitmap.createBitmap(mBitmap, 0, 0, mBitmap.getWidth(), mBitmap.getHeight(), matrix, true);
        setImageBitmap(mBitmap);

        mDegreesRotated += degrees;
        mDegreesRotated = mDegreesRotated % 360;
    }


    private void init(Context context) {

        final LayoutInflater inflater = LayoutInflater.from(context);
        final View v = inflater.inflate(R.layout.crop_image_view, this, true);

        mImageView = (ImageView) v.findViewById(R.id.ImageView_image);

        setImageResource(mImageResource);
        mCropOverlayView = (CropOverlayView) v.findViewById(R.id.CropOverlayView);
        mCropOverlayView.setInitialAttributeValues(mGuidelines, mFixAspectRatio, mAspectRatioX, mAspectRatioY);
    }

    private static int getOnMeasureSpec(int measureSpecMode, int measureSpecSize, int desiredSize) {

        int spec;
        if (measureSpecMode == MeasureSpec.EXACTLY) {
            spec = measureSpecSize;
        } else if (measureSpecMode == MeasureSpec.AT_MOST) {
            spec = Math.min(desiredSize, measureSpecSize);
        } else {
            spec = desiredSize;
        }

        return spec;
    }

}
