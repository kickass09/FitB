package com.example.fitb;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private String messageReceiverId,messageSenderId;
    private String messageReceiverName;
    private String profilePicUrl;
    private TextView username,userLastSeen;
    private CircleImageView userImage;
    private ImageButton sendMessageButton;
    private EditText messageInputText;

    private DatabaseReference RootRef;
    private final List<Messages> messagesList=new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter messageAdapter;

    private RecyclerView userMessagesList;

    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mAuth=FirebaseAuth.getInstance();
        messageSenderId=mAuth.getCurrentUser().getUid();
        RootRef= FirebaseDatabase.getInstance().getReference();

        messageReceiverId=getIntent().getExtras().get("userId").toString();
        messageReceiverName=getIntent().getExtras().get("name").toString();
        profilePicUrl=getIntent().getExtras().get("profilePicUrl").toString();

        Toast.makeText(ChatActivity.this, messageReceiverId + messageReceiverName, Toast.LENGTH_SHORT).show();

        InitializeControllers();

        username.setText(messageReceiverName);
        Picasso.get().load(profilePicUrl).into(userImage);

        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendMessage();
            }
        });



    }

    private void InitializeControllers() {


        ActionBar actionBar=getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater layoutInflater=(LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView =  layoutInflater.inflate(R.layout.custom_chat_bar,null);
        actionBar.setCustomView(actionBarView);

        username=(TextView) findViewById(R.id.custom_profile_name);
        userLastSeen=(TextView) findViewById(R.id.custom_user_last_seen);
        userImage=(CircleImageView) findViewById(R.id.custom_profile_image);
        sendMessageButton=(ImageButton) findViewById(R.id.send_message_btn);
        messageInputText=(EditText) findViewById(R.id.input_message);

        messageAdapter=new MessageAdapter(messagesList);
        userMessagesList=(RecyclerView) findViewById(R.id.private_messages_list_of_users);
        linearLayoutManager=new LinearLayoutManager(this);
        userMessagesList.setLayoutManager(linearLayoutManager);
        userMessagesList.setAdapter(messageAdapter);

    }

    @Override
    protected void onStart() {
        super.onStart();
        RootRef.child("Messages").child(messageSenderId).child(messageReceiverId).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Messages messages=snapshot.getValue(Messages.class);
                messagesList.add(messages);
                messageAdapter.notifyDataSetChanged();

                userMessagesList.smoothScrollToPosition(userMessagesList.getAdapter().getItemCount());

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void SendMessage(){

        String messageText=messageInputText.getText().toString();
        if(TextUtils.isEmpty(messageText)){
            Toast.makeText(this, "Enter something first", Toast.LENGTH_SHORT).show();
        }else{
            String messageSenderRef="Messages/"+ messageSenderId + "/" + messageReceiverId;
            String messageReceiverRef="Messages/"+ messageReceiverId + "/" + messageSenderId;

            DatabaseReference userMessageKeyRef=RootRef.child("Messages").child(messageSenderId).child(messageReceiverId).push();
            String messagePushId=userMessageKeyRef.getKey();

            Map messageTextBody= new HashMap();
            messageTextBody.put("message",messageText);
            messageTextBody.put("type","text");
            messageTextBody.put("from",messageSenderId);

            Map messageBodyDetails=new HashMap();
            messageBodyDetails.put(messageSenderRef+"/"+messagePushId,messageTextBody);
            messageBodyDetails.put(messageReceiverRef+"/"+messagePushId,messageTextBody);

            RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if(task.isSuccessful()){
                        Toast.makeText(ChatActivity.this, "Message sent successfully", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                    }
                    messageInputText.setText("");
                }
            });

        }

    }
}