package com.vitaviva.commongallery.model;

import android.graphics.drawable.Drawable;

import com.vitaviva.commongallery.itemview.GalleryItemView;

import javax.annotation.Nullable;

public abstract class GalleryItemWrapper implements IGalleryItem {
    public GalleryItemView convertView;
    public Drawable thumbnail;

    GalleryItemWrapper(int position) {
        this.position = position;
        this.mBase = new GalleryItemImpl();
    }

    private int position;

    private IGalleryItem mBase;

    public void attachImpl(IGalleryItem impl) {
        mBase = impl;
    }

    public int getPosition() {
        return position;
    }

    @Override
    public boolean canDragFinish() {
        return mBase.canDragFinish();
    }

    @Override
    public void onSlideIn() {
        mBase.onSlideIn();
    }

    @Override
    public void onSlideOut() {
        mBase.onSlideOut();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        return mBase.equals(obj);
    }
}
