package com.example.demo_tt;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MockDataGenerator {
    // title
    private static final String[] TITLES = {
        "分享一个超实用的生活小技巧",
        "今天的晚霞真的太美了🌅",
        "强烈推荐这款宝藏APP",
        "周末出游好去处推荐✨",
        "这家餐厅真的太好吃了😋",
        "新买的相机拍照效果惊艳",
        "读书笔记｜《活着》",
        "健身一个月的变化对比",
        "手工制作的小物件分享",
        "旅行中捕捉的美好瞬间",
        "第一次尝试做烘焙🍰",
        "City Walk发现的小店",
        "办公桌改造分享💼",
        "养猫一年的心得体会🐱",
        "护肤品空瓶记录",
        "穿搭灵感｜秋冬季",
        "咖啡店探店记☕",
        "学习笔记整理技巧",
        "露营装备推荐⛺",
        "电影观后感分享🎬"
    };

    // user name
    private static final String[] NAMES = {
        "小红", "小明", "小李", "小王", "小张",
        "阿强", "阿美", "晓晓", "欢欢", "乐乐",
        "月月", "星星", "云云", "雨雨", "风风",
        "花花", "草草", "树树", "山山", "水水"
    };

    // api picture
    private static final String IMAGE_BASE = "https://picsum.photos/"; // https://via.placeholder.com/ https://picsum.photos/

    private static int imageCounter = 0;

    // 生成卡片数据
    public static List<ExperienceCard> generateData(int count) {
        List<ExperienceCard> list = new ArrayList<>();
        Random random = new Random();

        for (int i = 0; i < count; i++) {
            int width = 400;  // 宽度
            int height = 400 + random.nextInt(400);  // 高度

            long timestamp = System.currentTimeMillis();
            String imageUrl = IMAGE_BASE + width + "/" + height + "?random=" + timestamp + "_" + (imageCounter++); // 随机图片

            String title = TITLES[random.nextInt(TITLES.length)];
            String userName = NAMES[random.nextInt(NAMES.length)]; // 随机标题用户名

            String avatarUrl = IMAGE_BASE + "100/100?random=" + userName.hashCode(); // hash实现头像图片对应

            int likeCount = random.nextInt(10000); // 随机点赞数

            // 创建卡片
            ExperienceCard card = new ExperienceCard(
                    "card_" + timestamp + "_" + i,
                    imageUrl,
                    title,
                    userName,
                    avatarUrl,
                    likeCount
            );
            card.setImageHeight(height);
            list.add(card);
        }

        return list;
    }
}
