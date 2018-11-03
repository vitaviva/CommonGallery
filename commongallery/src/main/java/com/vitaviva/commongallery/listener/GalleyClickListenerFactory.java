package com.vitaviva.commongallery.listener;


import android.content.Context;

import com.vitaviva.commongallery.model.GalleryItemWrapper;
import com.vitaviva.commongallery.util.ToastUtil;


public class GalleyClickListenerFactory {

    public static IGalleryClickListener create() {
        return new IGalleryClickListener() {

            @Override
            public void onClicked(Context context, GalleryItemWrapper item) {
                ToastUtil.toast(context, "onClicked:item");
            }

            @Override
            public void onLongClicked(Context context, GalleryItemWrapper item) {
                ToastUtil.toast(context, "onLongClicked:item");

            }

        };
    }

}
