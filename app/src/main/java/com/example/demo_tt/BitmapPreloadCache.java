package com.example.demo_tt;

import android.graphics.Bitmap;
import android.util.LruCache;

public class BitmapPreloadCache {

    private static final int MAX_COUNT = 50; // 缓存大小

    private static final LruCache<String, Bitmap> cache =
            new LruCache<String, Bitmap>(MAX_COUNT) {
                @Override
                protected int sizeOf(String key, Bitmap value) {
                    return 1;
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
