package com.vitaviva.commongallery.viewpager;

import android.content.Context;
import android.databinding.ObservableArrayList;
import android.databinding.ObservableList;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import com.vitaviva.commongallery.itemview.GalleryItemView;
import com.vitaviva.commongallery.itemview.GalleryPhotoView;
import com.vitaviva.commongallery.model.GalleryItemWrapper;

import java.util.Collections;
import java.util.List;

import static com.vitaviva.commongallery.model.GalleryItemImpl.ItemType.TYPE_IMAGE;
import static com.vitaviva.commongallery.model.GalleryItemImpl.ItemType.TYPE_VIDEO;


public class GalleryPagerAdapter extends PagerAdapter {

    private final Context mContext;
    private GalleryListenerBundle galleryListenerBundle;
    private ObservableList.OnListChangedCallback<ObservableList<GalleryItemWrapper>> onListChangedCallback;
    private List<GalleryItemWrapper> datas;


    public void setOnListChangedCallback(
            ObservableList.OnListChangedCallback<ObservableList<GalleryItemWrapper>> onListChangedCallback) {
        this.onListChangedCallback = onListChangedCallback;
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    public GalleryPagerAdapter(Context context) {
        mContext = context;
        ObservableArrayList<GalleryItemWrapper> observableArrayList = new ObservableArrayList<>();
        datas = Collections.synchronizedList(observableArrayList);
        observableArrayList.addOnListChangedCallback(new ObservableList.OnListChangedCallback<ObservableList<GalleryItemWrapper>>() {
            @Override
            public void onChanged(ObservableList<GalleryItemWrapper> sender) {
                if (onListChangedCallback != null) {
                    onListChangedCallback.onChanged(sender);
                }
            }

            @Override
            public void onItemRangeChanged(ObservableList<GalleryItemWrapper> sender, int positionStart, int itemCount) {
                if (onListChangedCallback != null) {
                    onListChangedCallback.onItemRangeChanged(sender, positionStart, itemCount);
                }
            }

            @Override
            public void onItemRangeInserted(ObservableList<GalleryItemWrapper> sender, int positionStart, int itemCount) {
                if (onListChangedCallback != null) {
                    onListChangedCallback.onItemRangeInserted(sender, positionStart, itemCount);
                }
            }

            @Override
            public void onItemRangeMoved(ObservableList<GalleryItemWrapper> sender, int fromPosition, int toPosition, int itemCount) {
                if (onListChangedCallback != null) {
                    onListChangedCallback.onItemRangeMoved(sender, fromPosition, toPosition, itemCount);
                }
            }

            @Override
            public void onItemRangeRemoved(ObservableList<GalleryItemWrapper> sender, int positionStart, int itemCount) {
                if (onListChangedCallback != null) {
                    onListChangedCallback.onItemRangeRemoved(sender, positionStart, itemCount);
                }
            }
        });
    }

    public List<GalleryItemWrapper> getDatas() {
        return datas;
    }

    @Override
    public Object instantiateItem(ViewGroup collection, int position) {
        GalleryItemWrapper galleryItem = datas.get(position);
        GalleryItemView galleryItemView = null;
        if (galleryItem != null) {
            if (galleryItem.convertView != null) {
                galleryItemView = galleryItem.convertView;
            } else if (galleryItem.type() == TYPE_IMAGE) {
                galleryItemView = new GalleryPhotoView(mContext);
                galleryItemView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                galleryItemView.setCallBack(galleryListenerBundle.getOnClickListener(), galleryListenerBundle.getOnLongClickListener());
                galleryItemView.load(galleryItem);
            } else if (galleryItem.type() == TYPE_VIDEO) {
//                galleryItemView = new GalleryVideoView(mContext);
//                galleryItemView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
//                galleryItemView.setCallBack(galleryListenerBundle.getOnClickListener(), galleryListenerBundle.getOnLongClickListener());
//                ((GalleryVideoView)galleryItemView).setOnToggleListener(toggleListener);
//                galleryItemView.load(galleryItem);
//                if (getCount() == 1) {
//                    galleryItemView.onShownAsFirstPage();
//                }
            }
        }
        if (galleryItemView == null) {
//            galleryItemView = new GalleryVideoView(mContext);
        }
        collection.addView(galleryItemView, 0);
        return galleryItemView;
    }

    @Override
    public void destroyItem(ViewGroup collection, int position, Object view) {
        collection.removeView((View) view);
    }

    @Override
    public int getCount() {
        return datas.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view.equals(object);
    }

    @Override
    public void finishUpdate(ViewGroup arg0) {
    }

    @Override
    public void restoreState(Parcelable arg0, ClassLoader arg1) {
    }

    @Override
    public Parcelable saveState() {
        return null;
    }

    @Override
    public void startUpdate(ViewGroup arg0) {
    }

    public void setGalleryListenerBundle(GalleryListenerBundle galleryListenerBundle) {
        this.galleryListenerBundle = galleryListenerBundle;
    }
//    private GalleryVideoView.OnToggleListener toggleListener;
//    public void setOnToggleListener(GalleryVideoView.OnToggleListener listener){
//        this.toggleListener = listener;
//    }

}