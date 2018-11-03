package com.vitaviva.commongallery.presenter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.view.ViewPager;

import com.trello.rxlifecycle.components.support.RxAppCompatActivity;
import com.vitaviva.commongallery.listener.IGalleryLoadListener;
import com.vitaviva.commongallery.model.GalleryItemWrapper;
import com.vitaviva.commongallery.model.ImageGalleryItem;
import com.vitaviva.commongallery.util.CollectionUtil;
import com.vitaviva.commongallery.viewpager.GalleryViewPager;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.vitaviva.commongallery.Constants.getCachedPageSize;


public class GalleryLoadPresenter {

    private Context context;
    private GalleryViewPager galleryPager;
    private IGalleryLoadListener listener;

    public GalleryLoadPresenter(Context context, GalleryViewPager galleryPager,
                                IGalleryLoadListener listener) {
        this.galleryPager = galleryPager;
        this.galleryPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == ViewPager.SCROLL_STATE_IDLE) {
                    tryLoadMore();
                }
            }
        });
        this.listener = listener;
        this.context = context;
    }

    public void load(Drawable thumbanil) {
        List<GalleryItemWrapper> tmp = new ArrayList<>();
        Observable.concat(
                Observable.defer(() -> {
                    if (thumbanil != null) {
                        GalleryItemWrapper item = new ImageGalleryItem(0, null);
                        item.thumbnail = thumbanil;
                        tmp.add(item);
                        return Observable.just(0);
                    }
                    return Observable.just(null);
                }).filter(it -> it != null),
                Observable.defer(() -> {
                    tmp.clear();
                    return listener.onLoadFirst()
                            .filter(it -> it != null)
                            .map(it -> {
                                if (thumbanil != null) {
                                    it.thumbnail = thumbanil;
                                }
                                tmp.add(it);
                                return 0;
                            })
                            .switchIfEmpty(Observable.defer(() ->
                                    listener.onLoadAll().map(it -> {
                                        tmp.addAll(it.first);
                                        return it.second;
                                    })));
                }))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(((RxAppCompatActivity) context).bindToLifecycle())
                .subscribe(it -> {
                    galleryPager.getDatas().clear();
                    galleryPager.getDatas().addAll(tmp);
                    galleryPager.notifyDataSetChanged();
                    galleryPager.setCurrentItem(it, false);//onLoadAll的时候，it有可能不为0
                }, Throwable::printStackTrace, this::tryLoadMore);

    }

    private void tryLoadMore() {
        if (CollectionUtil.isEmpty(galleryPager.getDatas())) return;
        int curPos = galleryPager.getCurrentItem();
        GalleryItemWrapper galleryItem = galleryPager.getDatas().get(curPos);
        final int[] newPos = {curPos};
        List<GalleryItemWrapper> tmp = new ArrayList<>(galleryPager.getDatas());
        Observable.merge(
                Observable.defer(() -> Observable.just(curPos)
                        .filter(it -> it == 0)
                        .flatMap(it -> listener.onLoadPre(context, galleryItem)
                                .filter(it1 -> it1 != null)
                                .flatMap(it1 -> {
                                    tmp.add(0, it1);
                                    if (tmp.size() > getCachedPageSize()) {
                                        tmp.remove(getCachedPageSize());
                                    }
                                    newPos[0] = 1;
                                    return null;
                                }))).subscribeOn(Schedulers.io()),
                Observable.defer(() -> Observable.just(curPos)
                        .filter(it -> it == galleryPager.getDatas().size() - 1)
                        .flatMap(it -> listener.onLoadNext(context, galleryItem)
                                .filter(it1 -> it1 != null)
                                .flatMap(it1 -> {
                                    tmp.add(it1);
                                    if (tmp.size() > getCachedPageSize()) {
                                        tmp.remove(0);
                                        newPos[0] = it - 1;
                                    }
                                    return null;
                                }))).subscribeOn(Schedulers.io()))
                .observeOn(AndroidSchedulers.mainThread())
                .compose(((RxAppCompatActivity) context).bindToLifecycle())
                .subscribe(it -> {
                }, Throwable::printStackTrace, () -> {
                    if (curPos != newPos[0] || tmp.size() != galleryPager.getDatas().size()) {
                        galleryPager.getDatas().clear();
                        galleryPager.getDatas().addAll(tmp);
                        galleryPager.notifyDataSetChanged();
                        galleryPager.setCurrentItem(newPos[0], false);
                    }
                });

    }

}
