package com.vitaviva.commongallery.itemview;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.common.base.Strings;
import com.vitaviva.commongallery.R;
import com.vitaviva.commongallery.glide.GlideApp;
import com.vitaviva.commongallery.model.ImageGalleryItem;
import com.vitaviva.commongallery.util.NavigationBarUtil;
import com.vitaviva.commongallery.widget.ImageBoothView;

import java.io.File;

import static com.vitaviva.commongallery.model.GalleryItemImpl.ItemType.TYPE_IMAGE;


public class GalleryPhotoView extends GalleryItemView<ImageGalleryItem> implements View.OnClickListener {
    private static final String TAG = GalleryPhotoView.class.getSimpleName();
    //    private final View loadingView;
    private ImageBoothView mImageVIew;
    private TextView mDownloadBtn;

    enum ImageLoadingState {
        INIT,
        ORIGINAL_DOWNLOAD_READY,
        ORIGINAL_DOWNLOAD_FINISHED,
        ORIGINAL_DOWNLOAD_STARTED
    }

    public GalleryPhotoView(Context context) {
        this(context, null);
    }

    public GalleryPhotoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        addView(LayoutInflater.from(context).inflate(R.layout.photo_view, this, false));
        mImageVIew = findViewById(R.id.image);
        mDownloadBtn = findViewById(R.id.tv_download_original);
//        loadingView = findViewById(R.id.loadingView);
        mDownloadBtn.setVisibility(GONE);
        MarginLayoutParams lp = (MarginLayoutParams) mDownloadBtn.getLayoutParams();
        lp.bottomMargin += NavigationBarUtil.getNavigationBarHeight(context);
        mDownloadBtn.setLayoutParams(lp);
        mDownloadBtn.setOnClickListener(this);
    }

    @Override
    public void setCallBack(OnClickListener onClickListener, OnLongClickListener onLongClickListener) {
        super.setCallBack(onClickListener, onLongClickListener);
        if (onLongClickListener == null) {
            mImageVIew.setIsNeedRefuseMsgInTwoMinutes(false);
        }
        mImageVIew.setListener(onClickListener, onLongClickListener);
    }

    @Override
    public Drawable snap() {
        return mImageVIew.getDrawable();
    }

    @Override
    public void load(ImageGalleryItem galleryItem) {
        super.load(galleryItem);
        updateLoadingState(ImageLoadingState.INIT);
        if (galleryItem.type() != TYPE_IMAGE) {
            return;
        }
        mImageVIew.setBackgroundColor(Color.TRANSPARENT);
        mImageVIew.setOriginalDrawableWidth(0);
        mImageVIew.setOriginalDrawableHeight(0);
        if (galleryItem.thumbnail != null) {
            mImageVIew.setImageDrawable(galleryItem.thumbnail);
        }

        if (Strings.isNullOrEmpty(this.galleryItem.image)) return;
        if (!loadImage(new File(this.galleryItem.image))) {
            loadImage(Uri.parse(this.galleryItem.image));
        }

    }

    private String getMediaSize() {
        if (galleryItem == null) {
            return "";
        }
        return "100kb";
    }


    /**
     * 本地文件
     */
    private boolean loadImage(File file) {
        if (file == null || !file.exists()) {
            return false;
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file.getAbsolutePath(), options);
        int dWidth = options.outWidth;
        int dHeight = options.outHeight;
        mImageVIew.setOriginalDrawableWidth(dWidth);
        mImageVIew.setOriginalDrawableHeight(dHeight);

        /*DisplayMetrics dm = getResources().getDisplayMetrics();
        int vWidth = dm.widthPixels;
        int vHeight = dm.heightPixels;*/

        //1. 同步加载高压缩
        //mImageVIew.setImageBitmap(file.getPath());
        //2. 异步加载高保真
        GlideApp.with(getContext()).load(galleryItem.image)
                .placeholder(mImageVIew.getDrawable())
                .transition(new DrawableTransitionOptions().dontTransition())
                .centerInside()
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(mImageVIew);
        return true;
    }


    private void updateLoadingState(ImageLoadingState loadingState) {
        switch (loadingState) {
            case INIT:
            case ORIGINAL_DOWNLOAD_FINISHED:
                mDownloadBtn.setVisibility(GONE);
                mDownloadBtn.setEnabled(false);
                break;
            case ORIGINAL_DOWNLOAD_READY:
                String mediaSize = getMediaSize();
                if (TextUtils.isEmpty(mediaSize)) {
                    mDownloadBtn.setText(R.string.bu_lookorigin_pic_txt);
                } else {
                    mDownloadBtn.setText(getContext().getString(R.string.bu_lookorigin_pic_txt2, getMediaSize()));
                }
                mDownloadBtn.setVisibility(VISIBLE);
                mDownloadBtn.setEnabled(true);
                break;
            case ORIGINAL_DOWNLOAD_STARTED:
                mDownloadBtn.setVisibility(VISIBLE);
                mDownloadBtn.setEnabled(false);
                break;
        }
    }

    @Override
    public void download() {
        if (galleryItem == null || TextUtils.isEmpty(galleryItem.image)) {
            return;
        }


    }


    private void stopLoading() {
        postDelayed(new Runnable() {
            @Override
            public void run() {
//                loadingView.setVisibility(View.GONE);
            }
        }, 100);
    }

    public boolean autoCancel() {
        return mDownloadBtn.getVisibility() == View.GONE;
    }


    private boolean loadImage(Uri uri) {
        GlideApp.with(getContext())
                .load(uri)
                .centerInside()
                .transition(new DrawableTransitionOptions().dontTransition())
                .into(mImageVIew);
        return true;
    }

    @Override
    public void onClick(View v) {
    }


}
