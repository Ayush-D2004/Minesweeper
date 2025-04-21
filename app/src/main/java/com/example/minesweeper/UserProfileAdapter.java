package com.example.minesweeper;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class UserProfileAdapter extends RecyclerView.Adapter<UserProfileAdapter.UserProfileViewHolder> {
    private List<User> users;
    private OnUserProfileClickListener listener;
    private UserDatabaseHelper dbHelper;

    public interface OnUserProfileClickListener {
        void onUserProfileClick(User user);
        void onProfileDeleted();
    }

    public UserProfileAdapter(List<User> users, OnUserProfileClickListener listener, UserDatabaseHelper dbHelper) {
        this.users = users;
        this.listener = listener;
        this.dbHelper = dbHelper;
    }

    @NonNull
    @Override
    public UserProfileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user_profile, parent, false);
        return new UserProfileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserProfileViewHolder holder, int position) {
        User user = users.get(position);
        holder.tvUsername.setText(user.getUsername());
        
        // Get the best high score across all difficulties
        int bestHighScore = Math.min(
            Math.min(user.getHighScore("easy"), user.getHighScore("medium")),
            user.getHighScore("hard")
        );
        
        String highScoreText = bestHighScore == Integer.MAX_VALUE ? 
            "No high score yet" : 
            "Best Score: " + bestHighScore + " seconds";
        holder.tvHighScore.setText(highScoreText);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onUserProfileClick(user);
            }
        });

        holder.ivDeleteButton.setOnClickListener(v -> {
            if (dbHelper.deleteUser(user.getUsername())) {
                users.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, users.size());
                if (listener != null) {
                    listener.onProfileDeleted();
                }
                Toast.makeText(v.getContext(), "Profile deleted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(v.getContext(), "Failed to delete profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public void updateUsers(List<User> newUsers) {
        this.users = newUsers;
        notifyDataSetChanged();
    }

    static class UserProfileViewHolder extends RecyclerView.ViewHolder {
        TextView tvUsername;
        TextView tvHighScore;
        ImageView ivProfileIcon;
        ImageView ivPlayButton;
        ImageView ivDeleteButton;

        UserProfileViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvHighScore = itemView.findViewById(R.id.tvHighScore);
            ivProfileIcon = itemView.findViewById(R.id.ivProfileIcon);
            ivPlayButton = itemView.findViewById(R.id.ivPlayButton);
            ivDeleteButton = itemView.findViewById(R.id.ivDeleteButton);
        }
    }
} 