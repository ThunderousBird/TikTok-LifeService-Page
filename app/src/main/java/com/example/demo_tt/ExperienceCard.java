package com.example.demo_tt;

import java.security.KeyStore;
import java.util.Random;
public class ExperienceCard {
    // init
    private String id;
    private String imageUrl;
    private String title;
    private String userName;
    private String userAvatar;
    private int likeCount;
    private boolean isLiked;
    private int imageHeight;

    public ExperienceCard(String id, String imageUrl, String title,
                          String userName, String userAvatar, int likeCount) {
        this.id = id;
        this.imageUrl = imageUrl;
        this.title = title;
        this.userName = userName;
        this.userAvatar = userAvatar;
        this.likeCount = likeCount;
        this.isLiked = false;  // 默认未点赞
        this.imageHeight = 400;
    }

    public String getId() {
        return id;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getTitle() {
        return title;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserAvatar() {
        return userAvatar;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public boolean isLiked() {
        return isLiked;
    }

    public int getImageHeight() {
        return imageHeight;
    }

    public void setImageHeight(int imageHeight) {
        this.imageHeight = imageHeight;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

    public void setLiked(boolean liked) {
        isLiked = liked;
    }

    // touch like
    public void toggleLike() {
        isLiked = !isLiked;
        likeCount += isLiked == true ? 1 : -1;
    }
}
