package com.vitaviva.commongallery.listener;


import android.content.Context;
import android.support.v4.util.Pair;


import com.vitaviva.commongallery.model.GalleryItemWrapper;

import java.util.List;

public interface IGalleryLoadListener {

    rx.Observable<GalleryItemWrapper> onLoadFirst();
    rx.Observable<Pair<List<GalleryItemWrapper>, Integer>> onLoadAll();
    rx.Observable<GalleryItemWrapper> onLoadPre(Context context, GalleryItemWrapper curItem);
    rx.Observable<GalleryItemWrapper> onLoadNext(Context context, GalleryItemWrapper curItem);
}
