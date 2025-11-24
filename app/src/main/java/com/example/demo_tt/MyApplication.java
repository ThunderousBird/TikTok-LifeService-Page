package com.example.demo_tt;

import android.app.Application;
import java.util.List;

// 存 开屏等待是加载的数据
public class MyApplication extends Application{
    private static List<ExperienceCard> preloadedData;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public static void setPreloadedData(List<ExperienceCard> data) {
        preloadedData = data;
    }

    public static List<ExperienceCard> getAndClearPreloadedData() {
        List<ExperienceCard> data = preloadedData;
        preloadedData = null;
        return data;
    }

    public static boolean hasPreloadedData() {
        return preloadedData != null && !preloadedData.isEmpty();
    }
}
