# TikTok-LifeService-Page
## A simple TikTok LifeService Page（Android）
一个仿TikTok经验分享频道的Android原生应用，采用瀑布流布局展示图文内容，支持单双列切换、智能预加载、网络缓存等功能。
## ✨ 功能特性
### 核心功能
- 🎨 **双列瀑布流布局**：RecyclerView + StaggeredGridLayoutManager 实现
- 🔄 **单双列切换**：支持一键切换单列/双列模式，布局偏好本地持久化
- 📥 **下拉刷新，上拉加载**：SwipeRefreshLayout + 滚动监听实现分页加载
- ⚡ **智能图片预加载**：自定义预加载策略，提前加载可见区域外的图片
- 🌐 **真实网络数据**：Picsum 获取真实图片
- 💾 **多级缓存机制**：Glide 内存缓存(50MB) + 磁盘缓存(250MB)
- 🚀 **开屏预加载**：启动时预加载首屏数据，提升首次展示速度
- 📡 **网络状态检测**：无网络时友好提示，避免空白卡片
### 交互功能
- ❤️ **点赞动画**：卡片点赞交互，带动画反馈
- 🏠 **底部导航**：TabLayout 实现/首页/朋友/消息/我/切换，并且支持双击{“首页”}进行刷新
- 🎯 **滚动优化**：禁用动画、增大缓存池等性能优化
### 项目结构
com.example.demo_tt/

├── MainActivity.java # 主界面Activity

├── PreMainActivate.java # 开屏页Activity

├── ExperienceAdapter.java # RecyclerView

├── ExperienceCard.java # 经验卡片

├── MockDataGenerator.java # Mock数据生成器

├── NetworkUtils.java # 网络状态检测工具

├── MyApplication.java # Application类(开屏页面加载)

└── MyGlideModule.java # Glide配置模块（内存缓存，磁盘缓存）
## 🎯 核心功能实现

### 1. 瀑布流布局
**特点**：
- 图片高度根据真实宽高比自适应计算
- 防止item位置跳动(`GAP_HANDLING_NONE`)
- 支持动态切换列数(1列/2列)
### 2. 智能预加载
**效果**：用户滑动时图片瞬间显示，无白屏（可根据网速适当跳帧预加载卡片多少）
### 3. 网络检查
如果网络断开直接显示提示
### 4. 开屏预加载✨
**流程**：开屏页显示图标的同时进行预加载 → 保存到Application → MainActivity直接使用

**效果**：打开跳转到主页面时就已经加载好首页的卡片和图片，提升用户体验

### 优化手段
- ✅ RecyclerView缓存池增大(30个)
- ✅ 禁用item动画
- ✅ 图片尺寸限制(override)
- ✅ Glide多级缓存
- ✅ 智能预加载策略