package com.vitaviva.commongallery.model;


import static com.vitaviva.commongallery.model.GalleryItemImpl.ItemType.TYPE_UNKNOWN;


public class GalleryItemImpl implements IGalleryItem {

    public enum ItemType {
        TYPE_UNKNOWN,
        TYPE_IMAGE,
        TYPE_VIDEO
    }


    public boolean canDragFinish() {
        return true;
    }

    @Override
    public void onSlideIn() {

    }

    @Override
    public void onSlideOut() {

    }

    @Override
    public ItemType type() {
        return TYPE_UNKNOWN;
    }


}