package com.vitaviva.commongallery.widget;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.animation.Interpolator;
import android.widget.Scroller;

import com.vitaviva.commongallery.util.ImageUtil;
import com.vitaviva.commongallery.util.OsVersionUtils;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


/**
 * 该类是图片展示工具类，接受本地资源和app资源图片
 */
public class ImageBoothView extends android.support.v7.widget.AppCompatImageView {
    private static final String TAG = "ImageBoothView";
    private static final float ENABLE_START_ROTATE_SCALE = 0.9f;//当缩放比例达到0.9，可以进行旋转，这里只是一个起始限制
    private static final float DEFAULT_MIN_SCALE = 0.25f;
    private static final float DEFAULT_MAX_SCALE = 3.0f;
    private static final float DEFAULT_ANIM_TIME = 280.0f;

    private static final int FILL_TYPE_FIT_TOP = 1;
    private static final int FILL_TYPE_FIT_CENTER = 2;
    private static final int FILL_TYPE_CENTER_CROP = 3;
    private static final int FILL_TYPE_CENTER_INSIDE = 4;
    private static final int FILL_TYPE_CENTER = 5;

    private static final int ADVANCE_BITMAP_WIDTH = 480;
    private static final int ADVANCE_BITMAP_HEIGHT = 800;

    private static final int ON_LONG_CLICK_MSG = 0x0101;
    private static final int ON_DOUBLE_CLICK_MSG = 0x0102;
    private static final int ON_SCALE_MSG = 0x0103;
    private static final int ON_CLICK_MSG = 0x0104;
    private static final int ON_FLING_MSG = 0x0105;
    private static final int IS_RESPONSEING_MSG = 0x0106;

    private static int MAX_BITMAP_SIZE = ADVANCE_BITMAP_WIDTH * ADVANCE_BITMAP_HEIGHT;

    private Matrix mMatrix;
    private Matrix matchViewMatrix_0;
    private Matrix matchViewMatrix_90;
    private Matrix matchViewMatrix_180;
    private Matrix matchViewMatrix_270;

    private GestureDetector mGestureDetector;
    private ScaleGestureDetector mScaleDetector;

    private float mNormalizedScale = 1;//当前的标准缩放比例
    private float HorizontalMatchScreenScale;//图片未旋转是铺满屏幕的缩放比例
    private float verticalMatchScreenScale;//图片旋转90度铺满屏幕的缩放比例

    private float mCurrentScale;
    private float mMaxScale = DEFAULT_MAX_SCALE;
    private float mDoubleClickScale;

    private int mViewHeight;
    private int mViewWidth;//可供ImageView展示的宽高
    private int mOriginalDrawableWidth;
    private int mOriginalDrawableHeight;

    private int mDrawableWidth;
    private int mDrawableHeight;

    private float mMatchedImageWidthWhenHorizontal;
    private float mMatchedImageHeightWhenHorizontal;
    private State mState = State.NONE;
    private float MIDDLE_SCALE_HORIZIONAL = 1.0f;
    private float MIDDLE_SCALE_VERTIVAL = 1.0f;
    private boolean mIsRotateEnabled;
    private boolean mIsResuming;//是否正在执行恢复图形操作
    private boolean mIsDealDoubleRunning;
    private Fling mFling;
    private int mDegree;//当前的旋转角度
    private boolean mIsDragging;
    private PointF mActionDownPoint = new PointF();
    private PointF mActionUpPoint = new PointF();
    /**
     * 需要自动旋转的角度
     */
    private float remainDegree;
    private boolean mReachLeft;
    private boolean mReachTop;
    private boolean mReachRight;

    private OnClickListener onClickListener;

    private OnLongClickListener onLongClickListener;

    private boolean isNeedRefuseMsgInTwoMinutes = true;

    private boolean isAttachedToWindow;

    private static final Handler staticHandlerForResonse = new Handler();

    private final Handler responseHandler = new Handler() {
        @Override
        public void dispatchMessage(Message msg) {
            super.dispatchMessage(msg);
            if (staticHandlerForResonse.hasMessages(IS_RESPONSEING_MSG)) {
                removeCallbacksAndMessages(null);
                return;
            }
            switch (msg.what) {
                case ON_CLICK_MSG:
                    if (onClickListener != null) {
                        if (onLongClickListener != null && isNeedRefuseMsgInTwoMinutes)//如果没有长按显示对话框的功能，单击事件不干扰其他消息
                        {
                            staticHandlerForResonse
                                    .sendEmptyMessageDelayed(IS_RESPONSEING_MSG, 2000);
                        }
                        removeCallbacksAndMessages(null);
                        onClickListener.onClick(ImageBoothView.this);
                    }

                    break;
                case ON_LONG_CLICK_MSG:
                    if (onLongClickListener != null && isNeedRefuseMsgInTwoMinutes) {
                        removeCallbacksAndMessages(null);
                        staticHandlerForResonse.sendEmptyMessageDelayed(IS_RESPONSEING_MSG, 2000);
                        onLongClickListener.onLongClick(ImageBoothView.this);
                    }
                    break;
                default:
                    break;
            }
        }
    };

    public ImageBoothView(Context context) {
        this(context, null);
    }

    public ImageBoothView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public static void setMaxBitmapPx(int maxPx) {
        MAX_BITMAP_SIZE = maxPx;
    }

    private static PointF getCenterWithTwoPoint(PointF p1, PointF p2) {
        return new PointF((p1.x + p2.x) / 2, (p1.y + p2.y) / 2);
    }

    private static float[] getMatrixValues(Matrix matrix) {
        float[] value = new float[9];
        matrix.getValues(value);
        return value;
    }

    private static int computeSampleSize(BitmapFactory.Options options, int minSideLength,
                                         int maxNumOfPixels) {
        int initialSize = computeInitialSampleSize(options, minSideLength, maxNumOfPixels);
        int roundedSize;
        if (initialSize <= 8) {
            roundedSize = 1;
            while (roundedSize < initialSize) {
                roundedSize <<= 1;
            }
        } else {
            roundedSize = (initialSize + 7) / 8 * 8;
        }
        return roundedSize;
    }

    private static int computeInitialSampleSize(BitmapFactory.Options options, int minSideLength,
                                                int maxNumOfPixels) {
        double w = options.outWidth;
        double h = options.outHeight;
        int lowerBound =
                maxNumOfPixels == -1 ? 1 : (int) Math.ceil(Math.sqrt(w * h / maxNumOfPixels));
        int upperBound = minSideLength == -1 ? 128 : (int) Math
                .min(Math.floor(w / minSideLength), Math.floor(h / minSideLength));
        if (upperBound < lowerBound) {
            return lowerBound;
        }
        if (maxNumOfPixels == -1 && minSideLength == -1) {
            return 1;
        } else if (minSideLength == -1) {
            return lowerBound;
        } else {
            return upperBound;
        }
    }

    /**
     * 判断某个点是否在两点之间
     */
    private static boolean isBetweenInTwoPoint(PointF center, PointF p1, PointF p2) {
        int error_limit = 10;
        return center.x > p1.x - error_limit && center.x < p2.x + error_limit && center.y < p1.y + error_limit && center.y > p2.y - error_limit
                || center.x < p1.x + error_limit && center.x > p2.x - error_limit && center.y > p1.y - error_limit && center.y < p2.y + error_limit
                || center.x > p1.x - error_limit && center.x < p2.x + error_limit && center.y > p1.y - error_limit && center.y < p2.y + error_limit
                || center.x < p1.x + error_limit && center.x > p2.x - error_limit && center.y < p1.y + error_limit && center.y > p2.y - error_limit;
    }

    //    @TargetApi(Build.VERSION_CODES.KITKAT)
    @SuppressLint("NewApi")
    private void init(Context context) {
        mMatrix = new Matrix();
        matchViewMatrix_0 = new Matrix();
        matchViewMatrix_90 = new Matrix();
        matchViewMatrix_180 = new Matrix();
        matchViewMatrix_270 = new Matrix();
        mGestureDetector = new GestureDetector(context,
                new GestureListener());
        mScaleDetector = new ScaleGestureDetector(context,
                new ScaleListener());
        if (OsVersionUtils.hasKitKat()) {
            mScaleDetector.setQuickScaleEnabled(false);
        }
        setScaleType(ScaleType.MATRIX);
        setOnTouchListener(new TouchListener());
    }

    public void setListener(OnClickListener onClickListener,
                            OnLongClickListener onLongClickListener) {
        if (onClickListener != null || onLongClickListener != null) {
            this.onClickListener = onClickListener;
            this.onLongClickListener = onLongClickListener;
        }
    }

    @Override
    public void setImageResource(int resId) {
        try {
            super.setImageBitmap(getUsableBitmap(resId));
        } catch (Exception ignored) {
        }

    }

    public void setImageBitmap(String filePath) {
        try {
            Observable<Bitmap> bmpObservable = getUsableBitmap(filePath);
            if (bmpObservable == null) {
                return;
            }
            bmpObservable.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(bmp -> super.setImageBitmap(bmp));
        } catch (Exception ignored) {
        }
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        super.setImageBitmap(bm);
        needShutdownHardwareAcc(bm);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    private boolean needShutdownHardwareAcc(Bitmap bmp) {
        if (bmp == null) {
            return false;
        }
        int glesLimit = ImageUtil.getGLESTextureLimit();
        if (glesLimit < bmp.getWidth() || glesLimit < bmp.getHeight()) {
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            return true;
        }
        setLayerType(View.LAYER_TYPE_HARDWARE, null);
        return false;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(width, height);
        boolean isInitMeasure = false;
        if (mViewWidth == 0 || mViewHeight == 0) {
            mViewWidth = width;
            mViewHeight = height;
            isInitMeasure = true;
        }
        Drawable drawable = getDrawable();
        if (drawable == null) {
            return;
        }
        int drawableWidth = drawable.getIntrinsicWidth();
        int drawableHeight = drawable.getIntrinsicHeight();
        if (mDrawableWidth == 0 || mDrawableHeight == 0) {
            mDrawableWidth = drawableWidth;
            mDrawableHeight = drawableHeight;
            isInitMeasure = true;
        }
        if (!isInitMeasure && mViewWidth == width && mViewHeight == height && mDrawableWidth == drawableWidth && mDrawableHeight == drawableHeight) {
            return;
        }
        mViewWidth = width;
        mViewHeight = height;
        mDrawableWidth = drawableWidth;
        mDrawableHeight = drawableHeight;


        final int dwidth = mDrawableWidth;
        final int dheight = mDrawableHeight;

        final int vwidth = mViewWidth;
        final int vheight = mViewHeight;

        float scale1 = (float) mViewWidth / mDrawableWidth;
        float scale2 = (float) mViewHeight / mDrawableHeight;
        HorizontalMatchScreenScale = Math.min(scale1, scale2);
        MIDDLE_SCALE_HORIZIONAL = Math.max(scale1, scale2);

        int fillType = getFillType();
        float dx = 0;
        float dy = 0;
        float scale = calcScale(fillType);
        setNormalizedScale(1.0f);
        mCurrentScale = scale;
        HorizontalMatchScreenScale = scale;
        if (fillType == FILL_TYPE_FIT_TOP) {
            dx = Math.round((vwidth - dwidth * scale) * 0.5f);
        } else if (fillType == FILL_TYPE_FIT_CENTER) {
            dx = Math.round((vwidth - dwidth * scale) * 0.5f);
            dy = Math.round((vheight - dheight * scale) * 0.5f);
        } else if (fillType == FILL_TYPE_CENTER) {
            dx = Math.round((vwidth - dwidth * scale) * 0.5f);
            dy = Math.round((vheight - dheight * scale) * 0.5f);
        } else if (fillType == FILL_TYPE_CENTER_CROP) {
            dx = Math.round((vwidth - dwidth * scale) * 0.5f);
            dy = Math.round((vheight - dheight * scale) * 0.5f);
        }

        if (matchViewMatrix_0 != null) {
            matchViewMatrix_0.reset();
            matchViewMatrix_0.setScale(scale, scale);
            matchViewMatrix_0.postTranslate(dx, dy);
        }

        mMatchedImageWidthWhenHorizontal = mDrawableWidth * HorizontalMatchScreenScale;
        mMatchedImageHeightWhenHorizontal = mDrawableHeight * HorizontalMatchScreenScale;
        if (matchViewMatrix_180 != null) {
            matchViewMatrix_180.reset();
            matchViewMatrix_180.setScale(HorizontalMatchScreenScale, HorizontalMatchScreenScale);
            matchViewMatrix_180.postTranslate((mViewWidth - mMatchedImageWidthWhenHorizontal) / 2f,
                    (mViewHeight - mMatchedImageHeightWhenHorizontal) / 2f);
            matchViewMatrix_180.postRotate(180, mViewWidth / 2f, mViewHeight / 2f);
        }
        float scale3 = (float) mViewWidth / mDrawableHeight;
        float scale4 = (float) mViewHeight / mDrawableWidth;
        //verticalMatchScreenScale = Math.min(scale3, scale4);
        verticalMatchScreenScale = HorizontalMatchScreenScale;
        MIDDLE_SCALE_VERTIVAL = Math.max(scale3, scale4);
        float mMatchedImageWidthWhenVertical = mDrawableWidth * verticalMatchScreenScale;
        float mMatchedImageHeightWhenVertical = mDrawableHeight * verticalMatchScreenScale;
        if (matchViewMatrix_90 != null) {
            matchViewMatrix_90.reset();
            matchViewMatrix_90.setScale(verticalMatchScreenScale, verticalMatchScreenScale);
            matchViewMatrix_90.postTranslate((mViewWidth - mMatchedImageWidthWhenVertical) / 2f,
                    (mViewHeight - mMatchedImageHeightWhenVertical) / 2f);
            matchViewMatrix_90.postRotate(90, mViewWidth / 2f, mViewHeight / 2f);
        }
        if (matchViewMatrix_270 != null) {
            matchViewMatrix_270.reset();
            matchViewMatrix_270.setScale(verticalMatchScreenScale, verticalMatchScreenScale);
            matchViewMatrix_270.postTranslate((mViewWidth - mMatchedImageWidthWhenVertical) / 2f,
                    (mViewHeight - mMatchedImageHeightWhenVertical) / 2f);
            matchViewMatrix_270.postRotate(270, mViewWidth / 2f, mViewHeight / 2f);
        }
        fitImageToViewWhenHorizionl(matchViewMatrix_0);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        isAttachedToWindow = true;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        isAttachedToWindow = false;
        responseHandler.removeCallbacksAndMessages(null);
    }

    /**
     * 使图片适应屏幕
     */
    private void fitImageToViewWhenHorizionl(Matrix matrix) {
        if (mMatrix == null) {
            return;
        }
        mMatrix.reset();
        mMatrix.setValues(getMatrixValues(matrix));
        setImageMatrix(mMatrix);
    }

    /**
     * 缩放图片
     */
    private void scaleImage(float deltaScale, float focusX, float focusY, boolean useCoor) {
        float deltaScale1 = deltaScale;

        float origScale = mNormalizedScale;
        setNormalizedScale(mNormalizedScale * deltaScale);
        float lowerScale = DEFAULT_MIN_SCALE;
        float upperScale = mMaxScale;
        if (mNormalizedScale > upperScale) {
            setNormalizedScale(upperScale);
            deltaScale1 = upperScale / origScale;
        } else if (mNormalizedScale < lowerScale) {
            setNormalizedScale(lowerScale);
            deltaScale1 = lowerScale / origScale;
        }

        if (useCoor) {
            mMatrix.postScale(deltaScale1, deltaScale1, focusX, focusY);
        } else {
            mMatrix.postScale(deltaScale1, deltaScale1);
        }
        if (mNormalizedScale < ENABLE_START_ROTATE_SCALE) {
            mIsRotateEnabled = true;
        }
        setImageMatrix(mMatrix);
    }

    private void postRunnable(Runnable runnable) {
        postOnAnimation(this, runnable);
    }

    public static void postOnAnimation(View view, Runnable runnable) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            postOnAnimationJellyBean(view, runnable);
        } else {
            view.postDelayed(runnable, 1000 / 60);
        }
    }

    @TargetApi(16)
    private static void postOnAnimationJellyBean(View view, Runnable runnable) {
        view.postOnAnimation(runnable);
    }

    public boolean isNeedRefuseMsgInTwoMinutes() {
        return isNeedRefuseMsgInTwoMinutes;
    }

    public void setIsNeedRefuseMsgInTwoMinutes(boolean isNeedRefuseMsgInTwoMinutes) {
        this.isNeedRefuseMsgInTwoMinutes = isNeedRefuseMsgInTwoMinutes;
    }

    /**
     * 移动图片，使适应屏幕
     */
    private void fixTranslation(float deltaX, float deltaY) {
        float[] matrixValues = getMatrixValues(mMatrix);
        float transX = matrixValues[Matrix.MTRANS_X] + deltaX;
        float transY = matrixValues[Matrix.MTRANS_Y] + deltaY;
        Size size = getImageSize(mNormalizedScale);
        transX = getFixTrans(transX, size.getWidth(), mViewWidth, true);
        transY = getFixTrans(transY, size.getHeight(), mViewHeight, false);
        matrixValues[Matrix.MTRANS_X] = transX;
        matrixValues[Matrix.MTRANS_Y] = transY;
        mMatrix.setValues(matrixValues);
        setImageMatrix(mMatrix);
    }

    private float getFixTrans(float transSize, float contentSize,
                              float viewSize, boolean isWidth) {
        return getFixTrans(transSize, contentSize, viewSize, isWidth, true);
    }

    /**
     * 得到合适的偏移量
     */
    private float getFixTrans(float transSize, float contentSize,
                              float viewSize, boolean isWidth, boolean fixed) {
        int degree = 90;
        if (!isWidth) {
            degree = 270;
        }
        if (viewSize >= contentSize) {
            transSize = (viewSize - contentSize) / 2;
            if (mDegree == 180 || mDegree == degree) {
                transSize += contentSize;
            }
            if (isWidth) {
                mReachRight = true;
                mReachLeft = true;
            }
        } else {
            float minSize = viewSize - contentSize;
            float maxSize = 0;
            if (mDegree == 180 || mDegree == degree) {
                minSize += contentSize;
                maxSize += contentSize;
            }
            if (isWidth) {
                if (transSize >= maxSize) {
                    mReachRight = true;
                    mReachLeft = false;
                } else if (transSize <= minSize) {
                    mReachLeft = true;
                    mReachRight = false;
                } else {
                    mReachRight = false;
                    mReachLeft = false;
                }
            }
            if (fixed) {
                transSize = transSize > maxSize ? maxSize : transSize;
                transSize = transSize < minSize ? minSize : transSize;
            }
        }

        if (!isWidth) {
            mReachTop = (transSize >= 0);
        }
        return transSize;
    }

    /**
     * 获取当前图片的宽高，考虑了图片的旋转角度
     */
    private Size getImageSize(float scale) {
        Size size;
        if (mDegree == 0 || mDegree == 180) {
            size = new Size(mMatchedImageWidthWhenHorizontal * scale,
                    mMatchedImageHeightWhenHorizontal * scale);
        } else {
            size = new Size(mMatchedImageHeightWhenHorizontal * scale,
                    mMatchedImageWidthWhenHorizontal * scale);
        }
        return size;
    }

    @Nullable
    public static Observable<Bitmap> getUsableBitmap(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return null;
        }
        return Observable.just(filePath).map(path -> {
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(filePath, opts);
            if (opts.outWidth * opts.outHeight > MAX_BITMAP_SIZE) {
                opts.inJustDecodeBounds = false;
                opts.inSampleSize = computeSampleSize(opts, -1, MAX_BITMAP_SIZE);
                return BitmapFactory.decodeFile(filePath, opts);
            } else {
                opts.inJustDecodeBounds = false;
                return BitmapFactory.decodeFile(filePath, opts);
            }
        });
    }

    private Bitmap getUsableBitmap(int resid) throws Exception {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(getResources(), resid, opts);
        if (opts.outWidth * opts.outHeight > MAX_BITMAP_SIZE) {
            opts.inJustDecodeBounds = false;
            opts.inSampleSize = computeSampleSize(opts, -1, MAX_BITMAP_SIZE);
            return BitmapFactory.decodeResource(getResources(), resid, opts);
        } else {
            opts.inJustDecodeBounds = false;
            return BitmapFactory.decodeResource(getResources(), resid);
        }
    }

    public int getOriginalDrawableWidth() {
        return mOriginalDrawableWidth;
    }

    public void setOriginalDrawableWidth(int originalDrawableWidth) {
        this.mOriginalDrawableWidth = originalDrawableWidth;
    }

    public int getOriginalDrawableHeight() {
        return mOriginalDrawableHeight;
    }

    public void setOriginalDrawableHeight(int originalDrawableHeight) {
        this.mOriginalDrawableHeight = originalDrawableHeight;
    }

    public void setNormalizedScale(float normalizedScale) {
        this.mNormalizedScale = normalizedScale;
    }

    private int getFillType() {
        int dwidth = mOriginalDrawableWidth;
        int dheight = mOriginalDrawableHeight;
        if (dwidth == 0) {
            dwidth = mDrawableWidth;
        }
        if (dheight == 0) {
            dheight = mDrawableHeight;
        }

        final int vwidth = mViewWidth;
        final int vheight = mViewHeight;

        if (dheight > vheight) {
            float dAspect = (float) dwidth / dheight;
            float vAspect = (float) vwidth / vheight;
            if (dAspect < 1.0f / 2.5) { // 长图
                return FILL_TYPE_FIT_TOP;
            } /*else if (dAspect >= 1.0f / 2.5 && dAspect < vAspect) {
                return FILL_TYPE_FIT_CENTER;
            } else if (dAspect >= vAspect){
                return FILL_TYPE_FIT_CENTER;
            }*/
        }

        if (dwidth < vwidth) {
            if (dheight <= vheight) {
                float dAspect = (float) dwidth / dheight;
                if (dwidth <= 200 && dheight <= 200) {
                    return FILL_TYPE_CENTER;
                } else if (dAspect < 1.0f / 3) {
                    return FILL_TYPE_FIT_TOP;
                }
            }
        } /*else if (dwidth == vwidth) {
            if (dheight == vheight) {
                return FILL_TYPE_FIT_CENTER;
            } else if (dheight < vheight) {
                return FILL_TYPE_FIT_CENTER;
            }
        }*/ else if (dwidth > vwidth) {
            if (dheight <= vheight) {
                return FILL_TYPE_FIT_CENTER;
            } else {
                if ((float) dwidth / dheight < 1.0f / 2.5) {
                    return FILL_TYPE_FIT_TOP;
                }
            }
        }

        return FILL_TYPE_FIT_CENTER;
    }

    private float calcScale(int fillType) {
        final int dwidth = mDrawableWidth;
        final int dheight = mDrawableHeight;

        final int vwidth = mViewWidth;
        final int vheight = mViewHeight;
        float scale = Math.min((float) vwidth / (float) dwidth,
                (float) vheight / (float) dheight);
        if (fillType == FILL_TYPE_FIT_TOP) {
            scale = (float) vwidth / (float) dwidth;
            mDoubleClickScale = scale + 2;
            mMaxScale = mDoubleClickScale + 1;
        } else if (fillType == FILL_TYPE_FIT_CENTER) {
            scale = Math.min((float) vwidth / (float) dwidth,
                    (float) vheight / (float) dheight);
            mDoubleClickScale = Math.max((float) vwidth / (float) dwidth,
                    (float) vheight / (float) dheight);
            if (Math.abs(mDoubleClickScale - scale) < 0.25f) {
                mDoubleClickScale = scale + 2;
                mMaxScale = mDoubleClickScale + 1;
            } else {
                mMaxScale = mDoubleClickScale + 2;
            }
        } else if (fillType == FILL_TYPE_CENTER) {
            if (dwidth <= 200 && dheight <= 200) {
                scale = Math.min((float) 250 / (float) dwidth,
                        (float) 250 / (float) dheight);
            } else {
                scale = 1.0f;
            }
            float fitCenterScale = Math.min((float) vwidth / (float) dwidth,
                    (float) vheight / (float) dheight);
            if (fitCenterScale > scale) {
                mDoubleClickScale = fitCenterScale;
                mMaxScale = mDoubleClickScale + 3;
            } else {
                mDoubleClickScale = scale + 2;
                mMaxScale = mDoubleClickScale + 1;
            }
        } else if (fillType == FILL_TYPE_CENTER_CROP) {
            scale = Math.max((float) vwidth / (float) dwidth,
                    (float) vheight / (float) dheight);
            mDoubleClickScale = scale + 2;
            mMaxScale = mDoubleClickScale + 1;
        }
        return scale;
    }

    private enum State {
        NONE, ONE_POINT, TWO_POINT,
    }

    private static class Size {
        private final float mWidth;
        private final float mHeight;

        Size(float width, float height) {
            mWidth = width;
            mHeight = height;
        }

        public float getWidth() {
            return mWidth;
        }

        public float getHeight() {
            return mHeight;
        }
    }

    private class TouchListener implements OnTouchListener {
        private final PointF lastP1 = new PointF();
        private final PointF lastP2 = new PointF();
        private final PointF curP1 = new PointF();
        private final PointF curP2 = new PointF();
        private float deltaX;
        private float deltaY;
        private float totalDeltaX;
        private float totalDeltaY;
        private PointF lastTwoPointerCoorP = new PointF(0, 0);
        private PointF movingTwoPointerCoorP = new PointF(0, 0);
        private final PointF lastP1_r = new PointF(0, 0);
        private final PointF lastP2_r = new PointF(0, 0);
        private final PointF curP1_r = new PointF(0, 0);
        private final PointF curP2_r = new PointF(0, 0);

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (!mIsResuming && !mIsDealDoubleRunning) {
                if ((mDegree == 0 || mDegree == 180) && Float.compare(mNormalizedScale,
                        1) == 0 || (mDegree == 90 || mDegree == 270) && Float
                        .compare(mNormalizedScale,
                                verticalMatchScreenScale / HorizontalMatchScreenScale) == 0) {//当前是适应屏幕的
                    if (getFillType() == FILL_TYPE_FIT_TOP) {
                        getParent().requestDisallowInterceptTouchEvent(true);
                    } else {
                        getParent().requestDisallowInterceptTouchEvent(false);
                    }
                } else {
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
                mScaleDetector.onTouchEvent(event);
                mGestureDetector.onTouchEvent(event);
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        if (mFling != null) {
                            mFling.cancelFling();
                        }
                        mState = State.ONE_POINT;
                        lastP1.set(event.getX(), event.getY());
                        mActionDownPoint.set(event.getX(), event.getY());
                        break;
                    case MotionEvent.ACTION_MOVE:
                        switch (mState) {
                            case ONE_POINT:
                                // 拖动
                                performDrag(event);
                                break;
                            case TWO_POINT:
                                if (event.getPointerCount() > 1) {
                                    performRotate(event);
                                }
                                break;
                            default:
                                break;
                        }
                        if ((mReachRight && deltaX > 0 || mReachLeft && deltaX < 0)
                                && Math.abs(totalDeltaX) >= 10) {
                            if (Math.abs(totalDeltaY) / Math.abs(totalDeltaX) < 0.1f) {
                                getParent().requestDisallowInterceptTouchEvent(false);
                            }
                        } else if (mReachTop && deltaY > 0 && Math.abs(totalDeltaY) >= 10) {
                            if (Math.abs(totalDeltaX) / Math.abs(totalDeltaY) < 0.1f) {
                                getParent().requestDisallowInterceptTouchEvent(false);
                            }
                        }
                        break;
                    case MotionEvent.ACTION_CANCEL:
                    case MotionEvent.ACTION_UP:
                        mState = State.NONE;
                        totalDeltaX = 0;
                        totalDeltaY = 0;
                        mIsDragging = false;
                        mActionUpPoint.set(event.getX(), event.getY());
                        break;
                    case MotionEvent.ACTION_POINTER_DOWN:
                        if (mState == State.ONE_POINT && event.getPointerCount() > 1) {
                            mState = State.TWO_POINT;
                            lastP2.set(event.getX(1), event.getY(1));
                        }
                        break;
                    case MotionEvent.ACTION_POINTER_UP:
                        mState = State.NONE;
                        lastTwoPointerCoorP.set(0, 0);
                        movingTwoPointerCoorP.set(0, 0);
                        lastP1_r.set(0, 0);
                        lastP2_r.set(0, 0);
                        curP1_r.set(0, 0);
                        curP2_r.set(0, 0);
                        float orgScale;
                        if (mDegree == 0) {
                            orgScale = HorizontalMatchScreenScale;
                        } else if (mDegree == 90) {
                            orgScale = verticalMatchScreenScale;
                        } else if (mDegree == 180) {
                            orgScale = HorizontalMatchScreenScale;
                        } else {
                            orgScale = verticalMatchScreenScale;
                        }
                        remainDegree = ModifyDegree(remainDegree);
                        float endScale;
                        if (mDegree == 0) {
                            endScale = HorizontalMatchScreenScale;
                        } else if (mDegree == 90) {
                            endScale = verticalMatchScreenScale;
                        } else if (mDegree == 180) {
                            endScale = HorizontalMatchScreenScale;
                        } else {
                            endScale = verticalMatchScreenScale;
                        }

                        Size size = getImageSize(mNormalizedScale);
                        float[] matrixValues = getMatrixValues(mMatrix);
                        float beginTransX = matrixValues[Matrix.MTRANS_X];
                        float beginTransY = matrixValues[Matrix.MTRANS_Y];
                        float endTransX = getFixTrans(beginTransX, size.getWidth(), mViewWidth,
                                true);
                        float endTransY = getFixTrans(beginTransY, size.getHeight(), mViewHeight,
                                false);
                        matrixValues[Matrix.MTRANS_X] = endTransX;
                        matrixValues[Matrix.MTRANS_Y] = endTransY;
                        mMatrix.setValues(matrixValues);
                        setImageMatrix(mMatrix);
                        if (remainDegree != 0 ||
                                mNormalizedScale < 1 && (mDegree == 0 || mDegree == 180) ||
                                mNormalizedScale < verticalMatchScreenScale / HorizontalMatchScreenScale && (mDegree == 90 || mDegree == 270)) {
                            Matrix targetMatrix = new Matrix();
                            if (mDegree == 0) {
                                targetMatrix.set(matchViewMatrix_0);
                            } else if (mDegree == 90) {
                                targetMatrix.set(matchViewMatrix_90);
                            } else if (mDegree == 180) {
                                targetMatrix.set(matchViewMatrix_180);
                            } else {
                                targetMatrix.set(matchViewMatrix_270);
                            }
                            Resume mResumeRunnable = new Resume(targetMatrix);
                            mResumeRunnable.start();
                        }

                        setNormalizedScale(mNormalizedScale * (endScale / orgScale));
                        remainDegree = 0;
                        mIsDragging = false;
                        break;
                    default:
                        break;
                }
            }
            return true;
        }

        /**
         * 拖动图片
         */
        private void performDrag(MotionEvent event) {
            responseHandler.removeMessages(ON_LONG_CLICK_MSG);
            curP1.set(event.getX(0), event.getY(0));
            deltaX = curP1.x - lastP1.x;
            deltaY = curP1.y - lastP1.y;
            totalDeltaX += Math.abs(deltaX);
            totalDeltaY += Math.abs(deltaY);
            if (Math.abs(totalDeltaX) >= 30 || Math.abs(totalDeltaY) >= 30) {
                if (!mIsDragging) {
                    mIsDragging = true;
                }
            }
            fixTranslation(deltaX, deltaY);
            lastP1.set(curP1);

        }

        /**
         * 移动图片
         */
        private void performTrans(MotionEvent event) {
            if (event.getPointerCount() < 2) {
                return;
            }
            curP1.set(event.getX(0), event.getY(0));
            curP2.set(event.getX(1), event.getY(1));
            if (lastTwoPointerCoorP.equals(0, 0)) {
                lastTwoPointerCoorP = getCenterWithTwoPoint(curP1, curP2);
            }
            movingTwoPointerCoorP = getCenterWithTwoPoint(curP1, curP2);
            if (!movingTwoPointerCoorP.equals(lastTwoPointerCoorP.x, lastTwoPointerCoorP.y)) {
                mMatrix.postTranslate(movingTwoPointerCoorP.x - lastTwoPointerCoorP.x,
                        movingTwoPointerCoorP.y - lastTwoPointerCoorP.y);
                lastTwoPointerCoorP.set(movingTwoPointerCoorP.x, movingTwoPointerCoorP.y);
                setImageMatrix(mMatrix);
            }
        }

        /**
         * 旋转图片
         */
        private void performRotate(MotionEvent event) {
            curP1_r.set(event.getX(0), event.getY(0));
            curP2_r.set(event.getX(1), event.getY(1));
            if (lastP1_r.equals(0, 0) || lastP2_r.equals(0, 0)) {
                lastP1_r.set(curP1_r);
                lastP2_r.set(curP2_r);
                return;
            }
            float curDegree = calculateDegree(lastP1_r, lastP2_r, curP1_r, curP2_r);
            if (Math.abs(curDegree) > 0 && mIsRotateEnabled) {

                PointF rotateCenter = getMeetingInTwoLine(lastP1_r, lastP2_r, curP1_r, curP2_r);
                if (isBetweenInTwoPoint(rotateCenter, lastP1_r, lastP2_r)) {
                    PointF rotateP = new PointF();
                    rotateP.set(rotateCenter);
                    mMatrix.postRotate(curDegree, rotateP.x, rotateP.y);
                    remainDegree += curDegree;
                } else {
                    performTrans(event);
                }

            } else {
                performTrans(event);
            }
            lastP1_r.set(curP1_r);
            lastP2_r.set(curP2_r);
            lastTwoPointerCoorP = getCenterWithTwoPoint(curP1_r, curP2_r);
            setImageMatrix(mMatrix);
        }

        private PointF getMeetingInTwoLine(PointF p1, PointF p2, PointF p3, PointF p4) {
            float x1 = p1.x;
            float y1 = p1.y;
            float x2 = p2.x;
            float y2 = p2.y;

            float x3 = p3.x;
            float y3 = p3.y;
            float x4 = p4.x;
            float y4 = p4.y;

            float x = ((x1 - x2) * (x3 * y4 - x4 * y3) - (x3 - x4) * (x1 * y2 - x2 * y1))
                    / ((x3 - x4) * (y1 - y2) - (x1 - x2) * (y3 - y4));

            float y = ((y1 - y2) * (x3 * y4 - x4 * y3) - (x1 * y2 - x2 * y1) * (y3 - y4))
                    / ((y1 - y2) * (x3 - x4) - (x1 - x2) * (y3 - y4));

            return new PointF(x, y);
        }

        /**
         * 根据两对坐标点计算旋转角度
         */
        private float calculateDegree(PointF a1, PointF b1, PointF a2, PointF b2) {
            double d1 = StrictMath.atan((b1.y - a1.y) / (b1.x - a1.x));
            double d2 = StrictMath.atan((b2.y - a2.y) / (b2.x - a2.x));
            d1 = (float) (d1 * 180 / Math.PI);
            d2 = (float) (d2 * 180 / Math.PI);
            float degree = (float) (d2 - d1);
            if (degree > 90) {
                degree = 180 - degree;
            } else if (degree < -90) {
                degree += 180;
            }
            return degree;
        }

        /**
         * 得到合适的旋转角度
         */
        private float ModifyDegree(float degree) {
            float degree1 = degree;
            while (degree1 < 0) {
                degree1 += 360;
            }
            degree1 %= 360;
            if (degree1 <= 45 || degree1 > 315) {
            } else if (degree1 > 45 && degree1 <= 135) {
                mDegree += 90;
            } else if (degree1 > 135 && degree1 <= 225) {
                mDegree += 180;
            } else {
                mDegree += 270;
            }
            mDegree %= 360;
            degree1 %= 90;
            degree1 = degree1 > 45 ? 90 - degree1 : -degree1;
            return degree1;
        }
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            scaleImage(detector.getScaleFactor(), detector.getFocusX(), detector.getFocusY(), true);
            if (responseHandler.hasMessages(ON_SCALE_MSG)) {
                responseHandler.removeMessages(ON_SCALE_MSG);
            }
            responseHandler.removeMessages(ON_CLICK_MSG);
            responseHandler.removeMessages(ON_FLING_MSG);
            responseHandler.removeMessages(ON_LONG_CLICK_MSG);
            responseHandler.removeMessages(ON_DOUBLE_CLICK_MSG);
            responseHandler.sendEmptyMessageDelayed(ON_SCALE_MSG, 200);
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            super.onScaleEnd(detector);
        }
    }

    private class GestureListener extends
            GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            if (Math.abs(mActionUpPoint.x - mActionDownPoint.x) >= 10 && Math.abs(mActionUpPoint.x - mActionDownPoint.y) >= 10) {
                return super.onSingleTapConfirmed(e);
            }
            if (!responseHandler.hasMessages(ON_SCALE_MSG) && !responseHandler
                    .hasMessages(ON_FLING_MSG) && !responseHandler.hasMessages(ON_LONG_CLICK_MSG)) {
                responseHandler.sendEmptyMessage(ON_CLICK_MSG);
            }
            return super.onSingleTapConfirmed(e);
        }

        @Override
        public void onLongPress(MotionEvent e) {
            if (responseHandler.hasMessages(ON_DOUBLE_CLICK_MSG)) {
                responseHandler.removeMessages(ON_LONG_CLICK_MSG);
            } else {
                responseHandler.removeMessages(ON_LONG_CLICK_MSG);
                if (!responseHandler.hasMessages(ON_CLICK_MSG) && !mIsDragging) {
                    //没有返回消息的时候
                    responseHandler.sendEmptyMessage(ON_LONG_CLICK_MSG);
                }
            }
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            if (getDrawable() == null)
                return true;
            float focusX = e.getX();
            float focusY = e.getY();
            // 双击图片缩放的最大或最小
            responseHandler.sendEmptyMessageDelayed(ON_DOUBLE_CLICK_MSG, 1000);
            if (mState == State.NONE && !mIsResuming && !mIsDealDoubleRunning) {
                float targetZoom;
                if (mDegree == 0 || mDegree == 180) {
                    if (Float.compare(mNormalizedScale, 1) == 0) {
                        targetZoom = mDoubleClickScale / mCurrentScale;//MIDDLE_SCALE_HORIZIONAL / HorizontalMatchScreenScale;
                    }
                    //                    else if (Float.compare(mNormalizedScale,
                    //                            MIDDLE_SCALE_HORIZIONAL / HorizontalMatchScreenScale) == 0) {
                    //                        targetZoom = 1;
                    //                    }
                    //                        targetZoom = mMaxScale ;
                    else {
                        Matrix targetMatrix = new Matrix();
                        if (mDegree == 0) {
                            targetMatrix.set(matchViewMatrix_0);
                        } else if (mDegree == 180) {
                            targetMatrix.set(matchViewMatrix_180);
                        }
                        if (!mIsResuming) {
                            Resume mResumeRunnable = new Resume(targetMatrix);
                            mResumeRunnable.start();
                        }
                        return true;
                    }
                } else {
                    if (Float.compare(mNormalizedScale,
                            verticalMatchScreenScale / HorizontalMatchScreenScale) == 0) {
                        targetZoom = 2 * verticalMatchScreenScale / HorizontalMatchScreenScale;//MIDDLE_SCALE_VERTIVAL / HorizontalMatchScreenScale;
                    } else {
                        targetZoom = verticalMatchScreenScale / HorizontalMatchScreenScale;
                    }
                }

                DoubleClickRunnable mDoubleClickRunnable = new DoubleClickRunnable(targetZoom,
                        focusX, focusY);
                mDoubleClickRunnable.setDuration(DEFAULT_ANIM_TIME);
                mDoubleClickRunnable.start();
            }
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                               float velocityY) {
            if (responseHandler.hasMessages(ON_FLING_MSG)) {
                responseHandler.removeMessages(ON_FLING_MSG);
            }
            responseHandler.removeMessages(ON_LONG_CLICK_MSG);
            responseHandler.removeMessages(ON_DOUBLE_CLICK_MSG);
            responseHandler.removeMessages(ON_CLICK_MSG);
            responseHandler.removeMessages(ON_SCALE_MSG);
            responseHandler.sendEmptyMessageDelayed(ON_FLING_MSG, 200);
            if (mFling != null) {
                mFling.cancelFling();
            }
            mFling = new Fling((int) velocityX, (int) velocityY);
            mFling.start();
            return super.onFling(e1, e2, velocityX, velocityY);
        }
    }

    private class Fling implements Runnable {
        private Scroller scroller;
        private int lastX;
        private int lastY;

        Fling(int velocityX, int velocityY) {
            scroller = new Scroller(getContext());
            float[] mtrixValues = getMatrixValues(mMatrix);
            Size size = getImageSize(mNormalizedScale);
            int startX = (int) mtrixValues[Matrix.MTRANS_X];
            int startY = (int) mtrixValues[Matrix.MTRANS_Y];
            int minX;
            int maxX;

            if (size.getWidth() > mViewWidth) {
                minX = mViewWidth - (int) size.getWidth();
                maxX = 0;
                if (mDegree == 90 || mDegree == 180) {
                    minX += size.getWidth();
                    maxX += size.getWidth();
                }
            } else {
                minX = maxX = startX;
            }

            int minY;
            int maxY;
            if (size.getHeight() > mViewHeight) {
                minY = mViewHeight - (int) size.getHeight();
                maxY = 0;
                if (mDegree == 270 || mDegree == 180) {
                    minY += size.getHeight();
                    maxY += size.getHeight();
                }
            } else {
                minY = maxY = startY;
            }

            scroller.fling(startX, startY, velocityX, velocityY,
                    minX, maxX, minY, maxY);
            lastX = startX;
            lastY = startY;
        }

        public void start() {
            postRunnable(this);
        }

        void cancelFling() {
            if (scroller != null) {
                scroller.forceFinished(true);
            }
        }

        @Override
        public void run() {
            if (OsVersionUtils.hasKitKat()) {
                if (!isAttachedToWindow) {
                    return;
                }
            } else {
                if (getWindowToken() == null) {
                    return;
                }
            }

            if (scroller.isFinished()) {
                scroller = null;
                return;
            }

            if (scroller.computeScrollOffset()) {
                int newX = scroller.getCurrX();
                int newY = scroller.getCurrY();
                int deltaX = newX - lastX;
                int deltaY = newY - lastY;
                lastX = newX;
                lastY = newY;

                fixTranslation(deltaX, deltaY);
                postRunnable(this);
            } else {
                scroller = null;
            }
        }
    }

    public class DoubleClickRunnable implements Runnable {
        private long startTime;

        private final float startScale;
        private final float targetScale;
        private final float bitmapX;
        private final float bitmapY;
        private final Interpolator interpolator = new CubicBezierInterpolator(0.1f, 0.05f, 0.1f, 0.99f);
        private final PointF startTouch;
        private final PointF endTouch;

        private float duration = DEFAULT_ANIM_TIME;

        private boolean isDoubleClickRunning;
        private boolean isFixTransRunning;

        DoubleClickRunnable(float targetScale, float focusX, float focusY) {
            startScale = mNormalizedScale;
            this.targetScale = targetScale;
            PointF bitmapPoint = transformCoordTouchToBitmap(focusX, focusY, false);
            bitmapX = bitmapPoint.x;
            bitmapY = bitmapPoint.y;
            startTouch = transformCoordBitmapToTouch(bitmapX, bitmapY);
            endTouch = new PointF(mViewWidth / 2f, mViewHeight / 2f);
        }

        private PointF transformCoordTouchToBitmap(float x, float y,
                                                   boolean clipToBitmap) {
            float[] m = getMatrixValues(mMatrix);
            float origW = getDrawable().getIntrinsicWidth();
            float origH = getDrawable().getIntrinsicHeight();
            float transX = m[Matrix.MTRANS_X];
            float transY = m[Matrix.MTRANS_Y];
            Size size = getImageSize(mNormalizedScale);
            float finalX = (x - transX) * origW / size.getWidth();
            float finalY = (y - transY) * origH / size.getHeight();
            if (clipToBitmap) {
                finalX = Math.min(Math.max(finalX, 0), origW);
                finalY = Math.min(Math.max(finalY, 0), origH);
            }
            return new PointF(finalX, finalY);
        }

        private PointF transformCoordBitmapToTouch(float bx, float by) {
            float[] m = getMatrixValues(mMatrix);
            float origW = getDrawable().getIntrinsicWidth();
            float origH = getDrawable().getIntrinsicHeight();
            float px = bx / origW;
            float py = by / origH;
            Size size = getImageSize(mNormalizedScale);
            float finalX = m[Matrix.MTRANS_X] + size.getWidth() * px;
            float finalY = m[Matrix.MTRANS_Y] + size.getHeight() * py;
            return new PointF(finalX, finalY);
        }

        public void start() {
            startTime = System.currentTimeMillis();
            mIsDealDoubleRunning = true;
            isDoubleClickRunning = true;
            isFixTransRunning = true;
            postRunnable(this);
        }

        public void setDuration(float duration) {
            this.duration = duration;
        }

        private float interpolate() {
            long currTime = System.currentTimeMillis();
            float elapsed = (currTime - startTime) / duration;
            elapsed = Math.min(1f, elapsed);
            if (elapsed >= 1) {
                isDoubleClickRunning = false;
            }
            return interpolator.getInterpolation(elapsed);
        }

        @Override
        public void run() {
            if (OsVersionUtils.hasKitKat()) {
                if (!isAttachedToWindow) {
                    return;
                }
            } else {
                if (getWindowToken() == null) {
                    return;
                }
            }
            float t = interpolate();
            float deltaScale = calculateDeltaScale(t);
            scaleImage(deltaScale, bitmapX, bitmapY, true);
            translateImageToCenterTouchPosition(t);
            if (isFixTransRunning) {
                fixTranslation(0, 0);
            } else {
                setImageMatrix(mMatrix);
            }
            if (mIsDealDoubleRunning) {
                if (!isDoubleClickRunning) {
                    mIsDealDoubleRunning = false;
                }
                postRunnable(this);
            }
        }

        private void translateImageToCenterTouchPosition(float t) {
            float targetX = startTouch.x + t * (endTouch.x - startTouch.x);
            float targetY = startTouch.y + t * (endTouch.y - startTouch.y);
            PointF curr = transformCoordBitmapToTouch(bitmapX, bitmapY);
            mMatrix.postTranslate(targetX - curr.x, targetY - curr.y);
        }

        private float calculateDeltaScale(float t) {
            float zoom = startScale + t * (targetScale - startScale);
            return zoom / mNormalizedScale;
        }
    }

    private class Resume implements Runnable {
        private long startTime;

        private final Interpolator interpolator = new CubicBezierInterpolator(0.1f, 0.05f, 0.1f, 0.99f);

        private float duration = DEFAULT_ANIM_TIME;

        private boolean isResuming;

        private final Matrix targetMatrix = new Matrix();

        Resume(Matrix targetMatrix) {
            this.targetMatrix.set(targetMatrix);
        }

        public void setDuration(float duration) {
            this.duration = duration;
        }

        public void start() {
            startTime = System.currentTimeMillis();
            mIsResuming = true;
            isResuming = true;
            postRunnable(this);
        }

        private float interpolate() {
            long currTime = System.currentTimeMillis();
            float elapsed = (currTime - startTime) / duration;
            elapsed = Math.min(1f, elapsed);
            if (elapsed >= 1) {
                isResuming = false;
            }
            return interpolator.getInterpolation(elapsed);
        }

        private Matrix calculateDeltaMatrix(float percent) {
            Matrix matrix = new Matrix();
            matrix.set(mMatrix);
            float[] matrixValue = getMatrixValues(matrix);
            float[] mMatrixValue = getMatrixValues(mMatrix);
            float[] targetMatrixValue = getMatrixValues(targetMatrix);
            for (int i = 0; i < 9; i++) {
                matrixValue[i] = mMatrixValue[i] + (targetMatrixValue[i] - mMatrixValue[i]) * percent;
            }
            matrix.setValues(matrixValue);
            return matrix;
        }

        @Override
        public void run() {
            float percent = interpolate();
            Matrix matrix = calculateDeltaMatrix(percent);
            setImageMatrix(matrix);
            if (mIsResuming) {
                if (!isResuming) {
                    mIsResuming = false;
                }
                postRunnable(this);
            } else {
                mMatrix.setValues(getMatrixValues(targetMatrix));
                if (mDegree == 0 || mDegree == 180) {
                    setNormalizedScale(1.0f);
                } else {
                    setNormalizedScale(verticalMatchScreenScale / HorizontalMatchScreenScale);
                }
            }
        }
    }
}
