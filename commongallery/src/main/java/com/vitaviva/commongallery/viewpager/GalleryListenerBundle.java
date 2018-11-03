package com.vitaviva.commongallery.viewpager;

import android.support.v4.view.ViewPager;
import android.view.View;

import java.util.Iterator;
import java.util.LinkedList;

public class GalleryListenerBundle {

    private View.OnClickListener onClickListener;
    private View.OnLongClickListener onLongClickListener;
    private ViewPager.OnPageChangeListener onPageChangeListener;

    public GalleryListenerBundle(View.OnClickListener onClickListener,
                                 View.OnLongClickListener onLongClickListener) {
        this(onClickListener, onLongClickListener, null);
    }

    public GalleryListenerBundle(View.OnClickListener onClickListener,
                                 View.OnLongClickListener onLongClickListener,
                                 ViewPager.OnPageChangeListener onPageChangeListener) {
        this.onClickListener = onClickListener;
        this.onLongClickListener = onLongClickListener;
        this.onPageChangeListener = onPageChangeListener;
    }

    public GalleryListenerBundle() {
        this(null, null, null);
    }

    public View.OnClickListener getOnClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Iterator<GalleryListenerBundle> iterator = head.iterator();
                while (iterator.hasNext()) {
                    View.OnClickListener onClickListener = iterator.next().onClickListener;
                    if (onClickListener != null) {
                        onClickListener.onClick(v);
                    }
                }
            }
        };
    }

    public View.OnLongClickListener getOnLongClickListener() {
        return new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Iterator<GalleryListenerBundle> iterator = head.iterator();
                while (iterator.hasNext()) {
                    View.OnLongClickListener onLongClickListener = iterator.next().onLongClickListener;
                    if (onLongClickListener != null) {
                        onLongClickListener.onLongClick(v);
                    }
                }
                return false;
            }
        };
    }


    public ViewPager.OnPageChangeListener getOnPageChangeListener() {
        return new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                Iterator<GalleryListenerBundle> iterator = head.iterator();
                while (iterator.hasNext()) {
                    ViewPager.OnPageChangeListener onPageChangeListener = iterator.next().onPageChangeListener;
                    if (onPageChangeListener != null) {
                        onPageChangeListener.onPageScrolled(position, positionOffset, positionOffsetPixels);
                    }
                }
            }

            @Override
            public void onPageSelected(int position) {
                Iterator<GalleryListenerBundle> iterator = head.iterator();
                while (iterator.hasNext()) {
                    ViewPager.OnPageChangeListener onPageChangeListener = iterator.next().onPageChangeListener;
                    if (onPageChangeListener != null) {
                        onPageChangeListener.onPageSelected(position);
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                Iterator<GalleryListenerBundle> iterator = head.iterator();
                while (iterator.hasNext()) {
                    ViewPager.OnPageChangeListener onPageChangeListener = iterator.next().onPageChangeListener;
                    if (onPageChangeListener != null) {
                        onPageChangeListener.onPageScrollStateChanged(state);
                    }
                }
            }
        };
    }

    private LinkedList<GalleryListenerBundle> head = new LinkedList<>();

    public void addListenerBundle(GalleryListenerBundle galleryListenerBundle) {
        if (!head.contains(galleryListenerBundle)) {
            head.add(galleryListenerBundle);
        }
    }

    public void removeListenerBundle(GalleryListenerBundle galleryListenerBundle) {
        if (head.contains(galleryListenerBundle)) {
            head.remove(galleryListenerBundle);
        }
    }
}

