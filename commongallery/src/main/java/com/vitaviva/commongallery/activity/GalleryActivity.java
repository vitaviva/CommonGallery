package com.vitaviva.commongallery.activity;

import android.annotation.TargetApi;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.transition.Transition;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.orhanobut.logger.Logger;
import com.trello.rxlifecycle.components.support.RxAppCompatActivity;
import com.vitaviva.commongallery.Constants;
import com.vitaviva.commongallery.R;
import com.vitaviva.commongallery.fragment.GalleryFragment;
import com.vitaviva.commongallery.listener.IGalleryListenerProxy;
import com.vitaviva.commongallery.util.CollectionUtil;
import com.vitaviva.commongallery.util.DisplayUtil;
import com.vitaviva.commongallery.util.ImageUtil;
import com.vitaviva.commongallery.util.OsVersionUtils;
import com.vitaviva.commongallery.util.ScreenOrientationHelper;
import com.vitaviva.commongallery.util.ToastUtil;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.subjects.PublishSubject;

import static com.vitaviva.commongallery.Constants.KEY_LISTENER;
import static com.vitaviva.commongallery.Constants.KEY_TRANSITION_DRAWABLE;
import static com.vitaviva.commongallery.Constants.TRANS_ANIM_DURATION_MLI;

public class GalleryActivity extends RxAppCompatActivity {
    private static final String TAG = "GalleryActivity";

    protected IGalleryListenerProxy listener;
    private ImageView transitionView;

    private GalleryFragment galleryFragment;
    private Transition.TransitionListener transitionListener;

    private ScreenOrientationHelper mScreenOrientationHelper;

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public GalleryActivity() {
        try {
            transitionListener = new Transition.TransitionListener() {
                @Override
                public void onTransitionStart(Transition transition) {
                }

                @Override
                public void onTransitionEnd(Transition transition) {
                    galleryFragment.getView().setVisibility(View.VISIBLE);
                    AndroidSchedulers.mainThread().createWorker().schedule(() -> {
                        if (transitionView != null) {
                            transitionView.setVisibility(View.GONE);
                        }
                    });
                }

                @Override
                public void onTransitionCancel(Transition transition) {

                }

                @Override
                public void onTransitionPause(Transition transition) {

                }

                @Override
                public void onTransitionResume(Transition transition) {

                }
            };
        } catch (Throwable e) {
            Logger.e(TAG, e);
            transitionListener = null;
        }
    }

    public void setWindowImmersive() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            getWindow().setStatusBarColor(Color.TRANSPARENT);

            /**
             * transition动画起点坐标的计算受到SYSTEM_UI_FLAG_FULLSCREEN影响,
             * 顾延迟设置SYSTEM_UI_FLAG_FULLSCREEN,保证动画坐标设置的准确，避免抖动。
             */
            AndroidSchedulers.mainThread().createWorker().schedule(() ->
                    getWindow().getDecorView().setSystemUiVisibility(
                            getWindow().getDecorView().getSystemUiVisibility()
                                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    ), 500, TimeUnit.MILLISECONDS);
        } else if (OsVersionUtils.hasKitKat()) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!checkData()) {
            ToastUtil.toast(this, R.string.com_err_data_invalid);
            finish();
            return;
        }
        setContentView(getLayout());
        if (enableFullScreen()) setWindowImmersive();
        mScreenOrientationHelper = new ScreenOrientationHelper(this);
        initTransition();
        initGalleryViewPager();
        enableLandscape(true, TRANS_ANIM_DURATION_MLI);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void onBackPressed() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                && DisplayUtil.getScreenHeight(this) > DisplayUtil.getScreenWidth(this) //竖屏下transition动画有效
                && transitionView != null) {
            if (galleryFragment.snap() != null) {
                transitionView.setImageDrawable(galleryFragment.snap());
            }
            transitionView.setLeft(galleryFragment.getGalleryPager().getLeft());
            transitionView.setTop(galleryFragment.getGalleryPager().getTop());
            transitionView.setRight(galleryFragment.getGalleryPager().getRight());
            transitionView.setBottom(galleryFragment.getGalleryPager().getBottom());
            transitionView.setScaleX(galleryFragment.getGalleryPager().getScaleX());
            transitionView.setScaleY(galleryFragment.getGalleryPager().getScaleY());
            transitionView.setVisibility(View.VISIBLE);
            galleryFragment.getView().setVisibility(View.GONE);
            getWindow().getSharedElementEnterTransition().removeListener(transitionListener);
            supportFinishAfterTransition();
            return;
        }
        finish();
        overridePendingTransition(R.anim.fade_in, R.anim.photo_scale_exit);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        enableLandscape(false, 0);
    }

    private boolean checkData() {
        listener = (IGalleryListenerProxy) getIntent().getSerializableExtra(KEY_LISTENER);
        return listener != null;
    }


    private void initTransition() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            byte[] bytes = this.getIntent().getByteArrayExtra(KEY_TRANSITION_DRAWABLE);
            if (!CollectionUtil.isEmpty(bytes)) {
                transitionView = findViewById(R.id.transition_view);
                transitionView.setImageDrawable(new BitmapDrawable(getResources(), ImageUtil.getBmpFromByteDealOOM(bytes)));
                getWindow().getSharedElementEnterTransition().setDuration(TRANS_ANIM_DURATION_MLI).addListener(transitionListener);
            }
        }
    }

    private void initGalleryViewPager() {
        galleryFragment = new GalleryFragment();
        galleryFragment.setArguments(getIntent().getExtras());
        galleryFragment.setThumbnail(transitionView != null ? transitionView.getDrawable() : null);
        getSupportFragmentManager().beginTransaction().add(R.id.gvPager, galleryFragment).commit();
        rx.subjects.Subject<Action0, Action0> subject = PublishSubject.create();
        subject.first().observeOn(AndroidSchedulers.mainThread()).subscribe(Action0::call, Throwable::printStackTrace);
        galleryFragment.getLifecycle().addObserver(new LifecycleObserver() {
            @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
            public void onResume() {
                subject.onNext(() -> {
                    onGalleryViewPagerInitialized(galleryFragment);
                    if (transitionView != null) {
                        //此处必须设置INVISIBLE!
                        //如设置GONE则无法触发GalleryPagerAdapter->instantiateItem->galleryItemView.onShownAsFirstPage
                        galleryFragment.getView().setVisibility(View.INVISIBLE);
                    }
                });
            }
        });

    }

    protected void onGalleryViewPagerInitialized(GalleryFragment galleryFragment) {
        galleryFragment.enableClickFinish(true);
        galleryFragment.enableDragFinish(true);
        galleryFragment.enableDownload(true);
        galleryFragment.enableViewGrid(getIntent().getBooleanExtra(Constants.EXTRA_CAN_VIEW_GRID, true));
    }

    protected @LayoutRes
    int getLayout() {
        return R.layout.activity_gallery;
    }

    protected boolean enableFullScreen() {
        return true;
    }

    private void enableLandscape(boolean enable, int delayMli) {
        if (enable) {
            Observable.just(null)
                    .delay(delayMli, TimeUnit.MILLISECONDS)
                    .compose(bindToLifecycle())
                    .subscribe(it -> mScreenOrientationHelper.enableSensorOrientation(), e -> Logger.e(TAG, e));
        } else {
            mScreenOrientationHelper.disableSensorOrientation();
        }
    }

}
