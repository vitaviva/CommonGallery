//package com.vitaviva.commongallery.itemview;
//
//import android.app.Activity;
//import android.content.Context;
//import android.graphics.drawable.Drawable;
//import android.net.Uri;
//import android.text.TextUtils;
//import android.util.AttributeSet;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.widget.ImageView;
//import android.widget.MediaController;
//
//import com.bumptech.glide.load.engine.DiskCacheStrategy;
//import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
//import com.trello.rxlifecycle.android.ActivityEvent;
//import com.trello.rxlifecycle.components.support.RxAppCompatActivity;
//import com.vitaviva.commongallery.model.GalleryItem;
//
//import java.io.File;
//
//import rx.android.schedulers.AndroidSchedulers;
//import rx.schedulers.Schedulers;
//
//
//
//public class GalleryVideoView extends GalleryItemView {
//    private static final String TAG = "GalleryVideoView";
//
//    @Override
//    public void onSlideOut() {
//        super.onSlideOut();
//        release();
//        updatePlayingState(VideoPlayingState.PLAY_FINISHED);
//    }
//    @Override
//    public void onSlideIn() {
//        super.onSlideIn();
//        if (mediaController != null && mPlayerView != null) {
//            mediaController.bindToVideoView(mPlayerView);
//            if(videoInfo!=null){
//                mediaController.initMediaBar(videoInfo.media);
//            }
//        }
//    }
//    private void release(){
//        if (mediaController != null) {
//            mediaController.unbindFromVideoView();
//        }
//
//        if (mPlayerView != null && mPlayerView.isPlaying()) {
//            mPlayerView.stopPlayback();
//            mPlayerView.release(true);
//        }
//    }
//
//    @Override
//    protected void onDetachedFromWindow() {
//        super.onDetachedFromWindow();
//    }
//
//    private ImageView mThumb;
//    private RoundProgressBar progressBar;
//    private IjkVideoView mPlayerView;
//    private MediaController mediaController;
//    private GalleryItem.VideoInfo videoInfo;
//    private View playBtn;
//    private View titleBar;
//    private IFileTransfer fileTransfer;
//
//    enum VideoPlayingState {
//        PLAY_STARTED,
//        PLAY_FINISHED,
//        PLAYING_FAILED,
//        DOWNLOAD_STARTED,
//        DOWNLOADING,
//        DOWNLOAD_FINISHED,
//        DOWNLOAD_FAILED;
//    }
//
//    private void updatePlayingState(VideoPlayingState playingState) {
//        switch (playingState) {
//            case DOWNLOAD_FAILED:
//                mThumb.setVisibility(VISIBLE);
//                playBtn.setVisibility(VISIBLE);
//                progressBar.setVisibility(GONE);
//                break;
//            case PLAY_FINISHED:
//                mThumb.setVisibility(VISIBLE);
//                if (progressBar.getVisibility() != VISIBLE) {
//                    playBtn.setVisibility(VISIBLE);
//                }
//                if (mediaController != null) {
//                    mediaController.setVisibility(View.GONE);
//                }
//                break;
//            case PLAY_STARTED:
//                mThumb.setVisibility(GONE);
//                titleBar.setVisibility(View.GONE);
//                mediaController.setVisibility(GONE);
//            case DOWNLOAD_FINISHED:
//                playBtn.setVisibility(GONE);
//                progressBar.setVisibility(GONE);
//                break;
//            case DOWNLOAD_STARTED:
//                progressBar.setProgress(0);
//            case DOWNLOADING:
//                mThumb.setVisibility(VISIBLE);
//                playBtn.setVisibility(GONE);
//                progressBar.setVisibility(VISIBLE);
//                break;
//        }
//    }
//
//
//    public GalleryVideoView(Context context) {
//        this(context, null);
//    }
//
//    public GalleryVideoView(Context context, AttributeSet attrs) {
//        super(context, attrs);
//        addView(LayoutInflater.from(context).inflate(R.layout.video_view, this, false));
//        titleBar = findViewById(R.id.video_title_bar);
//        titleBar.findViewById(R.id.close_btn).setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                ((Activity) context).onBackPressed();
//            }
//        });
//        mThumb = findViewById(R.id.thumb);
//        mThumb.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if(mediaController == null){
//                    return;
//                }
//                mediaController.toggle();
//            }
//        });
//        progressBar = (RoundProgressBar) findViewById(R.id.progressBar);
//        playBtn = findViewById(R.id.play_btn);
//        playBtn.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                prepareVideoPlay();
//            }
//        });
//        mediaController = findViewById(R.id.media_controller);
//        mediaController.setMediaPlayClickLisenter(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if(mediaController.isPlaying()){
//                    mPlayerView.pause();
//                    mediaController.pause();
//                } else if(mediaController.isIdle()){
//                    prepareVideoPlay();
//                } else if(mediaController.isPause()){
//                    mPlayerView.start();
//                    mediaController.play();
//                }
//            }
//        });
//        mediaController.setOnVisibleListener(new MediaController.OnVisibleListener() {
//            @Override
//            public void onVisibleChange(boolean isShow) {
//                if (titleBar != null) {
//                    titleBar.setVisibility(isShow ? VISIBLE : GONE);
//                }
//                if(onToggleListener!=null){
//                    onToggleListener.onToggleChanged(isShow);
//                }
//            }
//        });
//    }
//
//    @Override
//    public Drawable snap() {
//        return mThumb.getDrawable();
//    }
//
//    @Override
//    public void setCallBack(OnClickListener onClickListener, OnLongClickListener onLongClickListener) {
//        super.setCallBack(onClickListener, onLongClickListener);
//        mThumb.setOnLongClickListener(v -> {
//            if (onLongClickListener != null) {
//                onLongClickListener.onLongClick(v);
//            }
//            return false;
//        });
//        View anchorView = this.getParent() instanceof View ? (View) this.getParent() : this;
//        if (anchorView != null) {
//            anchorView.setOnLongClickListener(onLongClickListener);
//        }
//    }
//
//    @Override
//    public void onShownAsFirstPage() {
//        super.onShownAsFirstPage();
//        playBtn.setVisibility(GONE);
//        prepareVideoPlay();
//    }
//
//    @Override
//    public void load(GalleryItem galleryItem) {
//        super.load(galleryItem);
//        if (galleryItem.type != GalleryItem.ItemType.TYPE_VIDEO) {
//            return;
//        }
//        this.videoInfo = galleryItem.videoInfo;
//        showThumb(galleryItem.thumbnail);
//        mediaController.initMediaBar(videoInfo.media);
//    }
//
//    @Override
//    public boolean systemNavigationBarVisible() {
//        return true;
//    }
//
//    private void initVideoPlayer() {
//        if (mPlayerView == null) {
//            IjkMediaPlayer.loadLibrariesOnce(null);
//            IjkMediaPlayer.native_profileBegin("libijkplayer.so");
//            mPlayerView = findViewById(R.id.video);
//            mPlayerView.setOnCompletionListener(iMediaPlayer -> {
//                mPlayerView.setVisibility(GONE);
//                updatePlayingState(VideoPlayingState.PLAY_FINISHED);
//                mediaController.reset();//reset controllerbar
//            });
//            mPlayerView.setOnInfoListener(new IMediaPlayer.OnInfoListener() {
//                @Override
//                public boolean onInfo(IMediaPlayer iMediaPlayer, int i, int i1) {
//                    if (i == 3 //点击中间btn播放
//                            || i == 701 //点击seekbar播放
//                            ) {
//                        updatePlayingState(VideoPlayingState.PLAY_STARTED);
//                    }
//                    return false;
//                }
//            });
//            mediaController.bindToVideoView(mPlayerView);
//        }
//    }
//
//    private void showThumb(Drawable thumbnail) {
//        GlideApp.with(getContext())
//                .load(StringifyId.mediaId(videoInfo.thumbnail))
//                .error(GlideApp.with(getContext()).load(videoInfo.thumbnail)
//                        .transition(new DrawableTransitionOptions().dontTransition())
//                        .diskCacheStrategy(DiskCacheStrategy.NONE)
//                        .error(GlideApp.with(getContext()).load(videoInfo.media)
//                                .transition(new DrawableTransitionOptions().dontTransition())
//                                .diskCacheStrategy(DiskCacheStrategy.NONE)))
//                .fitCenter()
//                .placeholder(thumbnail)
//                .transition(new DrawableTransitionOptions().dontTransition())
//                .into(mThumb);
//    }
//
//    private File mFile;
//
//    public void prepareVideoPlay() {
//        MediaId mediaId = StringifyId.mediaId(videoInfo.media);
//        if (mediaId!= null) {//不是本地视频
//            prepareMediaVideoPlay(mediaId);
//        }else {
//            mFile = new File(videoInfo.media);
//            if(mFile.exists()){
//                play();
//            }
//        }
//
//        ((RxAppCompatActivity) getContext()).lifecycle()
//                .subscribe(it -> {
//                    if (it == ActivityEvent.PAUSE) {
//                        pause();
//                    } else if (it == ActivityEvent.RESUME) {
//                        resume();
//                    } else if (it == ActivityEvent.DESTROY) {
//                        release();
//                        checkNeedClean();
//                    }
//                }, e -> Logger.e(TAG, e));
//    }
//
//    private void prepareMediaVideoPlay(MediaId mediaId) {
//        if (fileTransfer != null && fileTransfer.isDownloading(mediaId)) {
//            updatePlayingState(VideoPlayingState.DOWNLOADING);
//            return;
//        }
//        if (mFile != null && mFile.exists()) {
//            play();
//            return;
//        }
//        S.Interface(IDataAccessManager.class).fetchData(SessionUtil.getOid(), new MediaMetaKey(mediaId), MediaMetaValue.class, IDataAccessManager.FetchMode.DEFAULT)
//                .subscribeOn(Schedulers.io())
//                .filter(it -> it != null && it.code() == 0)
//                .filter(it -> it.value() != null && it.value().value() != null)
//                .map(it -> it.value().value().v)
//                .compose(((RxAppCompatActivity) getContext()).bindToLifecycle())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(it -> {
//                    mFile = new File(Constants.LocalPath.FILE_DIR, LXFileUtil.encryptFileName(mediaId, it.getName()));
//                    Log.i(TAG, "prepareVideoPlay: "+it.categoryInfo.getCategoryVideo().duration);
//                    if (mFile.exists() && mFile.isFile()) {//已经下载 (缺乏视频完整性校验)
//                        Log.i(TAG, "video exist: length->"+mFile.length());
//                        play();
//                    } else {//未下载过
//                        Log.i(TAG, "video to downloaded: path->"+mFile.getAbsolutePath()+ " size->"+it.size);
//                        if(NetUtil.isWifi(getContext())){
//                            startDownload(mFile);
//                        }else if(NetUtil.is3G(getContext())){
//                            showDownloadAlert();
//                        }
//                    }
//                }, throwable -> {
//                    Logger.e(TAG, throwable);
//                    updatePlayingState(VideoPlayingState.DOWNLOAD_FAILED);
//                });
//    }
//
//    private void showDownloadAlert() {
//        if(mFile==null || !mFile.exists()){
//            return;
//        }
//        new LXAlertDialog.Builder(getContext())
//                .setCancelable(false)
//                .setMessage("您正在使用手机网络，下载视频将消耗手机流量，是否继续？")
//                .setPositiveButton(R.string.com_ok, (dialog, which) -> startDownload(mFile))
//                .setNegativeButton(R.string.com_cancel, (dialog, which) -> updatePlayingState(VideoPlayingState.DOWNLOAD_FAILED))
//                .show();
//    }
//
//    private void play(){
//        if(mFile==null || !mFile.exists()){
//            return;
//        }
//        initVideoPlayer();
//        mPlayerView.setVisibility(VISIBLE);
//        mPlayerView.setVideoURI(Uri.fromFile(mFile));
//        mPlayerView.requestFocus();
//        mPlayerView.setRenderView(new TextureRenderView(getContext()));
//        //默认播放时不显示titlebar和mediacontroller
//        titleBar.setVisibility(View.GONE);
//        mediaController.setVisibility(GONE);
//        mediaController.play();
//        mPlayerView.start();
//    }
//    //原来播放逻辑没有封装，暂时这样处理
//    private boolean bPaused = false;
//    private void pause() {
//        if(mediaController!=null && mPlayerView!=null){
//            onSlideOut();
//            mediaController.pause();
//            bPaused = true;
//        }
//    }
//    private void resume() {
//        if (mediaController != null && mPlayerView != null && bPaused) {
//            onSlideIn();
//            bPaused = false;
//        }
//    }
//
//    private void checkNeedClean() {
//        MediaId mediaId = StringifyId.mediaId(videoInfo.media);
//        if (mediaId != null) {
//            if (fileTransfer != null && fileTransfer.isDownloading(mediaId)) {
//                fileTransfer.cancel(mediaId, () -> {
////                    ToastUtil.toast("视频下载已取消");
//                });
//            }
//        }
//    }
//
//    private void startDownload(File path) {
//        if(videoInfo==null || TextUtils.isEmpty(videoInfo.media)){
//            return;
//        }
//        if(path!=null && path.exists()){
//            return;
//        }
//        //开始下载
//        if (fileTransfer == null) {
//            fileTransfer = S.Interface(IFileTransfer.class);
//        }
//        fileTransfer.download(StringifyId.mediaId(videoInfo.media), mFile, this);
//    }
//    private OnToggleListener onToggleListener;
//
//    public void setOnToggleListener(OnToggleListener onToggleListener) {
//        this.onToggleListener = onToggleListener;
//    }
//
//    public interface OnToggleListener{
//        void onToggleChanged(boolean toggled);
//    }
//
//}
