package com.example.fitb;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FriendRequestActivity extends AppCompatActivity {

    Button button,btnfind,btnChat;
    private RecyclerView recyclerViewFriendRequests;
    private FriendRequestsAdapter friendRequestsAdapter;
    private List<FriendRequest> friendRequests;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_request);


        // Get the current user's unique ID
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String userId = currentUser.getUid();

        String currentUserId = userId; // Replace this with the ID of the logged-in user

        DatabaseReference friendRequestsRef = FirebaseDatabase.getInstance().getReference("friend_requests");

// Query friend requests where the receiverId matches the current user's ID and the status is "pending"
        Query query = friendRequestsRef.orderByChild("receiverId").equalTo(currentUserId);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                friendRequests.clear(); // Clear the list to avoid duplicates
                for (DataSnapshot requestSnapshot : dataSnapshot.getChildren()) {
                    String senderId = requestSnapshot.child("senderId").getValue(String.class);
                    String receiverId = requestSnapshot.child("receiverId").getValue(String.class);
                    String status = requestSnapshot.child("status").getValue(String.class);
                    String requestId = requestSnapshot.getKey();
                    //FriendRequest friendRequest = new FriendRequest(senderId, receiverId, status);


//                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(); // Add this line
//                    Query query = databaseReference.child("friend_requests").orderByChild("receiverId").equalTo(currentUserId);
//                    // Get the UserProfile data for the senderId
//                    DatabaseReference userProfileRef = databaseReference.child("users").child(senderId);
//                    userProfileRef.addListenerForSingleValueEvent(new ValueEventListener() {
//                        @Override
//                        public void onDataChange(DataSnapshot userDataSnapshot) {
//                            if (userDataSnapshot.exists()) {
//                                String name = userDataSnapshot.child("name").getValue(String.class);
//                                String goal = userDataSnapshot.child("goal").getValue(String.class);
//
//                                String requestId = requestSnapshot.getKey();
//
//                                // Create a new FriendRequest instance with name and goal
//                                FriendRequest friendRequest = new FriendRequest(requestId,senderId, receiverId, status, name, goal);
//
//                                // Add the friend request to the list
//                                friendRequests.add(friendRequest);
//
//                                // Notify the adapter that the data has changed
//                                friendRequestsAdapter.notifyDataSetChanged();
//                            }
//                        }
//
//                        @Override
//                        public void onCancelled(DatabaseError databaseError) {
//                            // Handle errors
//                        }
//                    });
                    // Process the request
                    if ("pending".equals(status)) {
                        // This request is pending
                        // Notify the adapter that the data has changed
                        // Update your UI to display the request, and allow the user to accept or reject it

                        // Retrieve the UserProfile data for the senderId
                        DatabaseReference userProfileRef = FirebaseDatabase.getInstance().getReference().child("users").child(senderId);
                        userProfileRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot userDataSnapshot) {
                                if (userDataSnapshot.exists()) {
                                    String name = userDataSnapshot.child("name").getValue(String.class);
                                    String goal = userDataSnapshot.child("goal").getValue(String.class);

                                    // Create a new FriendRequest instance with name and goal
                                    FriendRequest friendRequest = new FriendRequest(requestId, senderId, receiverId, status, name, goal);


                                    // Add the friend request to the list
                                    friendRequests.add(friendRequest);

                                    // Notify the adapter that the data has changed
                                    friendRequestsAdapter.notifyDataSetChanged();
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                // Handle errors
                            }
                        });
                    }
                }


                // Check if friendRequests list is empty and show a "No Friend Requests" toast
                if (friendRequests.isEmpty()) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(FriendRequestActivity.this, "No Friend Requests", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }




            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle errors, such as database read issues.
            }
        });

        // In your onCreate method
        recyclerViewFriendRequests = findViewById(R.id.recyclerViewFriendRequests);
        friendRequests = new ArrayList<>();
        friendRequestsAdapter = new FriendRequestsAdapter(friendRequests,this);

// Set the adapter for the RecyclerView
        recyclerViewFriendRequests.setAdapter(friendRequestsAdapter);
        recyclerViewFriendRequests.setLayoutManager(new LinearLayoutManager(this));

    }
}