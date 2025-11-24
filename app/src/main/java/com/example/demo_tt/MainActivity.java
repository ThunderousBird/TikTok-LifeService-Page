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
import android.view.MenuItem;

import com.bumptech.glide.Glide;
import com.bumptech.glide.integration.recyclerview.RecyclerViewPreloader;
import com.bumptech.glide.request.RequestFutureTarget;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.util.ViewPreloadSizeProvider;
import com.bumptech.glide.ListPreloader.PreloadModelProvider;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;

import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import android.graphics.drawable.Drawable;

import java.util.List;
import java.util.Collections;




/**
 * 主界面Activity
 * 实现抖音经验频道的瀑布流效果
 */
public class MainActivity extends AppCompatActivity {

    // ========== 控件声明 ==========
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView loadingText;

    private FloatingActionButton fabSwitchLayout; // 切换按钮

    private TabLayout bottomTabs;

    // ========== 适配器和布局管理器 ==========
    private ExperienceAdapter adapter;
    private StaggeredGridLayoutManager layoutManager;

    // ========== 状态变量 ==========
    private boolean isLoading = false;      // 是否正在加载
    private int currentPage = 1;            // 当前页码
    private static final int PAGE_SIZE = 30; // 每页数量

    private int currentSpanCount = 2;  // 当前列数
    private static final int SPAN_COUNT_SINGLE = 1;  // 单列
    private static final int SPAN_COUNT_DOUBLE = 2;  // 双列
    private static final String PREFS_NAME = "AppSettings";
    private static final String KEY_SPAN_COUNT = "span_count";

    private View emptyView; // 空状态

    // ========== Activity生命周期 ==========

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
        // 根据当前状态设置按钮图标
        updateButtonIcon();

        // 点击切换
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

        // 长按显示当前模式
        fabSwitchLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                String mode = currentSpanCount == 1 ? "单列" : "双列";
//                Toast.makeText(MainActivity.this, "当前：" + mode, Toast.LENGTH_SHORT).show();
                return true;
            }
        });
    }

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

    /**
     * 初始化RecyclerView
     * 设置瀑布流布局管理器和适配器
     */
    private void initRecyclerView() {
        // ===== 创建瀑布流布局管理器 =====
        layoutManager = new StaggeredGridLayoutManager(currentSpanCount, StaggeredGridLayoutManager.VERTICAL); // 两列 垂直

        // 防止item位置跳动的关键设置
        layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_NONE);

        // 设置布局管理器
        recyclerView.setLayoutManager(layoutManager);

        // 性能优化
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(null);
        recyclerView.setItemViewCacheSize(30);  // 缓存大小增加到30
        RecyclerView.RecycledViewPool recycledViewPool = new RecyclerView.RecycledViewPool();
        recycledViewPool.setMaxRecycledViews(0, 30);  // 类型0，最多30个
        recyclerView.setRecycledViewPool(recycledViewPool);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

        // ===== 创建适配器 =====
        adapter = new ExperienceAdapter(this);
        recyclerView.setAdapter(adapter);

        // ===== 设置点赞监听 =====
        adapter.setOnItemClickListener(new ExperienceAdapter.OnLikeClickListener() {
            @Override
            public void onLikeClick(int position, ExperienceCard card) {
                // 切换点赞状态
                card.toggleLike();
                // 刷新这一项
                adapter.notifyItemChanged(position);

//                String message = card.isLiked() ? "点赞成功" : "取消点赞";
//                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });

        // ===== 添加滚动监听（实现上拉加载） =====
//        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrollStateChanged(@NonNull RecyclerView rv, int newState) {
//                super.onScrollStateChanged(rv, newState);
//                if (newState == RecyclerView.SCROLL_STATE_IDLE && !isLoading) {
//                    int[] lastPositions = layoutManager.findLastCompletelyVisibleItemPositions(null);
//                    int lastPosition = 0;
//                    for (int pos : lastPositions) {
//                        if (pos > lastPosition) {
//                            lastPosition = pos;
//                        }
//                    }
//                    if (lastPosition >= adapter.getItemCount() - 3) {
//                        loadMoreData();
//                    }
//                }
//
//
//            }
//        });
        recyclerView.addOnScrollListener(new SmartPreloadScrollListener());
    }

    /**
     * 初始化下拉刷新
     */
    private void initSwipeRefresh() {
        // 设置刷新动画的颜色
        swipeRefreshLayout.setColorSchemeResources(
                android.R.color.holo_blue_light,
                android.R.color.holo_red_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_green_light
        );

        // 设置下拉刷新监听
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // 重置页码
                currentPage = 1;
                // 重新加载数据
                loadData();
            }
        });
    }

    // ========== 数据加载方法 ==========
    /**
     * 加载初始数据
     */
    private void loadInitialData() {
        // 检查是否有预加载数据
        if (MyApplication.hasPreloadedData()) {
            android.util.Log.d("MainActivity", "使用预加载数据");

            // 获取预加载数据
            List<ExperienceCard> preloadedData = MyApplication.getAndClearPreloadedData();

            if (preloadedData != null && !preloadedData.isEmpty()) {
                // 直接使用，不需要等待
                adapter.setData(preloadedData);
                android.util.Log.d("MainActivity", "预加载数据已显示：" + preloadedData.size() + "条");
                return;
            }
        }

        // 没有预加载数据，正常加载
        android.util.Log.d("MainActivity", "没有预加载数据，正常加载");
        loadData();
    }

    /**
     * 加载刷新数据
     */
    private void loadData() {
        swipeRefreshLayout.setRefreshing(true); // 显示刷新动画
        loadingText.setVisibility(View.GONE);  // 隐藏底部加载

        if (!NetWork.isNetworkAvailable(this)) {
            // 停止刷新动画
            swipeRefreshLayout.setRefreshing(false);

            // 隐藏列表和空状态
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.GONE);

            // 显示无网络提示（一直显示，不自动隐藏）
            loadingText.setVisibility(View.VISIBLE);
            loadingText.setText("无网络连接\n\n请检查网络设置后下拉重试");
            loadingText.setGravity(android.view.Gravity.CENTER);
            loadingText.setTextSize(18);

            android.util.Log.e("MainActivity", "无网络连接");
            return;  // 不调用API
        }

        // 模拟网络请求延迟（实际项目中这里是网络请求）
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                // 生成Mock数据
                List<ExperienceCard> data = MockDataGenerator.generateData(PAGE_SIZE);

                // 设置数据到适配器
                adapter.setData(data);

                // 停止刷新动画
                swipeRefreshLayout.setRefreshing(false);

                // check 空状态
                checkEmptyState();

                // 滚动到顶部
                recyclerView.scrollToPosition(0);

                // print
//                if (data.size() > 0) {
//                    Toast.makeText(MainActivity.this,
//                            "刷新成功，加载了 " + data.size() + " 条数据",
//                            Toast.LENGTH_SHORT).show();
//                }
            }
        }, 500);  // 延迟0.5秒，模拟网络请求
    }

    /**
     * 加载更多数据
     */
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
            // 停止刷新动画
            swipeRefreshLayout.setRefreshing(false);

            // 隐藏列表和空状态
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.GONE);

            // 显示无网络提示
            loadingText.setVisibility(View.VISIBLE);
            loadingText.setText("无网络连接\n\n请检查网络设置后下拉重试");
            loadingText.setGravity(android.view.Gravity.CENTER);
            loadingText.setTextSize(18);

            android.util.Log.e("MainActivity", "无网络连接");
            return; // 直接return
        }

        // 模拟网络请求
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                // 生成更多数据
                List<ExperienceCard> moreData = MockDataGenerator.generateData(PAGE_SIZE);

                // 添加到适配器
                adapter.addData(moreData);

                // 隐藏加载提示
                loadingText.setVisibility(View.GONE);

                // 重置加载状态
                isLoading = false;

//                Toast.makeText(MainActivity.this,
//                        "加载了 " + moreData.size() + " 条新数据",
//                        Toast.LENGTH_SHORT).show();
            }
        }, 500);  // 延迟0.5秒
    }

    private class SmartPreloadScrollListener extends RecyclerView.OnScrollListener {
        private int lastVisiblePosition = 0;
        private int scrollDirection = 0;
        private static final int PRELOAD_IMAGE_COUNT = 8;
        private static final int LOAD_MORE_THRESHOLD  = 4;

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

            // ===== 1. 获取当前可见位置 =====
            int[] firstPositions = layoutManager.findFirstVisibleItemPositions(null);
            int[] lastPositions = layoutManager.findLastVisibleItemPositions(null);

            if (firstPositions == null || lastPositions == null) {
                return;
            }

            if (firstPositions.length == 0 || lastPositions.length == 0) {
                return;
            }

            int currentFirstPosition = getMinPosition(firstPositions);
            int currentLastPosition = getMaxPosition(lastPositions);

            // ===== 2. 判断滚动方向 =====
            if (currentLastPosition > lastVisiblePosition) {
                scrollDirection = 1;  // 向下滚动
                onScrollDown(currentLastPosition);
            } else if (currentFirstPosition < lastVisiblePosition) {
                scrollDirection = -1;  // 向上滚动
                onScrollUp(currentFirstPosition);
            }

            lastVisiblePosition = currentLastPosition;
        }

        @Override
        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);

            // ===== 3. 停止滚动时检查是否需要加载更多数据 =====
            if (newState == RecyclerView.SCROLL_STATE_IDLE && !isLoading) {
                int[] lastPositions = layoutManager.findLastCompletelyVisibleItemPositions(null);
                if (lastPositions != null) {
                    int lastPosition = getMaxPosition(lastPositions);

                    // 距离底部小于阈值时，加载更多数据
                    if (lastPosition >= adapter.getItemCount() - LOAD_MORE_THRESHOLD) {
                        loadMoreData();
                    }
                }
            }
        }

        private void preloadImage(String url, int width, int height) {
            try {
                Glide.with(MainActivity.this)
                        .load(url)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .override(width, height)
                        .listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onResourceReady(Drawable resource, Object model,
                                                           Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                android.util.Log.d("Preload", "预加载成功: " + url);
                                return false;
                            }
                            @Override
                            public boolean onLoadFailed(GlideException e, Object model,
                                                        Target<Drawable> target, boolean isFirstResource) {
                                android.util.Log.e("Preload", "预加载失败: " + url);
                                return false;
                            }
                        })
                        .preload();  // 关键：只预加载，不显示
            } catch (Exception e) {
                // 预加载失败不影响主流程
                android.util.Log.w("Preload", "预加载失败: " + url);
            }
        }

        private void onScrollDown(int currentLastPosition) {
            // 预加载接下来的图片
            preloadImagesAhead(currentLastPosition, PRELOAD_IMAGE_COUNT);
        }

        private void preloadImagesAhead(int currentPosition, int count) {
            for (int i = 1; i <= count; i++) {
                int preloadPosition = currentPosition + i;

                // 检查位置是否有效
                if (preloadPosition >= adapter.getItemCount()) {
                    break;
                }

                // 获取要预加载的卡片数据
                ExperienceCard card = adapter.getItem(preloadPosition);
                if (card == null) {
                    continue;
                }

                // ===== 预加载主图片 =====
                preloadImage(card.getImageUrl(), 400, card.getImageHeight());

                // ===== 预加载头像（可选，头像通常很小）=====
                preloadImage(card.getUserAvatar(), 100, 100);
            }
        }

        private void onScrollUp(int currentFirstPosition) {
            // 向上滚动时也可以预加载前面的图片（可选）
             preloadImagesBefore(currentFirstPosition, 3);
        }

        private void preloadImagesBefore(int currentPosition, int count) {
            for (int i = 1; i <= count; i++) {
                int preloadPosition = currentPosition + i;

                // 检查位置是否有效
                if (preloadPosition >= adapter.getItemCount()) {
                    break;
                }

                // 获取要预加载的卡片数据
                ExperienceCard card = adapter.getItem(preloadPosition);
                if (card == null) {
                    continue;
                }

                // ===== 预加载主图片 =====
                preloadImage(card.getImageUrl(), 400, card.getImageHeight());

                // ===== 预加载头像（可选，头像通常很小）=====
                preloadImage(card.getUserAvatar(), 100, 100);
            }
        }

    }

//    private void setupImagePreload() {
//        // 创建预加载大小提供者
//        ViewPreloadSizeProvider<ExperienceCard> sizeProvider =
//                new ViewPreloadSizeProvider<>();
//
//        // 创建预加载模型提供者
//        PreloadModelProvider<ExperienceCard> modelProvider =
//                new PreloadModelProvider<ExperienceCard>() {
//                    @NonNull
//                    @Override
//                    public List<ExperienceCard> getPreloadItems(int position) {
//                        ExperienceCard card = adapter.getItem(position);
//                        if (card == null) {
//                            return Collections.emptyList();
//                        }
//                        return Collections.singletonList(card);
//                    }
//
//                    @Override
//                    public RequestBuilder<?> getPreloadRequestBuilder(@NonNull ExperienceCard card) {
//                        return Glide.with(MainActivity.this)
//                                .load(card.getImageUrl())
//                                .override(500, card.getImageHeight());
//                    }
//                };
//
//        // 创建预加载器（预加载10个item）
//        RecyclerViewPreloader<ExperienceCard> preloader =
//                new RecyclerViewPreloader<>(
//                        Glide.with(this),
//                        modelProvider,
//                        sizeProvider,
//                        10  // 预加载数量
//                );
//
//        // 添加到RecyclerView
//        recyclerView.addOnScrollListener(preloader);
//    }

}