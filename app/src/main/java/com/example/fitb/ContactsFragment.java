package com.example.fitb;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
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

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ContactsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ContactsFragment extends Fragment {

    private RecyclerView recyclerViewFriendRequests;
    private FriendRequestsAdapter friendRequestsAdapter;
    private List<FriendRequest> friendRequests;



    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ContactsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ContactsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ContactsFragment newInstance(String param1, String param2) {
        ContactsFragment fragment = new ContactsFragment();
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
        View view = inflater.inflate(R.layout.fragment_contacts, container, false);

        // Initialize RecyclerView and the adapter
        recyclerViewFriendRequests = view.findViewById(R.id.recyclerViewFriendRequests);
        friendRequests = new ArrayList<>();
        friendRequestsAdapter = new FriendRequestsAdapter(friendRequests, requireActivity());

        // Set the adapter for the RecyclerView
        recyclerViewFriendRequests.setAdapter(friendRequestsAdapter);
        recyclerViewFriendRequests.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Get the current user's unique ID
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
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
                        Log.d("FriendRequest", "RequestSnapshot: " + requestSnapshot.getValue());
                        String senderId = requestSnapshot.child("senderId").getValue(String.class);
                        String receiverId = requestSnapshot.child("receiverId").getValue(String.class);
                        String status = requestSnapshot.child("status").getValue(String.class);
                        String requestId = requestSnapshot.getKey();

                        // Process the request

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

                    // Check if friendRequests list is empty and show a "No Friend Requests" toast
                    if (friendRequests.isEmpty()) {
                        //Toast.makeText(requireContext(), "No Friend Requests", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Handle errors, such as database read issues.
                }
            });
        }

        return view;
    }



}