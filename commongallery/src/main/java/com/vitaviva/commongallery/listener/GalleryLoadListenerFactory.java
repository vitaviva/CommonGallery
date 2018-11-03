package com.vitaviva.commongallery.listener;


import android.content.Context;
import android.support.v4.util.Pair;

import com.trello.rxlifecycle.components.support.RxAppCompatActivity;
import com.vitaviva.commongallery.data.DataManger;
import com.vitaviva.commongallery.model.GalleryItemWrapper;
import com.vitaviva.commongallery.model.ImageGalleryItem;
import com.vitaviva.commongallery.util.CollectionUtil;

import java.util.Collections;
import java.util.List;

import rx.Observable;


public class GalleryLoadListenerFactory {

    public static IGalleryLoadListener create(List<GalleryItemWrapper> imageInfoList, int position) {
        return create(imageInfoList, position, false);
    }

    /**
     * 固定长度列表中加载图片
     *
     * @param imageInfoList
     * @param position
     * @return
     */
    public static IGalleryLoadListener create(List<GalleryItemWrapper> imageInfoList, int position, boolean loadAll) {
        if (loadAll) {
            return new IGalleryLoadListener() {
                @Override
                public Observable<GalleryItemWrapper> onLoadFirst() {
                    return Observable.just(null);
                }

                @Override
                public Observable<Pair<List<GalleryItemWrapper>, Integer>> onLoadAll() {
                    return Observable.defer(() -> {
                        if (imageInfoList.size() > position)
                            return Observable.just(new Pair<>(imageInfoList, position));
                        return Observable.just(null);
                    });

                }

                @Override
                public Observable<GalleryItemWrapper> onLoadPre(Context context, GalleryItemWrapper curItem) {
                    return Observable.just(null);
                }

                @Override
                public Observable<GalleryItemWrapper> onLoadNext(Context context, GalleryItemWrapper curItem) {
                    return Observable.just(null);
                }
            };
        }
        return new IGalleryLoadListener() {
            @Override
            public Observable<GalleryItemWrapper> onLoadFirst() {
                return Observable.defer(() -> {
                    if (imageInfoList.size() > position)
                        return Observable.just(imageInfoList.get(position));
                    return Observable.just(null);
                });
            }

            @Override
            public Observable<Pair<List<GalleryItemWrapper>, Integer>> onLoadAll() {
                return Observable.just(null);
            }

            @Override
            public Observable<GalleryItemWrapper> onLoadPre(Context context, GalleryItemWrapper curItem) {
                return Observable.defer(() -> {
                    int idx = imageInfoList.indexOf(curItem);
                    if (idx == -1) return Observable.just(null);
                    if (idx == 0) return Observable.just(null);
                    return Observable.just(imageInfoList.get(idx - 1));
                }).compose(((RxAppCompatActivity) context).bindToLifecycle());
            }

            @Override
            public Observable<GalleryItemWrapper> onLoadNext(Context context, GalleryItemWrapper curItem) {
                return Observable.defer(() -> {
                    int idx = imageInfoList.indexOf(curItem);
                    if (idx == -1) return Observable.just(null);
                    if (idx == imageInfoList.size() - 1) return Observable.just(null);
                    return Observable.just(imageInfoList.get(idx + 1));
                }).compose(((RxAppCompatActivity) context).bindToLifecycle());
            }

        };
    }

    public static IGalleryLoadListener create(GalleryItemWrapper image) {
        return create(Collections.singletonList(image), 0);
    }


    /**
     * 无限长度加载
     *
     * @param position
     * @return
     */
    public static IGalleryLoadListener create(int position) {
        return new IGalleryLoadListener() {
            private final int COUNT_ONCE_LOAD = 5;

            @Override
            public Observable<GalleryItemWrapper> onLoadFirst() {
                return load(position);
            }

            @Override
            public Observable<Pair<List<GalleryItemWrapper>, Integer>> onLoadAll() {
                return Observable.just(null);
            }

            @Override
            public Observable<GalleryItemWrapper> onLoadPre(Context context, GalleryItemWrapper curItem) {
                return searchPre(curItem.getPosition(), COUNT_ONCE_LOAD)
                        .flatMap(this::load)
                        .switchIfEmpty(Observable.just(null))
                        .compose(((RxAppCompatActivity) context).bindToLifecycle());
            }

            @Override
            public Observable<GalleryItemWrapper> onLoadNext(Context context, GalleryItemWrapper curItem) {
                return searchNext(curItem.getPosition(), COUNT_ONCE_LOAD)
                        .flatMap(this::load)
                        .switchIfEmpty(Observable.just(null))
                        .compose(((RxAppCompatActivity) context).bindToLifecycle());
            }

            private Observable<GalleryItemWrapper> load(int position) {
                return Observable.defer(() ->
                        Observable.just(new ImageGalleryItem(position, DataManger.getInstance().getData(position)))
                );
            }

            private Observable<Integer> searchPre(int pos, int count) {
                return Observable.just(pos).filter(it -> pos >= 0)
                        .map(it -> DataManger.getInstance().getPreData(pos, count))
                        .filter(it1 -> !CollectionUtil.isEmpty(it1))
                        .flatMap(it1 -> Observable.from(it1)
                                //flatMap切换source,防止尾部switchIfEmpty无限循环
                                .filter(it2 -> DataManger.getInstance().isValid(it2))
                                .take(1)
                                .switchIfEmpty(Observable.defer(() -> searchPre(pos - count, count))));
            }

            private Observable<Integer> searchNext(int pos, int count) {
                return Observable.just(pos).filter(it -> pos >= 0)
                        .map(it -> DataManger.getInstance().getNextData(pos, count))
                        .filter(it1 -> !CollectionUtil.isEmpty(it1))
                        .flatMap(it1 -> Observable.from(it1)
                                //flatMap切换source,防止尾部switchIfEmpty无限循环
                                .filter(it2 -> DataManger.getInstance().isValid(it2))
                                .take(1)
                                .switchIfEmpty(Observable.defer(() -> searchNext(pos - count, count))));
            }

        };
    }
}
