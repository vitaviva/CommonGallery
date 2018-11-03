package com.vitaviva.commongallery.fragment;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.trello.rxlifecycle.components.support.RxFragment;
import com.vitaviva.commongallery.R;
import com.vitaviva.commongallery.drag.GalleryDragLayout;
import com.vitaviva.commongallery.itemview.GalleryPhotoView;
import com.vitaviva.commongallery.listener.IGalleryListenerProxy;
import com.vitaviva.commongallery.model.GalleryItemImpl;
import com.vitaviva.commongallery.model.GalleryItemWrapper;
import com.vitaviva.commongallery.presenter.GalleryLoadPresenter;
import com.vitaviva.commongallery.util.DisplayUtil;
import com.vitaviva.commongallery.util.NavigationBarUtil;
import com.vitaviva.commongallery.util.StatusBarUtil;
import com.vitaviva.commongallery.viewpager.GalleryListenerBundle;
import com.vitaviva.commongallery.viewpager.GalleryPagerAdapter;
import com.vitaviva.commongallery.viewpager.GalleryViewPager;

import static com.vitaviva.commongallery.Constants.KEY_LISTENER;


@SuppressLint("ValidFragment")
public class GalleryFragment extends RxFragment {
    private GalleryViewPager galleryPager;
    private ImageView ivDownload;
    private ImageView ivViewGrid;
    private GalleryItemWrapper curItem;
    private IGalleryListenerProxy listenerProxy;

    private Drawable thumbnail;
    private GalleryListenerBundle galleryListenerBundle = new GalleryListenerBundle();
    private GalleryLoadPresenter galleryLoadPresenter;
    private boolean clickFinish = true;
    private boolean canDownload = false;
    private boolean canViewGrid = false;
    private static final int HIDE_DOWNLOAD = 1;
    private static final int HIDE_GRIDVIEW = 2;
    private static final int DELAY_HIDE = 5000;
    private final Handler timerHandler = new Handler() {
        @Override
        public void dispatchMessage(Message msg) {
            switch (msg.what) {
                case HIDE_DOWNLOAD:
                    if (ivDownload != null) {
                        ivDownload.setVisibility(View.GONE);
                    }
                    break;
                case HIDE_GRIDVIEW:
                    if (ivViewGrid != null) {
                        ivViewGrid.setVisibility(View.GONE);
                    }
                    break;
                default:
                    break;
            }
        }
    };

    public void enableDragFinish(boolean dragFinish) {
        ((GalleryDragLayout) galleryPager.getParent()).setCanDragFinish(dragFinish);
    }

    public void setThumbnail(Drawable thumbnail) {
        this.thumbnail = thumbnail;
    }

    public void enableDownload(boolean canDownload) {
        this.canDownload = canDownload;
        if (canDownload) {
            ivDownload = new ImageView(getActivity());
            ivDownload.setImageResource(R.drawable.ic_gallery_download);
            ivDownload.setOnClickListener(v -> {
                if (curItem != null && curItem.convertView != null) {
                    curItem.convertView.download();//load交给photoview
                }
//                listenerProxy.onSave(getContext(),
//                        (GalleryItemForChat) getGalleryPager().getDatas().get(
//                                getGalleryPager().getCurrentItem()));

            });
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                    DisplayUtil.dip2px(getActivity(), 24), DisplayUtil.dip2px(getActivity(), 24));
            layoutParams.setMargins(0, 0, DisplayUtil.dip2px(getActivity(), 16),
                    DisplayUtil.dip2px(getActivity(), 20));
            layoutParams.gravity = Gravity.RIGHT | Gravity.BOTTOM;
            layoutParams.bottomMargin += NavigationBarUtil.getNavigationBarHeight(getActivity());
            ivDownload.setLayoutParams(layoutParams);
            ((FrameLayout) getView()).addView(ivDownload);
            ivDownload.setVisibility(View.GONE);
        }
    }

    private void checkNeedShowDownload(GalleryItemWrapper galleryItem) {
        if (canDownload) {
            ivDownload.setVisibility(View.VISIBLE);
            if (curItem != null && curItem.convertView != null
                    && curItem.convertView instanceof GalleryPhotoView) {
                if (!((GalleryPhotoView) curItem.convertView).autoCancel()) {
                    timerHandler.removeMessages(HIDE_DOWNLOAD);
                    return;
                }
            }
            timerHandler.removeMessages(HIDE_DOWNLOAD);
            timerHandler.sendEmptyMessageDelayed(HIDE_DOWNLOAD, DELAY_HIDE);
            return;
        }
        if (ivDownload != null) {
            ivDownload.setVisibility(View.GONE);
        }
    }

    private void checkNeedShowViewGrid(GalleryItemWrapper galleryItem) {
        if (canViewGrid) {
            ivViewGrid.setVisibility(View.VISIBLE);
            //暂时先这样处理，有查看原图按钮时不自动隐藏
            if (curItem != null && curItem.convertView != null
                    && curItem.convertView instanceof GalleryPhotoView) {
                if (!((GalleryPhotoView) curItem.convertView).autoCancel()) {
                    timerHandler.removeMessages(HIDE_GRIDVIEW);
                    return;
                }
            }
            timerHandler.removeMessages(HIDE_GRIDVIEW);
            timerHandler.sendEmptyMessageDelayed(HIDE_GRIDVIEW, DELAY_HIDE);
            return;
        }
        if (ivViewGrid != null) {
            ivViewGrid.setVisibility(View.GONE);
        }
    }

    private void toggleView(boolean toggled) {
        if (!canViewGrid) {
            return;
        }
        if (toggled) {
            timerHandler.removeMessages(HIDE_GRIDVIEW);
            ivViewGrid.setVisibility(View.VISIBLE);
        } else {
            ivViewGrid.setVisibility(View.GONE);
        }

    }

    public void enableViewGrid(boolean canViewGrid) {
        this.canViewGrid = canViewGrid;
        if (canViewGrid) {
            ivViewGrid = new ImageView(getActivity());
            ivViewGrid.setBackgroundResource(R.drawable.ic_gallery_grid);
            ivViewGrid.setOnClickListener(v -> {
//                Intent intent = new Intent(getActivity(), ChatMediaGridActivity.class);
//                intent.putExtra(BUNDLE_CHAT_ID, (((GalleryItemForChat) getGalleryPager().getDatas().get(
//                        getGalleryPager().getCurrentItem())).chatId));
//                startActivity(intent);
////                getActivity().finish();
            });
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                    DisplayUtil.dip2px(getActivity(), 24), DisplayUtil.dip2px(getActivity(), 24));
            layoutParams.setMargins(0, DisplayUtil.dip2px(getActivity(), 20),
                    DisplayUtil.dip2px(getActivity(), 16), 0);
            layoutParams.gravity = Gravity.RIGHT | Gravity.TOP;
            ivViewGrid.setLayoutParams(layoutParams);
            ((FrameLayout) getView()).addView(ivViewGrid);
            ivViewGrid.setVisibility(View.GONE);
        }
    }

    public void enableClickFinish(boolean enabled) {
        clickFinish = enabled;
    }

    public GalleryViewPager getGalleryPager() {
        return galleryPager;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViewPager();
        listenerProxy = (IGalleryListenerProxy) getArguments().getSerializable(KEY_LISTENER);
        galleryLoadPresenter = new GalleryLoadPresenter(getActivity(), galleryPager, listenerProxy);
        galleryLoadPresenter.load(thumbnail);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        GalleryDragLayout frameLayout = new GalleryDragLayout(getActivity());
        galleryPager = new GalleryViewPager(getActivity());
        galleryPager.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        frameLayout.addView(galleryPager);
        return frameLayout;
    }

    private void initViewPager() {
        galleryPager.setAdapter(new GalleryPagerAdapter(getActivity()));
        galleryPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onPageSelected(int position) {
                if (position < 0 || position >= galleryPager.getAdapter().getDatas().size())
                    return;
                GalleryItemWrapper item = galleryPager.getAdapter().getDatas().get(position);
                if (item == null) return;
                if (curItem == null || !item.equals(curItem)) {
                    item.onSlideIn();
                    if (item.convertView != null) {
                        if (StatusBarUtil.isTranslucentStatus(getActivity())) {
                            if (item.convertView.systemNavigationBarVisible()) {//NavigationBar透明
                                getActivity().getWindow().getDecorView().setSystemUiVisibility(
                                        getActivity().getWindow().getDecorView().getSystemUiVisibility()
                                                & ~View.SYSTEM_UI_FLAG_HIDE_NAVIGATION & ~View.SYSTEM_UI_FLAG_IMMERSIVE);
                                getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
                            } else {
                                getActivity().getWindow().getDecorView().setSystemUiVisibility(//NavigationBar不可见
                                        getActivity().getWindow().getDecorView().getSystemUiVisibility()
                                                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE);
                                getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
                            }
                        }
                    }
                    if (curItem != null) {
                        curItem.onSlideOut();
                    }
                    curItem = item;
                }
                checkNeedShowDownload(curItem);
                checkNeedShowViewGrid(curItem);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
//        galleryPager.getAdapter().setOnToggleListener(new GalleryVideoView.OnToggleListener() {
//            @Override
//            public void onToggleChanged(boolean toggled) {
//                toggleView(toggled);
//            }
//        });
        galleryPager.addOnPageChangeListener(galleryListenerBundle.getOnPageChangeListener());
        galleryPager.getAdapter().setGalleryListenerBundle(galleryListenerBundle);
        galleryListenerBundle.addListenerBundle(
                new GalleryListenerBundle(
                        arg0 -> {
                            GalleryItemWrapper galleryItem = galleryPager.getDatas().get(galleryPager.getCurrentItem());
                            if (galleryItem.type() == GalleryItemImpl.ItemType.TYPE_IMAGE) {
                                listenerProxy.onClicked(getActivity(), galleryItem);
                                if (!getActivity().isFinishing() && clickFinish) {
                                    getActivity().onBackPressed();
                                }
                            }
                        },
                        v -> {
                            listenerProxy.onLongClicked(getActivity(), galleryPager.getDatas().get(galleryPager.getCurrentItem()));
                            return false;
                        }
                ));
    }


    public Drawable snap() {
        if (curItem != null && curItem.convertView != null) {
            return curItem.convertView.snap();
        }
        return null;
    }

    public void setGalleryListenerBundle(GalleryListenerBundle galleryListenerBundle) {
        this.galleryListenerBundle.addListenerBundle(galleryListenerBundle);
    }
}
