package com.example.fitb;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class Profile extends AppCompatActivity {
    Spinner spinnerGoal;
    EditText etName,etGender,etGymLocations;
    Button btnSave;
    //public DatabaseReference databaseReference;
    DatabaseReference profileRef;

    private static final int GALLERY_REQUEST_CODE = 1;
    private ImageView profileImage;
    private Uri selectedImageUri;
    String userId;
    String ImagePicUrl;

    private CircleImageView userProfileimage;

    private static final int GalleryPick=1;

    Uri imageUri;
    StorageReference storageReference;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        //databaseReference = FirebaseDatabase.getInstance().getReference("users");


        EditText etName = findViewById(R.id.etName);
        EditText etGender = findViewById(R.id.etGender);
        spinnerGoal = findViewById(R.id.spinnerGoal);
        EditText etGymLocations = findViewById(R.id.etGymLocations);
        btnSave = findViewById(R.id.btnSave);

        userProfileimage=findViewById(R.id.profileImage);


        storageReference=FirebaseStorage.getInstance().getReference().child("Profile_images");


        userProfileimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent=new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent,GalleryPick);
            }
        });




// upload photo begin
// Inside your onCreate method
        profileImage = findViewById(R.id.profileImage);
        Button selectImagebtn = findViewById(R.id.selectImagebtn);
        Button uploadimagebtn= findViewById(R.id.uploadimagebtn);

//        selectImagebtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // Open the gallery
//                //selectImage1();
//
//            }
//        });



        //upload photo end

        //upload photo begin

        selectImagebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                selectImage();


            }
        });

        uploadimagebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                uploadImage();

            }
        });


// Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.goal_options, android.R.layout.simple_spinner_item);

// Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

// Apply the adapter to the spinner
        spinnerGoal.setAdapter(adapter);




        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            userId = user.getUid();

            // Create a reference to the Firebase Realtime Database
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

            profileRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
            // Create a reference to the user's profile
            DatabaseReference userProfileRef = databaseReference.child("users").child(userId);

            // Check if the user's profile node exists
            userProfileRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        // User's profile data exists
                        // Display the "Edit Profile" option

                        // Data exists, which means the user's profile information is present in the database

                        // Retrieve the user's profile data from the DataSnapshot
                        String name = dataSnapshot.child("name").getValue(String.class);
                        String gender = dataSnapshot.child("gender").getValue(String.class);
                        String goal = dataSnapshot.child("goal").getValue(String.class);
                        String gymLocations = dataSnapshot.child("gymLocations").getValue(String.class);
                        String profileImage=dataSnapshot.child("profilePicUrl").getValue(String.class);

                        // Assuming you have EditText fields for name, gender, goal, and gymLocations
//                        EditText etName = findViewById(R.id.etName);
//                        EditText etGender = findViewById(R.id.etGender);
//                        Spinner spinnerGoal = findViewById(R.id.spinnerGoal);
//                        EditText etGymLocations = findViewById(R.id.etGymLocations);

                        // Set the retrieved data into the UI components
                        Picasso.get().load(profileImage).into(userProfileimage);
                        etName.setText(name);
                        etGender.setText(gender);

                        // Set the selected item of the spinner based on the goal
                        int goalPosition = adapter.getPosition(goal); // Assuming you have an adapter for the spinner
                        spinnerGoal.setSelection(goalPosition);

                        etGymLocations.setText(gymLocations);

                        // Allow the user to edit and save the data

                    } else {
                        // User's profile data doesn't exist
                        // Allow the user to add their profile

                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Handle errors
                }
            });
        }


        // Add a click listener to the "Save" button
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Retrieve user input
                String name = etName.getText().toString();
                String gender = etGender.getText().toString();
                String goal = spinnerGoal.getSelectedItem().toString();
                String gymLocations = etGymLocations.getText().toString();
//                String profilePicUrl=userProfileimage.toString();
                // Get the current user's unique ID
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                String userId = user.getUid();

                UserProfile userProfile = new UserProfile(userId, name, gender, goal, gymLocations, ImagePicUrl);

                //UserProfile userProfile = new UserProfile(userId,name, gender, goal, gymLocations);
                profileRef.setValue(userProfile);





                if (user != null) {


                    // Create a reference to the Firebase Realtime Database
                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

                    // Save the user's profile information under their unique user ID
                    //DatabaseReference userLocationRef = databaseReference.child(userId).child("location");
                    DatabaseReference userRef = databaseReference.child("users").child(userId);






                    userRef.child("name").setValue(name);
                    userRef.child("gender").setValue(gender);
                    userRef.child("goal").setValue(goal);
                    userRef.child("gymLocations").setValue(gymLocations);
                    userRef.child("profilePicUrl").setValue(ImagePicUrl);

                    // Show a success message
                    Toast.makeText(Profile.this, "Profile information saved.", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(Profile.this,BottomNavi.class));
                    finish();
                }
            }
        });
    }

    private void selectImage() {

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,100);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 && data != null && data.getData() != null){

            imageUri = data.getData();
            profileImage.setImageURI(imageUri);


        }
    }



//        @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        Log.d("requestCode","This"+requestCode+" userId"+data);
//
//        if (requestCode == GalleryPick && resultCode==RESULT_OK &&  data != null){
//
//            Uri imageUri = data.getData();
////            CropImage.activity()

////                    .setGuidelines(CropImageView.Guidelines.ON)
////                    .start(this);
//            Toast.makeText(this, "This"+imageUri+" userId"+userId, Toast.LENGTH_SHORT).show();
//
//            Log.d("uderId","This"+imageUri+" userId"+userId);
//            userProfileimage.setImageURI(imageUri);
//
//            StorageReference filepath=storageReference.child(userId+".jpg");
//            filepath.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
//                @Override
//                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
//                    if(task.isSuccessful()){
//                        Toast.makeText(Profile.this, "Profile image uploaded", Toast.LENGTH_SHORT).show();
//                    }else{
//                        String message=task.getException().toString();
//                        Toast.makeText(Profile.this, "Error"+message, Toast.LENGTH_SHORT).show();
//                    }
//                }
//            });
//
//
//        }
//    }

    private void uploadImage() {

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Uploading File....");
        progressDialog.show();


        SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.CANADA);
        Date now = new Date();
        String fileName = formatter.format(now);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userId = user.getUid();
        storageReference = FirebaseStorage.getInstance().getReference("profile_photos/" + userId);


        storageReference.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        profileImage.setImageURI(imageUri);
                        Toast.makeText(Profile.this,"Successfully Uploaded",Toast.LENGTH_SHORT).show();

                        //save image url
                        // Inside the onSuccess listener of the photo upload
                        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                // Update the user's database entry with the image URL
                                ImagePicUrl = uri.toString();

                                // Get a reference to the user's entry in the Firebase Realtime Database
                                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
                                DatabaseReference userRef = databaseReference.child("users").child(userId);

                                // Update the 'profilePhotoUrl' field in the database
                                userRef.child("profilePicUrl").setValue(ImagePicUrl);
                            }
                        });


                        if (progressDialog.isShowing())
                            progressDialog.dismiss();

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {


                        if (progressDialog.isShowing())
                            progressDialog.dismiss();
                        Toast.makeText(Profile.this,"Failed to Upload",Toast.LENGTH_SHORT).show();


                    }
                });

    }





//    private void selectImage1() {
//        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
//        galleryIntent.setType("image/*");
//        startActivityForResult(galleryIntent, GALLERY_REQUEST_CODE);
//    }
//
//    // Handle the result from the gallery
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK) {
//            selectedImageUri = data.getData();
//            profileImage.setImageURI(selectedImageUri);
//        }
//    }
}