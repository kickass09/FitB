package com.example.fitb;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class UserProfileAdapter extends RecyclerView.Adapter<UserProfileAdapter.ViewHolder> {
    private List<UserProfile> userProfiles;
    private Context context;
    private UserProfileClickListener clickListener;

    public interface UserProfileClickListener {
        void onUserProfileClick(String userId, String name,String profilePicUrl);
    }

    public UserProfileAdapter(Context context, List<UserProfile> userProfiles, UserProfileClickListener clickListener) {
        this.context = context;
        this.userProfiles = userProfiles;
        this.clickListener = clickListener;
    }



    public UserProfileAdapter(List<UserProfile> userProfiles) {
        this.userProfiles = userProfiles;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_profile_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserProfile userProfile = userProfiles.get(position);
        holder.nameTextView.setText(userProfile.getName());
        holder.goalTextView.setText(userProfile.getGoal());
        // You can load the profile picture from the URL here
        // Load the profile picture using Picasso
        if (userProfile.getProfilePicUrl() != null && !userProfile.getProfilePicUrl().isEmpty()) {
            Picasso.get()
                    .load(userProfile.getProfilePicUrl())
                    .placeholder(R.drawable.profile_image) // Placeholder image while loading
                    .error(R.drawable.profile_image) // Error image if loading fails
                    .into(holder.profileImageView);
        } else {
            // If the profilePhotoUrl is empty or null, you can set a default image
            holder.profileImageView.setImageResource(R.drawable.profile_image);
        }

        // Set a click listener to open the ChatActivity
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Pass userId and name to the clickListener interface
                clickListener.onUserProfileClick(userProfile.getUserId(), userProfile.getName(),userProfile.getProfilePicUrl());
            }
        });



    }

    @Override
    public int getItemCount() {
        return userProfiles.size();
    }



    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        TextView goalTextView;
        ImageView profileImageView;

        public ViewHolder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.userProfileName);
            goalTextView = itemView.findViewById(R.id.userProfileGoal);
            profileImageView = itemView.findViewById(R.id.userProfileImage);
        }
    }
}

