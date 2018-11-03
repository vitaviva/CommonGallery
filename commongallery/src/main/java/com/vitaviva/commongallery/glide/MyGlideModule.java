package com.vitaviva.commongallery.glide;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.Registry;
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory;
import com.bumptech.glide.load.engine.cache.LruResourceCache;
import com.bumptech.glide.load.engine.cache.MemorySizeCalculator;
import com.bumptech.glide.load.engine.executor.GlideExecutor;
import com.bumptech.glide.module.AppGlideModule;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.signature.EmptySignature;

import static com.bumptech.glide.load.engine.executor.GlideExecutor.newDiskCacheExecutor;
import static com.bumptech.glide.load.engine.executor.GlideExecutor.newSourceExecutor;
import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

@com.bumptech.glide.annotation.GlideModule
public class MyGlideModule extends AppGlideModule {

    public static final int DISK_CACHE_SIZE = 500 * 1024 * 1024;
    private static final int DISK_CACHE_EXECUTOR_THREADS = 4;
    private static final String DISK_CACHE_EXECUTOR_NAME = "disk-cache";

    @Override
    public void applyOptions(Context context, GlideBuilder builder) {
        builder.setDiskCache(new InternalCacheDiskCacheFactory(context, DISK_CACHE_SIZE));
        builder.setMemoryCache(new LruResourceCache(new MemorySizeCalculator.Builder(context)
                .setMemoryCacheScreens(2).setMaxSizeMultiplier(0.5f).build().getMemoryCacheSize()));
        builder.setDefaultRequestOptions(RequestOptions.signatureOf(EmptySignature.obtain()));
        builder.setDefaultTransitionOptions(Drawable.class, withCrossFade());
        builder.setDiskCacheExecutor(newDiskCacheExecutor(DISK_CACHE_EXECUTOR_THREADS,
                DISK_CACHE_EXECUTOR_NAME, GlideExecutor.UncaughtThrowableStrategy.IGNORE));
        builder.setSourceExecutor(newSourceExecutor(GlideExecutor.UncaughtThrowableStrategy.IGNORE));
        builder.setLogLevel(Log.ERROR);
    }

    @Override
    public void registerComponents(@NonNull Context context, @NonNull Glide glide, @NonNull Registry registry) {
        super.registerComponents(context, glide, registry);
    }

}
