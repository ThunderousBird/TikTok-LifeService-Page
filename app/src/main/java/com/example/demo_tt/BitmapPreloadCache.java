package com.example.demo_tt;

import android.graphics.Bitmap;
import android.util.LruCache;

public class BitmapPreloadCache {

    private static final int MAX_CACHE_SIZE_BYTES = (int)(Runtime.getRuntime().maxMemory() / 8);
    private static final LruCache<String, Bitmap> cache =
        new LruCache<String, Bitmap>(MAX_CACHE_SIZE_BYTES) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getByteCount();
            }
        };

    public static Bitmap get(String key) {
        return cache.get(key);
    }

    public static void put(String key, Bitmap bitmap) {
        if (key == null || bitmap == null) return;
        cache.put(key, bitmap);
    }

    public static void clear() {
        cache.evictAll();
    }
}
