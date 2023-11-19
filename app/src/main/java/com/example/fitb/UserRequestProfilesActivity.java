package com.example.fitb;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class UserRequestProfilesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_request_profiles);


        // Display user details in the activity's views
        ImageView imageView = findViewById(R.id.imageView);
        TextView textViewName = findViewById(R.id.textViewName);
        TextView textViewGender = findViewById(R.id.textViewGender);
        TextView textViewGoal = findViewById(R.id.textViewGoal);
        TextView textViewGymLocations = findViewById(R.id.textViewGymLocations);

        // Set the user details in the views
        // Assuming the User class has corresponding getter methods
        // Replace these with actual getter methods if they exist
        imageView.setImageResource(R.drawable.profile_image); // You may need to load the image using a library like Glide


        // Get data from the intent
        Intent intent = getIntent();
        String friendRequestId = intent.getStringExtra("friendRequestId");
        String senderId = intent.getStringExtra("senderId");

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(senderId);


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
}