package com.example.fitb;

import android.content.Intent;
import android.os.Bundle;
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

public class GymPartnersActivity extends AppCompatActivity {

    public interface FriendRequestCallback {
        void onFriendRequestCheck(boolean hasAcceptedRequest);
    }
    private RecyclerView recyclerView;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser currentUser = mAuth.getCurrentUser();
    //private GymPartnersAdapter adapter;
    GymPartnersAdapter adapter = new GymPartnersAdapter(new GymPartnersAdapter.OnUserItemClickListener() {
        @Override
        public void onUserItemClick(UserProfile userProfile) {
            // Handle item click here, userProfile will contain the user ID and other details
            String userId = userProfile.getUserId();
            // Navigate to the user's profile or perform any other actions
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gym_partners);

        recyclerView = findViewById(R.id.recyclerView);



        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize the adapter and set it to the RecyclerView
//        GymPartnersAdapter adapter = new GymPartnersAdapter();

        recyclerView.setAdapter(adapter);

        // Query Firebase for users with the same goal and populate the adapter
        queryUsersWithSameGoal();
    }

    private void queryUsersWithSameGoal() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users");

        String currentUserId = currentUser.getUid();

        // Retrieve the current user's goal from the Firebase Realtime Database
        databaseReference.child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Fetch the user's goal
                    String currentUserGoal = dataSnapshot.child("goal").getValue(String.class);

                    // Now you have the current user's goal
                    // Use it in the query to filter out friends
                    Query query = databaseReference.orderByChild("goal").equalTo(currentUserGoal);

                    query.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                // Iterate through the results and populate the adapter, excluding friends
                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                    String userId = snapshot.getKey(); // Get the user's ID

                                    // Check if there is an accepted friend request between the users
                                    if (!userId.equals(currentUserId)) {
                                        checkFriendRequest(currentUserId, userId, new FriendRequestCallback() {
                                            @Override
                                            public void onFriendRequestCheck(boolean hasAcceptedRequest) {
                                                if (!hasAcceptedRequest) {
                                                    UserProfile userProfile = snapshot.getValue(UserProfile.class);
                                                    adapter.addUser(userProfile);
                                                }
                                            }
                                        });
                                    }
                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            // Handle errors
                        }
                    });
                } else {
                    // The user's profile data does not exist, so navigate to the profile activity
                    // Replace ProfileActivity.class with the actual profile activity class
                    Toast.makeText(GymPartnersActivity.this, "You need to create a profile first", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(GymPartnersActivity.this, Profile.class);
                    startActivity(intent);
                    finish(); // Finish the current activity if needed
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle errors
            }
        });
    }

    private void checkFriendRequest(String currentUserId, String otherUserId, FriendRequestCallback callback) {
        DatabaseReference friendRequestsRef = FirebaseDatabase.getInstance().getReference("friend_requests");

        // Check if there is a friend request with status "accepted" between the two users
        Query query = friendRequestsRef.orderByChild("receiverId").equalTo(currentUserId);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean hasAcceptedRequest = false;
                for (DataSnapshot requestSnapshot : dataSnapshot.getChildren()) {
                    String senderId = requestSnapshot.child("senderId").getValue(String.class);
                    String status = requestSnapshot.child("status").getValue(String.class);

                    if (otherUserId.equals(senderId) && "accepted".equals(status)) {
                        // There is an accepted friend request between the users
                        // Set the flag to true and break the loop
                        hasAcceptedRequest = true;
                        break;
                    }
                }

                // Invoke the callback with the result
                callback.onFriendRequestCheck(hasAcceptedRequest);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle errors
            }
        });
    }

//    private void queryUsersWithSameGoal() {
//        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users");
//
//
//        String currentUserId = currentUser.getUid();
//        //DatabaseReference currentUserRef = databaseReference.child(currentUserId);
//        // Retrieve the current user's goal from the Firebase Realtime Database
//        databaseReference.child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                if (dataSnapshot.exists()) {
//                    // Fetch the user's goal
//                    String currentUserGoal = dataSnapshot.child("goal").getValue(String.class);
//
//                    // Now you have the current user's goal, and you can use it in the query
//                    Query query = databaseReference.orderByChild("goal").equalTo(currentUserGoal);
//
//                    query.addValueEventListener(new ValueEventListener() {
//                        @Override
//                        public void onDataChange(DataSnapshot dataSnapshot) {
//                            if (dataSnapshot.exists()) {
//                                // Iterate through the results and populate the adapter, excluding the current user
//                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
//                                    String userId = snapshot.getKey(); // Get the user's ID
//                                    if (!userId.equals(currentUserId)) {
//                                        UserProfile userProfile = snapshot.getValue(UserProfile.class);
//                                        adapter.addUser(userProfile);
//                                    }
//                                }
//                            }
//                        }
//
//                        @Override
//                        public void onCancelled(DatabaseError databaseError) {
//                            // Handle errors
//                        }
//                    });
//                }else{
//                    // The user's profile data does not exist, so navigate to the profile activity
//                    // Replace ProfileActivity.class with the actual profile activity class
//                    Toast.makeText(GymPartnersActivity.this, "You need to create a profile first", Toast.LENGTH_SHORT).show();
//                    Intent intent = new Intent(GymPartnersActivity.this, Profile.class);
//                    startActivity(intent);
//                    finish(); // Finish the current activity if needed
//
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//                // Handle errors
//            }
//        });
////        String currentUserGoal = "Lose Weight/Fat"; // Replace with the current user's goal
////
////        Query query = databaseReference.orderByChild("goal").equalTo(currentUserGoal);
////
////        query.addValueEventListener(new ValueEventListener() {
////            @Override
////            public void onDataChange(DataSnapshot dataSnapshot) {
////                if (dataSnapshot.exists()) {
////                    // Iterate through the results and populate the adapter
////                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
////                        UserProfile userProfile = snapshot.getValue(UserProfile.class);
////                        adapter.addUser(userProfile);
////                    }
////                }
////            }
////
////            @Override
////            public void onCancelled(DatabaseError databaseError) {
////                // Handle errors
////            }
////        });
//    }
}