package com.vitaviva.commongallery;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.view.View;
import android.widget.ImageView;

import com.trello.rxlifecycle.android.ActivityEvent;
import com.trello.rxlifecycle.components.support.RxAppCompatActivity;
import com.vitaviva.commongallery.activity.GalleryActivity;
import com.vitaviva.commongallery.listener.GalleryLoadListenerFactory;
import com.vitaviva.commongallery.listener.GalleyClickListenerFactory;
import com.vitaviva.commongallery.listener.IGalleryClickListener;
import com.vitaviva.commongallery.listener.IGalleryListenerProxy;
import com.vitaviva.commongallery.listener.IGalleryLoadListener;
import com.vitaviva.commongallery.model.GalleryItemWrapper;
import com.vitaviva.commongallery.serializable_callback.base.CallbackProxy;
import com.vitaviva.commongallery.serializable_callback.base.ICallbackBase;
import com.vitaviva.commongallery.util.ImageUtil;
import com.vitaviva.commongallery.util.OsVersionUtils;

import java.util.Collections;
import java.util.List;

import rx.Observable;
import rx.schedulers.Schedulers;

import static com.vitaviva.commongallery.Constants.KEY_LISTENER;
import static com.vitaviva.commongallery.Constants.KEY_TRANSITION_DRAWABLE;


public class GalleryHelper {

    public static void open(Context context,
                            IGalleryLoadListener loadListener,
                            IGalleryClickListener clickListener) {

        Intent intent = getIntent(context, loadListener, clickListener);
        context.startActivity(intent);
        if (context instanceof Activity) {
            ((Activity) context).overridePendingTransition(R.anim.photo_scale_enter, R.anim.fade_out);
        }
        addRemoveCallback(context);
    }

    /**
     * open activity by transition-anim based on @param view
     *
     * @param view
     * @param loadListener
     * @param clickListener
     */
    public static void open(ImageView view,
                             IGalleryLoadListener loadListener,
                             IGalleryClickListener clickListener) {
        open(view, true, loadListener, clickListener);
    }

    private static void open(ImageView view,
                             boolean canViewGrid,
                             IGalleryLoadListener loadListener,
                             IGalleryClickListener clickListener) {
        if (!OsVersionUtils.hasLollipop()) {
            open(getActivityFromView(view), loadListener, clickListener);
            return;
        }
        Context context = getActivityFromView(view);
        Intent intent = getIntent(context, loadListener, clickListener);
        if (view.getDrawable() != null
                && view.getDrawable().getIntrinsicHeight() > 0
                && view.getDrawable().getIntrinsicWidth() > 0) {
            intent.putExtra(KEY_TRANSITION_DRAWABLE, ImageUtil.drawable2Bytes(view.getDrawable()));
        }
        intent.putExtra(Constants.EXTRA_CAN_VIEW_GRID, canViewGrid);
        ActivityOptionsCompat compat = ActivityOptionsCompat.makeSceneTransitionAnimation(
                (Activity) context, view, context.getString(R.string.transition_view_name));
        ContextCompat.startActivity(context, intent, compat.toBundle());
        addRemoveCallback(context);
    }

    public static void open(Context context,
                            IGalleryLoadListener listener) {
        open(context, listener, GalleyClickListenerFactory.create());
    }

    public static void open(ImageView view,
                            IGalleryLoadListener loadListener) {
        open(view, loadListener, GalleyClickListenerFactory.create());
    }


    public static void open(Context context,
                            GalleryItemWrapper imageInfo) {
        open(context, Collections.singletonList(imageInfo), 0);
    }

    public static void open(Context context,
                            List<GalleryItemWrapper> itemList,
                            int curIndex) {
        open(context, GalleryLoadListenerFactory.create(itemList, curIndex), null);
    }


    private static Activity getActivityFromView(View view) {
        Context context = view.getContext();
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity) context;
            }
            context = ((ContextWrapper) context).getBaseContext();
        }
        return null;
    }

    private static ICallbackBase.ICallbackRemover sCallbackRemover;

    private static void addRemoveCallback(Context context) {
        if (context instanceof RxAppCompatActivity) {
            ((RxAppCompatActivity) context).lifecycle()
                    .skip(1)
                    .filter(it -> it.equals(ActivityEvent.RESUME)//从GalleryActivity退出时
                            || it.equals(ActivityEvent.DESTROY))//退出当前Activity时
                    .observeOn(Schedulers.io())
                    .subscribe(it -> {
                        if (sCallbackRemover != null) {
                            sCallbackRemover.removeCallback();
                        }
                    }, Throwable::printStackTrace);
        }
    }

    private static Intent getIntent(Context context,
                                    IGalleryLoadListener loadListener,
                                    IGalleryClickListener clickListener) {
        Class clazz = GalleryActivity.class;
        Intent intent = new Intent(context, clazz);
        intent.putExtra(KEY_LISTENER, CallbackProxy
                .newProxy(IGalleryListenerProxy.class, new IGalleryListenerProxy() {


                    @Override
                    public void onClicked(Context context, GalleryItemWrapper item) {

                    }

                    @Override
                    public void onLongClicked(Context context, GalleryItemWrapper item) {
                        if (clickListener != null) {
                            clickListener.onLongClicked(context, item);
                        }
                    }


                    @Override
                    public Observable<GalleryItemWrapper> onLoadFirst() {
                        if (loadListener != null) {
                            return loadListener.onLoadFirst();
                        }
                        return Observable.just(null);
                    }

                    @Override
                    public Observable<Pair<List<GalleryItemWrapper>, Integer>> onLoadAll() {
                        if (loadListener != null) {
                            return loadListener.onLoadAll();
                        }
                        return Observable.just(null);
                    }

                    @Override
                    public Observable<GalleryItemWrapper> onLoadPre(Context context, GalleryItemWrapper curItem) {
                        if (loadListener != null) {
                            return loadListener.onLoadPre(context, curItem);
                        }
                        return Observable.just(null);
                    }

                    @Override
                    public Observable<GalleryItemWrapper> onLoadNext(Context context, GalleryItemWrapper curItem) {
                        if (loadListener != null) {
                            return loadListener.onLoadNext(context, curItem);
                        }
                        return Observable.just(null);
                    }

                    @Override
                    public void onGetRemover(ICallbackBase.ICallbackRemover remover) {
                        sCallbackRemover = remover;
                    }

                }));
        return intent;
    }

}
