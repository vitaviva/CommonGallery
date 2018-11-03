package com.vitaviva.commongallery.drag;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.vitaviva.commongallery.viewpager.GalleryViewPager;


/**
 * 拖拽退出Activity
 */
public class GalleryDragLayout extends FrameLayout {
    private ViewDragHelper mDragHelper;

    public void setCanDragFinish(boolean canDragFinish) {
        this.canDragFinish = canDragFinish;
    }

    private boolean canDragFinish;

    public GalleryDragLayout(@NonNull Context context) {
        this(context, null);
    }

    public GalleryDragLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        if (getContext() instanceof Activity) {
            ((Activity) getContext()).getWindow().getDecorView().setBackgroundColor(0xff000000);
        }

        mDragHelper = ViewDragHelper.create(this, 1.0f, new ViewDragHelper.Callback() {
            @Override
            public boolean tryCaptureView(View child, int pointerId) {
                if (child instanceof GalleryViewPager) {
                    GalleryViewPager pager = (GalleryViewPager) child;
                    int nIndex = pager.getCurrentItem();
                    if(nIndex<0 || nIndex >=pager.getDatas().size())
                        return false;
                    return pager.getDatas().get(pager.getCurrentItem()).canDragFinish();
                }
                return false;
            }

            @Override
            public void onViewReleased(View releasedChild, float xvel, float yvel) {
                super.onViewReleased(releasedChild, xvel, yvel);

                if (mNeedRelease) {
                    if (getContext() instanceof Activity) {
                        ((Activity) getContext()).onBackPressed();
                    }
                } else {
                    needDrag = false;
                    //让视图归位,
                    mDragHelper.settleCapturedViewAt(finalLeft, finalTop);
                    releasedChild.setScaleX(1.0f);
                    releasedChild.setScaleY(1.0f);
                    invalidate();
                }
            }

            boolean mNeedRelease;

            @Override
            public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
                super.onViewPositionChanged(changedView, left, top, dx, dy);

                mNeedRelease = top > 150;//Release

                //ChangeBg
                float present = 1 - (top * 1.0f) / (getHeight());
                if (getContext() instanceof Activity) {
                    int alpha = Math.min((int) (255 * present), 255);
                    ((Activity) getContext()).getWindow().getDecorView().setBackgroundColor(Color.argb(alpha, 0, 0, 0));
                }
                //ChangeScale
                float minScale = Math.max(0.5f, Math.min(present, 1.0f));//0.5f <= scale <= 1.0f
                changedView.setScaleX(minScale);
                changedView.setScaleY(minScale);
            }

            boolean needDrag;

            @Override
            public int clampViewPositionVertical(View child, int top, int dy) {
                if (needDrag) {
                    return top;
                }
                if (top < 0) {//只允许向下拖拽
                    top = 0;
                } else if (top > 100) {//向下拖拽超过100px后，释放允许任何方向拖拽
                    needDrag = true;
                }
                return top;
            }

            @Override
            public int clampViewPositionHorizontal(View child, int left, int dx) {
                return needDrag ? left : 0;
            }

            @Override
            public int getViewVerticalDragRange(View child) {
                return getHeight() / 2;
            }
        });
    }

    int finalLeft;
    int finalTop;

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        finalLeft = getChildAt(0).getLeft();
        finalTop = getChildAt(0).getTop();
    }

    @Override
    public void computeScroll() {
        if (mDragHelper.continueSettling(true)) {
            invalidate();
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (!canDragFinish || ev.getPointerCount() > 1) return false;
        return mDragHelper.shouldInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mDragHelper.processTouchEvent(event);
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }
}
