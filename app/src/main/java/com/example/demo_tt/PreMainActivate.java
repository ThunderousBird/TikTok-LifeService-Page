package com.example.demo_tt;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.List;

public class PreMainActivate extends AppCompatActivity{
    private static final int SPLASH_DURATION = 8000;  // 8s
    private boolean isDataReady = false;
    private boolean isTimeUp = false;

    private List<ExperienceCard> startPageData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pre_activate_main);
        if (!isTaskRoot()) {
            finish();
            return;
        }

        // 预加载
        startPreloading();

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                isTimeUp = true;
                checkAndJump();
            }
        }, SPLASH_DURATION);
    }

    private void startPreloading() {
//        if (!NetWork.isNetworkAvailable(this)) {
//            return;
//        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    startPageData = MockDataGenerator.generateData(30);


                    Log.d("PreMainActivate", "数据生成完成，共" + startPageData.size() + "条");

                    int preloadCount = Math.min(10, startPageData.size());
                    for (int i = 0; i < preloadCount; i++) {
                        ExperienceCard card = startPageData.get(i);

                        // 主图片
                        Glide.with(PreMainActivate.this)
                                .load(card.getImageUrl())
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .override(400, card.getImageHeight())
                                .preload();

                        // 头像
                        Glide.with(PreMainActivate.this)
                                .load(card.getUserAvatar())
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .override(100, 100)
                                .preload();
                    }
                    MyApplication.setPreloadedData(startPageData);
                    Log.d("PreMainActivate", "数据已保存");
                    isDataReady = true;
                } catch (Exception e) {
                    e.printStackTrace();
                    isDataReady = true;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            checkAndJump();
                        }
                    });
                }
            }
        }).start();
    }

    // 跳转检查
    private void checkAndJump() {
        if (isDataReady || isTimeUp) {
            jumpToMain();
        }
    }

    // 跳转
    private void jumpToMain() {
        Intent intent = new Intent(PreMainActivate.this, MainActivity.class);
        startActivity(intent);
        finish();  // 关闭开屏页
    }
}
