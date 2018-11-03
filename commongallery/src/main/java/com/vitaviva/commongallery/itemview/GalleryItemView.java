package com.vitaviva.commongallery.itemview;


import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.vitaviva.commongallery.model.GalleryItemWrapper;


public abstract class GalleryItemView<T extends GalleryItemWrapper> extends FrameLayout {

    protected T galleryItem;

    public GalleryItemView(@NonNull Context context) {
        super(context);
    }

    public GalleryItemView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public void setCallBack(OnClickListener onClickListener,
                            OnLongClickListener onLongClickListener) {
    }

    public void onShownAsFirstPage() {

    }

    public Drawable snap() {
        return null;
    }



    public void load(T galleryItem) {
        this.galleryItem = galleryItem;
        galleryItem.convertView = this;
    }

    public boolean systemNavigationBarVisible() {
        return true;
    }
    public void download(){}

}
