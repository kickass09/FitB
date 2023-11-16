package com.example.fitb;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class FriendRequestsAdapter extends RecyclerView.Adapter<FriendRequestsAdapter.ViewHolder> {
    private List<FriendRequest> friendRequests;
    private Activity activity;

    public FriendRequestsAdapter(List<FriendRequest> friendRequests, Activity activity) {
        this.friendRequests = friendRequests;
        this.activity = activity;
    }

    public FriendRequestsAdapter(List<FriendRequest> friendRequests) {
        this.friendRequests = friendRequests;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.friend_request_item, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FriendRequest friendRequest = friendRequests.get(position);

        holder.textViewFriendName.setText(friendRequest.getName());
        holder.textViewFriendGoal.setText(friendRequest.getGoal());

        // Get references to the accept and reject buttons
        Button buttonAccept = holder.buttonAccept;
        Button buttonReject = holder.buttonReject;

        // Set default state for buttons
        buttonAccept.setEnabled(true);
        buttonReject.setEnabled(true);

        // Set default text for buttons
        buttonAccept.setText("Accept");
        buttonReject.setText("Reject");

        buttonAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Handle the Accept button click
                updateRequestStatus(friendRequest.getRequestId(), "accepted");
                // Disable the buttons after accepting
                buttonAccept.setEnabled(false);
                buttonReject.setEnabled(true);
                // Change text to indicate accepted
                buttonAccept.setText("Accepted");
            }
        });

        buttonReject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Handle the Reject button click
                updateRequestStatus(friendRequest.getRequestId(), "rejected");
                // Disable the buttons after rejecting
                buttonAccept.setVisibility(View.INVISIBLE);
                buttonReject.setEnabled(false);
                // Change text to indicate rejected
                buttonReject.setText("Rejected");
            }
        });

        // Update UI based on the request status
        String status = friendRequest.getStatus();
        if ("accepted".equals(status)) {
            // If the status is accepted, disable both buttons and display "Accepted"
            buttonAccept.setEnabled(false);
            buttonReject.setEnabled(true);
            buttonAccept.setText("Accepted");
            buttonReject.setText("Reject");
        } else if ("rejected".equals(status)) {
            // If the status is rejected, disable both buttons and display "Rejected"
            buttonAccept.setVisibility(View.INVISIBLE);
            buttonReject.setEnabled(false);
            //buttonAccept.setText("Rejected");
            buttonReject.setText("Rejected");
        } else {
            // If the status is pending, buttons are already set to default state
        }


    }

    private void updateRequestStatus(String requestId, String newStatus) {
        Log.d("requestId",requestId);
        if (requestId == null) {
            // Handle the case where requestId is null
            // You can log an error message or take appropriate action

            return;
        }
        // Implement Firebase Database logic to update the request status
        DatabaseReference requestsRef = FirebaseDatabase.getInstance().getReference("friend_requests");

        // Create a reference to the specific request
        DatabaseReference requestRef = requestsRef.child(requestId);

        // Update the status to the new value
        requestRef.child("status").setValue(newStatus);
    }

    @Override
    public int getItemCount() {
        return friendRequests.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewFriendName;
        TextView textViewFriendGoal;
        Button buttonAccept,buttonReject;

        public ViewHolder(View itemView) {
            super(itemView);
            textViewFriendName = itemView.findViewById(R.id.textViewFriendName);
            textViewFriendGoal = itemView.findViewById(R.id.textViewFriendGoal);
            buttonAccept=itemView.findViewById(R.id.buttonAccept);
            buttonReject=itemView.findViewById(R.id.buttonReject);
        }
    }
}
