package com.example.fitb;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ChatsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChatsFragment extends Fragment implements UserProfileAdapter.UserProfileClickListener{

    private RecyclerView recyclerViewChats;
    private UserProfileAdapter userProfileAdapter;
    private List<UserProfile> userProfiles;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ChatsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ChatsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ChatsFragment newInstance(String param1, String param2) {
        ChatsFragment fragment = new ChatsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chats, container, false);

        // Initialize the RecyclerView and userProfiles list
        recyclerViewChats = view.findViewById(R.id.recyclerViewChats);
        userProfiles = new ArrayList<>();

        // Initialize the adapter and set it on the RecyclerView
        //userProfileAdapter = new UserProfileAdapter(userProfiles);

        userProfileAdapter = new UserProfileAdapter(getActivity(), userProfiles, this); // Pass 'this' to set the click listener
        recyclerViewChats.setAdapter(userProfileAdapter);
        recyclerViewChats.setLayoutManager(new LinearLayoutManager(getActivity()));

        // Fetch and populate user profiles (You can call a method to do this)

        fetchAndPopulateUserProfiles();

        return view;
    }

    @Override
    public void onUserProfileClick(String userId, String name,String profilePicUrl) {
        // Start the ChatActivity and pass userId and name as extras
        Intent intent = new Intent(getActivity(), ChatActivity.class); // Use 'getActivity()' to get the context
        intent.putExtra("userId", userId);
        intent.putExtra("name", name);
        intent.putExtra("profilePicUrl", profilePicUrl);
        startActivity(intent);
    }

//    private void fetchAndPopulateUserProfiles() {
//        // Fetch user profiles with "accepted" status from Firebase Realtime Database
//        DatabaseReference userProfilesRef = FirebaseDatabase.getInstance().getReference("users");
//        userProfilesRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                userProfiles.clear();
//                for (DataSnapshot userProfileSnapshot : dataSnapshot.getChildren()) {
//                    UserProfile userProfile = userProfileSnapshot.getValue(UserProfile.class);
//                    userProfiles.add(userProfile);
//                }
//                userProfileAdapter.notifyDataSetChanged();
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//                // Handle errors
//            }
//        });
//    }


    private void fetchAndPopulateUserProfiles() {
        // Get the current user's ID
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            // Handle the case where the user is not signed in
            return;
        }
        String currentUserId = currentUser.getUid();

        // Reference to the friend_requests node
        DatabaseReference friendRequestsRef = FirebaseDatabase.getInstance().getReference("friend_requests");

        // Query the friend_requests node to find accepted requests where the currentUserId matches senderId or receiverId
        friendRequestsRef.orderByChild("status").equalTo("accepted").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userProfiles.clear();
                for (DataSnapshot requestSnapshot : dataSnapshot.getChildren()) {
                    FriendRequest friendRequest = requestSnapshot.getValue(FriendRequest.class);

                    // Check if the current user's ID matches senderId or receiverId in the request
                    if (friendRequest != null && (friendRequest.getSenderId().equals(currentUserId) || friendRequest.getReceiverId().equals(currentUserId))) {
                        // Determine the ID of the other user (sender or receiver)
                        String otherUserId = (friendRequest.getSenderId().equals(currentUserId))
                                ? friendRequest.getReceiverId()
                                : friendRequest.getSenderId();

                        // Fetch the corresponding user profile
                        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users").child(otherUserId);
                        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                                UserProfile userProfile = userSnapshot.getValue(UserProfile.class);
                                userProfiles.add(userProfile);
                                userProfileAdapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                // Handle errors
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors
            }
        });
    }
}