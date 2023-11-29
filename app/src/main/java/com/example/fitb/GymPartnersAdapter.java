package com.example.fitb;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class GymPartnersAdapter extends RecyclerView.Adapter<GymPartnersAdapter.ViewHolder> {
    private List<UserProfile> usersList = new ArrayList<>();
    private OnUserItemClickListener listener;
    public String userId1;

    public GymPartnersAdapter(OnUserItemClickListener listener) {
        this.listener = listener;
    }

    public void clearUsers() {
        usersList.clear();
        notifyDataSetChanged();
    }

    public interface OnUserItemClickListener {
        void onUserItemClick(UserProfile userProfile);
    }

    public void addUser(UserProfile user) {
        usersList.add(user);
        notifyDataSetChanged();
    }

    private void sendFriendRequest(String senderId, String receiverId) {
        // Implement the logic to send a friend request to the receiver
        // Save the request to the database and send a notification
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.gym_partner_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        UserProfile user = usersList.get(position);
        // Bind user data to the CardView here
        // Example: holder.nameTextView.setText(user.getName());
        holder.usernameTextView.setText(user.getName());
        holder.genderTextView.setText(user.getGender());
        Picasso.get().load(user.getProfilePicUrl()).into(holder.profilePic);

        //userId1=holder.userId;
        holder.bind(user);
    }

    @Override
    public int getItemCount() {
        return usersList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        // Define CardView elements here (e.g., TextViews for name, gender, etc.)
        TextView usernameTextView=itemView.findViewById(R.id.usernameTextView); // Define usernameTextView
        TextView genderTextView=itemView.findViewById(R.id.genderTextView);   // Define genderTextView
        String userId;
        ImageView profilePic;

        public ViewHolder(View itemView) {
            super(itemView);
            // Initialize CardView elements here
            usernameTextView = itemView.findViewById(R.id.usernameTextView); // Initialize usernameTextView
            genderTextView = itemView.findViewById(R.id.genderTextView);
            profilePic=itemView.findViewById(R.id.imageView2);

            // Set a click listener for the entire CardView
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Handle the click, e.g., navigate to a detailed profile view
                    Context context = itemView.getContext();
                    UserProfile selectedUser = new UserProfile(userId, usernameTextView.getText().toString(), genderTextView.getText().toString());
                    listener.onUserItemClick(selectedUser);

                    // Get the user's data from the TextViews
                    String username = usernameTextView.getText().toString();
                    String gender = genderTextView.getText().toString();
                    //Toast.makeText(context, "this is "+ selectedUser.getUserId(), Toast.LENGTH_SHORT).show();

                    //String userId = "unique_user_id"; // Get the user's unique ID

                    Intent intent = new Intent(context, UserProfileActivity.class);
                    intent.putExtra("userId", userId);
                    context.startActivity(intent);
                }
            });
        }

        public void bind(UserProfile user) {
            userId = user.getUserId();
            usernameTextView.setText(user.getName());
            genderTextView.setText(user.getGender());
        }
    }
}
