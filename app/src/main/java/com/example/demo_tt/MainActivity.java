package com.example.demo_tt;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.content.SharedPreferences;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;

import android.graphics.Bitmap;
import androidx.annotation.Nullable;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;

import java.util.List;




// 主页面
public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView loadingText;

    private FloatingActionButton fabSwitchLayout; // 切换按钮

    private TabLayout bottomTabs;

    private ExperienceAdapter adapter;
    private StaggeredGridLayoutManager layoutManager;

    private boolean isLoading = false;      // 加载ing ？
    private int currentPage = 1;            // 页码
    private static final int PAGE_SIZE = 30; // 每页数量

    private int currentSpanCount = 2;  // 列数
    private static final int SPAN_COUNT_SINGLE = 1;  // 单列
    private static final int SPAN_COUNT_DOUBLE = 2;  // 双列
    private static final String PREFS_NAME = "AppSettings";
    private static final String KEY_SPAN_COUNT = "span_count";

    private View emptyView;

    // 一个Activity生命周期
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadLayoutPreference();

        initViews();

        // 初始化瀑布流
        initRecyclerView();

        // 初始化下拉刷新
        initSwipeRefresh();

        // 切换
        initSwitchButton();

        // 导航栏
        initBottomTabs();

        // 加载第一页数据
        loadInitialData();
    }

    // init
    private void initViews() {
        recyclerView = findViewById(R.id.recycler_view);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh);
        loadingText = findViewById(R.id.loading_text);
        fabSwitchLayout = findViewById(R.id.fab_switch_layout);
        emptyView = findViewById(R.id.empty_view);
        bottomTabs = findViewById(R.id.bottom_tabs);
    }

    // check 空状态
    private void checkEmptyState() {
        if (adapter.getItemCount() == 0) {
            emptyView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void loadLayoutPreference() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        currentSpanCount = prefs.getInt(KEY_SPAN_COUNT, SPAN_COUNT_DOUBLE);
    }

    private void saveLayoutPreference() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().putInt(KEY_SPAN_COUNT, currentSpanCount).apply();
    }

    private void initSwitchButton() {
        // 根据当前状态设置图标
        updateButtonIcon();

        // 切换单双列
        fabSwitchLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentSpanCount == SPAN_COUNT_DOUBLE) {
                    switchToSingleColumn();
                } else {
                    switchToDoubleColumn();
                }
            }
        });
    }

    // 切换单列
    private void switchToSingleColumn() {
        currentSpanCount = SPAN_COUNT_SINGLE;
        updateLayoutManager();
        saveLayoutPreference();
        updateButtonIcon();
//        Toast.makeText(this, "切换到单列", Toast.LENGTH_SHORT).show();
    }

    // 切换双列
    private void switchToDoubleColumn() {
        currentSpanCount = SPAN_COUNT_DOUBLE;
        updateLayoutManager();
        saveLayoutPreference();
        updateButtonIcon();
//        Toast.makeText(this, "切换到双列", Toast.LENGTH_SHORT).show();
    }

    private void updateButtonIcon() {
        // 单列时
        if (currentSpanCount == SPAN_COUNT_SINGLE) {
            fabSwitchLayout.setImageResource(android.R.drawable.ic_menu_sort_by_size);
            fabSwitchLayout.setRotation(90f);
        } else {
            fabSwitchLayout.setImageResource(android.R.drawable.ic_menu_view);
            fabSwitchLayout.setRotation(0f);
        }
    }

    private void updateLayoutManager() {
        int[] firstVisiblePositions = layoutManager.findFirstVisibleItemPositions(null);
        int firstPosition = 0;
        if (firstVisiblePositions != null && firstVisiblePositions.length > 0) {
            firstPosition = firstVisiblePositions[0];
        }

        recyclerView.clearOnScrollListeners(); // 清除旧listener

        layoutManager = new StaggeredGridLayoutManager(
                currentSpanCount,
                StaggeredGridLayoutManager.VERTICAL
        );
        layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_NONE);

        recyclerView.setLayoutManager(layoutManager);

        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(null);
        recyclerView.setItemViewCacheSize(20);

        RecyclerView.RecycledViewPool recycledViewPool = new RecyclerView.RecycledViewPool();
        recycledViewPool.setMaxRecycledViews(0, 20);
        recyclerView.setRecycledViewPool(recycledViewPool);

        recyclerView.addOnScrollListener(new SmartPreloadScrollListener()); // 添加新listener

        adapter.notifyDataSetChanged();

        recyclerView.scrollToPosition(firstPosition);

        fabSwitchLayout.animate()
                .scaleX(0.8f)
                .scaleY(0.8f)
                .setDuration(100)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        fabSwitchLayout.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(100)
                                .start();
                    }
                })
                .start();
    }

    // 底部导航栏
    private void initBottomTabs() {
        bottomTabs.addTab(bottomTabs.newTab().setText("首页"));
        bottomTabs.addTab(bottomTabs.newTab().setText("朋友"));
        bottomTabs.addTab(bottomTabs.newTab().setText("消息"));
        bottomTabs.addTab(bottomTabs.newTab().setText("我"));

        // 点击监听
        bottomTabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();

                switch (position) {
                    case 0:  // 首页
                        onHomeClicked();
                        break;
                    case 1:  // 朋友
                        onFriendsClicked();
                        break;
                    case 2:  // 消息
                        onMessageClicked();
                        break;
                    case 3:  // 我
                        onProfileClicked();
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                if (position == 0) {
                    recyclerView.smoothScrollToPosition(0);
                }
            }
        });
    }

    private void onHomeClicked() {
        android.util.Log.d("MainActivity", "点击首页");
        swipeRefreshLayout.setRefreshing(true);
        loadData();
    }

    private void onFriendsClicked() {
        android.util.Log.d("MainActivity", "点击朋友");
//        android.widget.Toast.makeText(this, "开发中",
//                android.widget.Toast.LENGTH_SHORT).show();
    }

    private void onMessageClicked() {
        android.util.Log.d("MainActivity", "点击消息");
//        android.widget.Toast.makeText(this, "开发中",
//                android.widget.Toast.LENGTH_SHORT).show();
    }

    private void onProfileClicked() {
        android.util.Log.d("MainActivity", "点击我");
//        android.widget.Toast.makeText(this, "开发中",
//                android.widget.Toast.LENGTH_SHORT).show();
    }

    // init 瀑布流
    private void initRecyclerView() {
        layoutManager = new StaggeredGridLayoutManager(currentSpanCount, StaggeredGridLayoutManager.VERTICAL); // 两列 垂直

        layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_NONE); // 防止item位置跳动

        recyclerView.setLayoutManager(layoutManager); // 设置manager

        // 性能优化
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(null);
        recyclerView.setItemViewCacheSize(30);  // 缓存大小
        RecyclerView.RecycledViewPool recycledViewPool = new RecyclerView.RecycledViewPool();
        recycledViewPool.setMaxRecycledViews(0, 30);
        recyclerView.setRecycledViewPool(recycledViewPool);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

        adapter = new ExperienceAdapter(this); // 创建适配器
        recyclerView.setAdapter(adapter);

        // 点赞监听
        adapter.setOnItemClickListener(new ExperienceAdapter.OnLikeClickListener() {
            @Override
            public void onLikeClick(int position, ExperienceCard card) {
                card.toggleLike();
                adapter.notifyItemChanged(position);

//                String message = card.isLiked() ? "点赞成功" : "取消点赞";
//                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
        recyclerView.addOnScrollListener(new SmartPreloadScrollListener());
    }

    // init 刷新
    private void initSwipeRefresh() {
        // 设置刷新颜色
        swipeRefreshLayout.setColorSchemeResources(
                android.R.color.holo_blue_light,
                android.R.color.holo_red_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_green_light
        );

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                currentPage = 1;
                loadData();
            }
        });
    }

    // init 开屏预加载数据
    private void loadInitialData() {
        // 检查是否有预加载数据
        if (MyApplication.hasPreloadedData()) {
            android.util.Log.d("MainActivity", "使用预加载数据");

            List<ExperienceCard> preloadedData = MyApplication.getAndClearPreloadedData();

            if (preloadedData != null && !preloadedData.isEmpty()) {
                adapter.setData(preloadedData);
                android.util.Log.d("MainActivity", "预加载数据已显示：" + preloadedData.size() + "条");
                return;
            }
        }

        // 没有预加载数据，正常加载
        android.util.Log.d("MainActivity", "没有预加载数据，正常加载");
        loadData();
    }

    // 加载数据 可用于刷新，下拉
    private void loadData() {
        swipeRefreshLayout.setRefreshing(true); // 刷新动画
        loadingText.setVisibility(View.GONE);

        if (!NetWork.isNetworkAvailable(this)) {
            swipeRefreshLayout.setRefreshing(false);

            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.GONE);

            // 无网络
            loadingText.setVisibility(View.VISIBLE);
            loadingText.setText("无网络连接\n\n请检查网络设置后重试");
            loadingText.setGravity(android.view.Gravity.CENTER);
            loadingText.setTextSize(18);

            android.util.Log.e("MainActivity", "无网络连接");
            return;
        }

        // Handler实现调用api
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                // 生成Mock数据
                List<ExperienceCard> data = MockDataGenerator.generateData(PAGE_SIZE);

                adapter.setData(data);

                swipeRefreshLayout.setRefreshing(false);

                // check 空状态
                checkEmptyState();

                recyclerView.scrollToPosition(0);

                // print
//                if (data.size() > 0) {
//                    Toast.makeText(MainActivity.this,
//                            "刷新成功，加载了 " + data.size() + " 条数据",
//                            Toast.LENGTH_SHORT).show();
//                }
            }
        }, 50);
    }

    // 加载更多数据
    private void loadMoreData() {
        if (isLoading) {
            return;  // 如果正在加载，直接返回
        }

        isLoading = true;
        currentPage++;

        // 显示加载提示
        loadingText.setVisibility(View.VISIBLE);
        loadingText.setText("Lodaing...");

        if (!NetWork.isNetworkAvailable(this)) {
            swipeRefreshLayout.setRefreshing(false);

            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.GONE);

            loadingText.setVisibility(View.VISIBLE);
            loadingText.setText("无网络连接\n\n请检查网络设置后下拉重试");
            loadingText.setGravity(android.view.Gravity.CENTER);
            loadingText.setTextSize(18);

            android.util.Log.e("MainActivity", "无网络连接");
            return;
        }

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                List<ExperienceCard> moreData = MockDataGenerator.generateData(PAGE_SIZE);

                adapter.addData(moreData);

                loadingText.setVisibility(View.GONE);

                isLoading = false;

//                Toast.makeText(MainActivity.this,
//                        "加载了 " + moreData.size() + " 条新数据",
//                        Toast.LENGTH_SHORT).show();
            }
        }, 100);  // 延迟0.5秒
    }

    private class SmartPreloadScrollListener extends RecyclerView.OnScrollListener {
        private int lastVisiblePosition = 0;
        private static final int PRELOAD_IMAGE_COUNT = 8;
        private static final int LOAD_MORE_THRESHOLD = 4;

        private int getMinPosition(int[] positions) {
            if (positions == null || positions.length == 0) {
                return 0;
            }

            int min = positions[0];
            for (int i = 1; i < positions.length; i++) {
                if (positions[i] < min) {
                    min = positions[i];
                }
            }
            return min;
        }

        private int getMaxPosition(int[] positions) {
            if (positions == null || positions.length == 0) {
                return 0;
            }

            int max = positions[0];
            for (int i = 1; i < positions.length; i++) {
                if (positions[i] > max) {
                    max = positions[i];
                }
            }
            return max;
        }

        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);

            int[] lastPositions = layoutManager.findLastVisibleItemPositions(null);
            if (lastPositions != null && lastPositions.length > 0) {
                lastVisiblePosition = getMaxPosition(lastPositions);
            }
        }

        @Override
        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);

            // 停止滚动时预加载
            if (newState == RecyclerView.SCROLL_STATE_IDLE && !isLoading) {
                preloadImagesAhead(lastVisiblePosition, PRELOAD_IMAGE_COUNT);
                int[] lastPositions = layoutManager.findLastCompletelyVisibleItemPositions(null);
                if (lastPositions != null) {
                    int lastPosition = getMaxPosition(lastPositions);

                    if (lastPosition >= adapter.getItemCount() - LOAD_MORE_THRESHOLD) {
                        loadMoreData();
                    }
                }
            }
        }

        private void preloadImage(final String url, int width, int height) {
            try {
                Glide.with(MainActivity.this)
                    .asBitmap() // 解码成 Bitmap
                    .load(url)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .override(width, height)
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(
                                Bitmap resource,
                                @Nullable Transition<? super Bitmap> transition) {

                            BitmapPreloadCache.put(url, resource);
                            android.util.Log.d("Preload", "预加载成功(BITMAP): " + url);
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {
                        }

                        @Override
                        public void onLoadFailed(@Nullable Drawable errorDrawable) {
                            android.util.Log.e("Preload", "预加载失败: " + url);
                        }
                    });

            } catch (Exception e) {
                android.util.Log.w("Preload", "预加载异常: " + url);
            }
        }

        private void preloadImagesAhead(int currentPosition, int count) {
            for (int i = 1; i <= count; i++) {
                int preloadPosition = currentPosition + i;

                if (preloadPosition >= adapter.getItemCount()) {
                    break;
                }

                ExperienceCard card = adapter.getItem(preloadPosition);
                if (card == null) {
                    continue;
                }
                // 主图
                preloadImage(card.getImageUrl(), 400, card.getImageHeight());
                // 头像
                preloadImage(card.getUserAvatar(), 100, 100);
            }
        }
    }
}