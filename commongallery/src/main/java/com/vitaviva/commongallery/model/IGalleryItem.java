package com.vitaviva.commongallery.model;


public interface IGalleryItem {


    boolean canDragFinish();

    void onSlideIn();

    void onSlideOut();

    GalleryItemImpl.ItemType type();

}