package com.vitaviva.commongallery.listener;


import android.content.Context;

import com.vitaviva.commongallery.model.GalleryItemWrapper;

public interface IGalleryClickListener {

    void onClicked(Context context, GalleryItemWrapper item);

    void onLongClicked(Context context, GalleryItemWrapper item);
}
