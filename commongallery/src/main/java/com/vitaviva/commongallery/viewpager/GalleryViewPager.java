package com.vitaviva.commongallery.viewpager;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;


import com.vitaviva.commongallery.R;
import com.vitaviva.commongallery.model.GalleryItemWrapper;
import com.vitaviva.commongallery.util.Reflector;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public class GalleryViewPager extends ViewPager {
    public GalleryViewPager(Context context) {
        this(context, null);
    }

    @Override
    public void setCurrentItem(int item, boolean smoothScroll) {
        super.setCurrentItem(item, smoothScroll);
        if (getCurrentItem() == item) {
            Method method = Reflector.getMethodSafe(ViewPager.class, "dispatchOnPageSelected", int.class);
            if (method != null) {
                try {
                    method.invoke(this, item);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public GalleryViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOffscreenPageLimit(2);
        setPageMargin((int) getResources().getDimension(R.dimen.common_dp_10));
    }

    public void setAdapter(GalleryPagerAdapter adapter) {
        super.setAdapter(adapter);
    }

    @Override
    public GalleryPagerAdapter getAdapter() {
        return (GalleryPagerAdapter) super.getAdapter();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        try {
            return super.onInterceptTouchEvent(ev);
        } catch (Exception ignored) {
        }
        return false;
    }

    public List<GalleryItemWrapper> getDatas() {
        if (getAdapter() == null) return null;
        return getAdapter().getDatas();
    }

    public void notifyDataSetChanged() {
        if (getAdapter() == null) return;
        getAdapter().notifyDataSetChanged();
    }

}
