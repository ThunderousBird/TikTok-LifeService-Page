package com.example.demo_tt;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.core.content.ContextCompat;
import android.graphics.PorterDuff;

import android.graphics.Bitmap;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;
import java.util.List;

public class ExperienceAdapter extends RecyclerView.Adapter<ExperienceAdapter.ViewHolder> {
    private final Context context;
    private final List<ExperienceCard> datalist;
    private OnLikeClickListener  listener;

    public interface OnLikeClickListener  {
        void onLikeClick(int position, ExperienceCard card);
    }

    public ExperienceAdapter(Context context) {
        this.context = context;
        this.datalist = new ArrayList<>();
    }

    public void setOnItemClickListener(OnLikeClickListener  listener) {
        this.listener = listener;
    }

    public void setData(List<ExperienceCard> newData) {
        this.datalist.clear();
        if (newData != null) {
            this.datalist.addAll(newData);
        }
        notifyDataSetChanged();  // 通知RecyclerView刷新
    }

    public void addData(List<ExperienceCard> newData) {
        if (newData == null || newData.isEmpty()) {
            return;
        }
        int oldSize = datalist.size();
        this.datalist.addAll(newData);
        // 只刷新新增的部分
        notifyItemRangeInserted(oldSize, newData.size());
    }

    public ExperienceCard getItem(int position) {
        if (position >= 0 && position < datalist.size()) {
            return datalist.get(position);
        }
        return null;
    }

    // RecyclerView实现
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.experience_card, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // 获取位置的数据
        ExperienceCard card = datalist.get(position);

        // 获取图片高度
        ViewGroup.LayoutParams params = holder.cardImage.getLayoutParams();
        params.height = card.getImageHeight();
        holder.cardImage.setLayoutParams(params);

        String imageUrl = card.getImageUrl();
        String avatarUrl = card.getUserAvatar();

        // 从Bitmap加载 主图
        Bitmap preBitmap = BitmapPreloadCache.get(imageUrl);
        if (preBitmap != null && !preBitmap.isRecycled()) {
            holder.cardImage.setImageBitmap(preBitmap);
            android.util.Log.d("PreBitmap", "使用Bitmap: " + imageUrl);
        } else {
            Glide.with(context)
                    .load(imageUrl)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .override(500, card.getImageHeight())
                    .placeholder(android.R.color.darker_gray)
                    .into(holder.cardImage);
            android.util.Log.d("PreBitmap", "正常加载: " + imageUrl);
        }

        // 头像
        Glide.with(context)
            .load(card.getUserAvatar())
            .placeholder(android.R.color.darker_gray)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .skipMemoryCache(false)
            .override(100, 100)  // 固定size
            .circleCrop()
            .dontAnimate()
            .into(holder.userAvatar);

        // 文字部分
        holder.title.setText(card.getTitle());
        holder.userName.setText(card.getUserName());
        holder.likeCount.setText(String.valueOf(card.getLikeCount()));

        if (card.isLiked()) {
            holder.likeIcon.setImageResource(R.drawable.heart_filled); // 点赞
        } else {
            holder.likeIcon.setImageResource(R.drawable.heart_empty);
        }

        // 点击点赞
        holder.likeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    int currentPosition = holder.getAbsoluteAdapterPosition();
                    if (currentPosition != RecyclerView.NO_POSITION && listener != null) {
                        ExperienceCard currentCard = datalist.get(currentPosition);
                        listener.onLikeClick(currentPosition, currentCard);
                        // liked
                        holder.likeIcon.animate()
                                .scaleX(1.3f)
                                .scaleY(1.3f)
                                .setDuration(100)
                                .withEndAction(new Runnable() {
                                    @Override
                                    public void run() {
                                        holder.likeIcon.animate()
                                                .scaleX(1.0f)
                                                .scaleY(1.0f)
                                                .setDuration(100)
                                                .start();
                                    }
                                }).start();
                    }
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return datalist.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder{
        ImageView cardImage; // 主图片
        ImageView userAvatar;
        TextView title;
        TextView userName;
        ImageView likeIcon;
        TextView likeCount;

        ViewHolder(View itemView) {
            super(itemView);
            cardImage = itemView.findViewById(R.id.card_image);
            userAvatar = itemView.findViewById(R.id.user_avatar);
            title = itemView.findViewById(R.id.card_title);
            userName = itemView.findViewById(R.id.user_name);
            likeIcon = itemView.findViewById(R.id.like_icon);
            likeCount = itemView.findViewById(R.id.like_count);
        }
    }
}
