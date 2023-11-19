package com.example.fitb;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class UserProfileActivity extends AppCompatActivity {

    //String userId = getIntent().getStringExtra("userId");

    private DatabaseReference notificaionRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        Intent intent = getIntent();
        String userId = intent.getStringExtra("userId");
//        String userId="HEtPz7DTbqNaqbfo9Hw2uh2sMwa2";

        notificaionRef=FirebaseDatabase.getInstance().getReference().child("Notifications");

        Button sendRequestButton = findViewById(R.id.sendRequestButton);
        // Get the current user's unique ID
        FirebaseUser currentUseruser = FirebaseAuth.getInstance().getCurrentUser();
        String currentUserId = currentUseruser.getUid();

        // Retrieve the saved button state from SharedPreferences
        boolean isRequestSent = isRequestSent(currentUserId, userId);

        // Set the initial text of the button based on the retrieved state
        updateButtonState(sendRequestButton, isRequestSent);

        // Set up a click listener for the Send Request button
        sendRequestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle the button click event here
                // You can implement the logic to send a request to the user with 'userId'
                //sendRequestToUser(userId);
                if(sendRequestButton.getText().toString().equals("Cancel Request")){
                    sendRequestCancel(currentUserId,userId);
                    sendRequestButton.setText("Send Request");

                }else{
                    sendFriendRequest(currentUserId,userId);
                    sendRequestButton.setText("Cancel Request");
                }

                // Save the updated button state in SharedPreferences
                saveRequestState(currentUserId, userId, sendRequestButton.getText().toString());

            }
        });

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(userId);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // User's details found in the database
                    String username = dataSnapshot.child("name").getValue(String.class);
                    String gender = dataSnapshot.child("gender").getValue(String.class);
                    String Goal=dataSnapshot.child("goal").getValue(String.class);
                    String GymLocations=dataSnapshot.child("gymLocations").getValue(String.class);
                    String profilePicUrl=dataSnapshot.child("profilePicUrl").getValue(String.class);
                    // Retrieve other user details in a similar manner

                    // Update your UI elements with the user's details
                    TextView usernameTextView = findViewById(R.id.textViewName);
                    TextView genderTextView = findViewById(R.id.textViewGender);
                    TextView textViewGoal = findViewById(R.id.textViewGoal);
                    TextView textViewGymLocations = findViewById(R.id.textViewGymLocations);
                    ImageView profilePhotoUrl=findViewById(R.id.imageView);

                    usernameTextView.setText("Name: "+username);
                    genderTextView.setText("Gender: " +gender);
                    textViewGoal.setText("Goal: " +Goal);
                    textViewGymLocations.setText("Preferred gym locations: "+GymLocations);
                    Picasso.get().load(profilePicUrl).into(profilePhotoUrl);

                    // Update other UI elements with the respective data
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle errors
            }
        });
    }


    // Method to check if a friend request has already been sent
    private boolean isRequestSent(String currentUserId, String userId) {
        // Retrieve the saved button state from SharedPreferences
        SharedPreferences preferences = getSharedPreferences("FriendRequests", MODE_PRIVATE);
        String buttonState = preferences.getString(currentUserId + userId, "Send Request");
        return buttonState.equals("Cancel Request");
    }

    // Method to save the button state in SharedPreferences
    private void saveRequestState(String currentUserId, String userId, String buttonState) {
        // Save the current button state in SharedPreferences
        SharedPreferences preferences = getSharedPreferences("FriendRequests", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(currentUserId + userId, buttonState);
        editor.apply();
    }

    // Method to update the button state based on whether a request has been sent
    private void updateButtonState(Button sendRequestButton, boolean isRequestSent) {
        if (isRequestSent) {
            sendRequestButton.setText("Cancel Request");
        } else {
            sendRequestButton.setText("Send Request");
        }
    }

    private void sendRequestCancel(String currentUserId, String userId) {
        DatabaseReference friendRequestsRef = FirebaseDatabase.getInstance().getReference("friend_requests");

        // Query the friend requests to find the specific request to delete
        friendRequestsRef
                .orderByChild("senderId")
                .equalTo(currentUserId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot requestSnapshot : dataSnapshot.getChildren()) {
                            String receiverId = requestSnapshot.child("receiverId").getValue(String.class);

                            if (receiverId.equals(userId)) {
                                // Found the request to cancel, delete it
                                String requestId = requestSnapshot.getKey();
                                friendRequestsRef.child(requestId).removeValue();

                                // Notify the user that the request has been canceled
                                sendNotificationToUser(userId, "Your friend request has been canceled.");
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        // Handle errors, such as database read issues.
                    }
                });
    }

    // Implement the logic to send a request to the user with the specified 'userId'
    private void sendRequestToUser(String userId) {
        // You can use Firebase or other methods to send the request to the user with the given 'userId'.
        // For example, you might update the user's requests list in the database.
        // Be sure to implement your own logic based on your application's requirements.
    }

    private void sendFriendRequest(String senderId, String receiverId) {
        DatabaseReference friendRequestsRef = FirebaseDatabase.getInstance().getReference("friend_requests");

        // Generate a unique request ID
        String requestId = friendRequestsRef.push().getKey();

        // Create a friend request object with sender, receiver, and status
        FriendRequest request = new FriendRequest(senderId, receiverId, "pending");

        // Save the request to the database
        friendRequestsRef.child(requestId).setValue(request);

        // Notify the receiver using FCM (optional)
        sendNotificationToUser(receiverId, "You have a new friend request!");
        HashMap<String,String> chatnotificationMap=new HashMap<>();
        chatnotificationMap.put("from",senderId);
        chatnotificationMap.put("type","request");
        notificaionRef.child(receiverId).push().setValue(chatnotificationMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if(task.isSuccessful()){

                }

            }
        });

    }


    private void sendNotificationToUser(String userId, String message) {
        // Implement FCM logic here to send a notification to the user with the specified 'userId'.
        // You can refer to Firebase FCM documentation for this part.
        String userFCMToken = getUserFCMToken(userId);
        if (userFCMToken != null) {
            // Create a notification message
            Map<String, String> notificationMessage = new HashMap<>();
            notificationMessage.put("to", userFCMToken);
            notificationMessage.put("priority", "high");

            Map<String, String> data = new HashMap<>();
            data.put("title", "Your App Name");
            data.put("body", message);
            notificationMessage.put("data", String.valueOf(data));

            // Send the notification using FCM
            // You can use a library like Retrofit to make HTTP POST requests.
            // Make a POST request to the FCM endpoint with the notification message.
            // The exact code for this part depends on your network library.
        }
    }

    // Method to retrieve the user's FCM token based on userId (You need to implement this)
    private String getUserFCMToken(String userId) {
        // Retrieve and return the user's FCM token from your database based on userId.
        // You need to implement the logic to get the FCM token.
        // This will vary depending on how you store FCM tokens in your database.
        // It's typically stored when the user logs into your app.
        return userId;
    }
}