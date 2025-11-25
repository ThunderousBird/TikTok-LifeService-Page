package com.example.demo_tt;

import android.content.Context;
import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory;
import com.bumptech.glide.load.engine.cache.LruResourceCache;
import com.bumptech.glide.load.engine.cache.MemorySizeCalculator;
import com.bumptech.glide.module.AppGlideModule;

@GlideModule
public class MyGlideModule extends AppGlideModule {

    @Override
    public void applyOptions(@NonNull Context context, @NonNull GlideBuilder builder) {
        android.util.Log.d("GlideConfig", "========================================");
        android.util.Log.d("GlideConfig", "MyGlideModule 已加载！");
        android.util.Log.d("GlideConfig", "========================================");
        // 计算推荐缓存大小
        MemorySizeCalculator calculator = new MemorySizeCalculator.Builder(context).setMemoryCacheScreens(2).build();

        // 设置内存缓存
        int memoryCacheSize = calculator.getMemoryCacheSize();
        builder.setMemoryCache(new LruResourceCache(memoryCacheSize));

        // 设置磁盘缓存
        int diskCacheSizeBytes = 1024 * 1024 * 250;  // MB
        builder.setDiskCache(new InternalCacheDiskCacheFactory(context, diskCacheSizeBytes));
        android.util.Log.d("GlideConfig", "内存缓存大小: " + (memoryCacheSize / 1024 / 1024) + " MB");
        android.util.Log.d("GlideConfig", "磁盘缓存大小: 250 MB");
    }

    @Override
    public boolean isManifestParsingEnabled() {
        return false;
    }
}
