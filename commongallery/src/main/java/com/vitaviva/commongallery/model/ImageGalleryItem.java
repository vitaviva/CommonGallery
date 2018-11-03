package com.vitaviva.commongallery.model;

import javax.annotation.Nullable;

public class ImageGalleryItem extends GalleryItemWrapper {
    public String image;

    public ImageGalleryItem(int position, String image) {
        super(position);
        this.image = image;
    }

    @Override
    public GalleryItemImpl.ItemType type() {
        return GalleryItemImpl.ItemType.TYPE_IMAGE;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        return obj != null && image.equals(((ImageGalleryItem) obj).image);
    }
}
