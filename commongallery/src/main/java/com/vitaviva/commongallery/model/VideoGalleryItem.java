package com.vitaviva.commongallery.model;

import javax.annotation.Nullable;

public class VideoGalleryItem extends GalleryItemWrapper{
    public String thumbnail;
    public String media;


    public VideoGalleryItem(int position, String thumbnail, String media) {
        super(position);
        this.thumbnail = thumbnail;
        this.media = media;
    }

    @Override
    public GalleryItemImpl.ItemType type() {
        return GalleryItemImpl.ItemType.TYPE_VIDEO;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        return obj != null && media.equals(((VideoGalleryItem) obj).media);
    }
}
