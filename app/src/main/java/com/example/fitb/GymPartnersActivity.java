package com.example.fitb;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class GymPartnersActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private ProgressBar progressBar;
    private ProgressDialog progressDialog;

    Integer SearchDistance=10000;


    private FusedLocationProviderClient fusedLocationClient;
    private Location currentLocation;

    public interface LocationCallback {
        void onLocationAvailable(Location location);
    }

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
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        auth = FirebaseAuth.getInstance();
        progressBar = findViewById(R.id.progressBar);
        progressDialog = new ProgressDialog(GymPartnersActivity.this);
        progressDialog.setMessage("Searching for users with the same goal and within 10kms");
        progressDialog.show();
        // Add a delay of 3 seconds before dismissing the ProgressDialog
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                progressDialog.dismiss();
                queryUsersWithSameGoalAndDistance();
            }
        }, 3000);

        if (ActivityCompat.checkSelfPermission(GymPartnersActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Request location permission if not granted
            Log.d("hi","yo5");
            ActivityCompat.requestPermissions(GymPartnersActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }


        try{
            // Get the user's current location
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(GymPartnersActivity.this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            Log.d("hi","null");
                            if (location != null) {
                                //String lang= String.valueOf(location.getLatitude());
                                //.makeText(GymPartnersActivity.this, lang, Toast.LENGTH_SHORT).show();
                                //Log.d("hi","yo2"+lang);
                                currentLocation = location;
                                // Save location to Firebase under the user's account
                                saveLocationToFirebase(location.getLatitude(), location.getLongitude());
                            }else {
                                Log.d("Location", "Location is null");
                            }
                        }
                    });

        }catch (Exception e) {
            e.printStackTrace();
        }

        recyclerView = findViewById(R.id.recyclerView);



        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize the adapter and set it to the RecyclerView
//        GymPartnersAdapter adapter = new GymPartnersAdapter();

        recyclerView.setAdapter(adapter);

        // Query Firebase for users with the same goal and populate the adapter
        //queryUsersWithSameGoal();
//        queryUsersWithSameGoalAndDistance();


    }

    public void onUpdateSearchDistanceClick(View view) {
        EditText editTextDistance = findViewById(R.id.editTextDistance);
        String distanceStr = editTextDistance.getText().toString();

        if (!distanceStr.isEmpty()) {
            try {
                // Parse the entered distance
                int newDistance = Integer.parseInt(distanceStr);

                // Update the SearchDistance variable
                SearchDistance = newDistance*1000;

                // Refresh the RecyclerView with the updated distance
                queryUsersWithSameGoalAndDistance();
            } catch (NumberFormatException e) {
                // Handle invalid input (non-integer value)
                Toast.makeText(this, "Invalid distance value", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Handle empty input
            Toast.makeText(this, "Please enter a distance value", Toast.LENGTH_SHORT).show();
        }
    }

    private void queryUsersWithSameGoalAndDistance() {
        progressBar.setVisibility(View.VISIBLE);
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users");
        Log.d("Location", "Current location is null5");
        String currentUserId = currentUser.getUid();

        databaseReference.child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {

                    String currentUserGoal = dataSnapshot.child("goal").getValue(String.class);

                    double currentLatitude = 49.251934;
                    double currentLongitude = -123.049160;

                    if (currentLatitude == 0 && currentLongitude == 0) {
                        // Handle the case where the user's location is not available
                        return;
                    }

                    // Now you have the current user's goal and location
                    // Use them in the query to filter out friends within 10km
                    Query query = databaseReference.orderByChild("goal").equalTo(currentUserGoal);

                    query.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            adapter.clearUsers();
                            if (dataSnapshot.exists()) {
                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                    String userId = snapshot.getKey();

                                    // Check if there is an accepted friend request between the users
                                    if (!userId.equals(currentUserId)) {
                                        Double userLatitude = snapshot.child("location/latitude").getValue(Double.class);
                                        Double userLongitude = snapshot.child("location/longitude").getValue(Double.class);

                                        if (userLatitude != null && userLongitude != null) {
                                            // Calculate distance between users

                                            float[] results = new float[1];
                                            Location.distanceBetween(currentLatitude, currentLongitude, userLatitude, userLongitude, results);
                                            double distance1 = results[0];
                                            double distance = calculateDistance(currentLatitude, currentLongitude, userLatitude, userLongitude);

                                            // Check if the user is within 10km
                                            if (distance <= SearchDistance) {

                                                // Check if there is an accepted friend request
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
                            }else{
                                Log.d("dataSnapshot does not exists","true");
                            }

                            progressBar.setVisibility(View.GONE);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            // Handle errors
                            progressBar.setVisibility(View.GONE);
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
                    }else if (otherUserId.equals(senderId) && "rejected".equals(status)) {
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


    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        // Haversine formula to calculate distance

        // The radius of the Earth in meters
        double earthRadius = 6371000; // approximately 6371 km

        // Convert latitude and longitude from degrees to radians
        double lat1Rad = Math.toRadians(lat1);
        double lon1Rad = Math.toRadians(lon1);
        double lat2Rad = Math.toRadians(lat2);
        double lon2Rad = Math.toRadians(lon2);

        // Haversine formula
        double dLat = lat2Rad - lat1Rad;
        double dLon = lon2Rad - lon1Rad;
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                        Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double distance = earthRadius * c;

        return distance;
    }

    private void saveLocationToFirebase(double latitude, double longitude) {
        FirebaseUser user = auth.getCurrentUser();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users");
//        latitude=876888;
//        longitude=123232321;
        if (user != null) {
            String userId = user.getUid();
            //String userId="user_uid_1";
            DatabaseReference userLocationRef = databaseReference.child(userId).child("location");
            userLocationRef.child("latitude").setValue(latitude);
            userLocationRef.child("longitude").setValue(longitude);
            Log.d("Location", "Location saved");
        }else {
            Log.d("Location", "User is not authenticated");
        }




    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, you can now try to get the location again.
                //getLocationButton.performClick(); // Trigger the button click again
            } else {
                Log.d("Location", "Permission denied");
            }
        }
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