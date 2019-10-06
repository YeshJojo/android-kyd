package com.example.map;

import android.graphics.Color;
import android.net.Uri;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    private List<Messages> userMessagesList;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;
    public MessageAdapter (List<Messages> userMessagesList) {
        this.userMessagesList = userMessagesList;
    }
    public class MessageViewHolder extends RecyclerView.ViewHolder {
        public TextView senderMessageText, receiverMessageText, senderMsgTime, receiverMsgTime;
        public CircleImageView receiverProfileImage;
        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            senderMessageText = itemView.findViewById(R.id.sender_messsage_text);
            receiverMessageText = itemView.findViewById(R.id.receiver_message_text);
            senderMsgTime = itemView.findViewById(R.id.sender_message_time);
            receiverMsgTime = itemView.findViewById(R.id.receiver_message_time);
            receiverProfileImage = itemView.findViewById(R.id.message_profile_image);
        }
    }
    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.custom_messages_layout, viewGroup, false);
        mAuth = FirebaseAuth.getInstance();
        return new MessageViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder messageViewHolder, int i) {
        String messageSenderId = mAuth.getCurrentUser().getUid();
        Messages messages = userMessagesList.get(i);
        String fromUserID = messages.getFrom();
        String fromMessageType = messages.getType();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserID);
        final StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Profile Images");

        storageReference.child(fromUserID+".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Log.d("UriImageChat",uri.toString());
                Picasso.get().load(uri).into(messageViewHolder.receiverProfileImage);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
            }
        });

        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("image")) {
                    String receiverImage = dataSnapshot.child("image").getValue().toString();
                    Picasso.get().load(receiverImage).placeholder(R.drawable.ic_boy).into(messageViewHolder.receiverProfileImage);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });
        messageViewHolder.receiverMessageText.setVisibility(View.GONE);
        messageViewHolder.receiverProfileImage.setVisibility(View.GONE);
        messageViewHolder.senderMessageText.setVisibility(View.GONE);
        if (fromMessageType.equals("text")) {
            if (fromUserID.equals(messageSenderId)) {
                messageViewHolder.senderMessageText.setVisibility(View.VISIBLE);
                messageViewHolder.senderMessageText.setTextColor(Color.BLACK);
                messageViewHolder.receiverMsgTime.setVisibility(View.GONE);
                messageViewHolder.senderMessageText.setBackgroundResource(R.drawable.sent_msg);
                messageViewHolder.senderMsgTime.setText(messages.getTime() +" - "+messages.getDate());
                messageViewHolder.senderMessageText.setText(messages.getMessage());
            }
            else {
                messageViewHolder.receiverProfileImage.setVisibility(View.VISIBLE);
                messageViewHolder.receiverMessageText.setVisibility(View.VISIBLE);
                messageViewHolder.senderMsgTime.setVisibility(View.GONE);
                messageViewHolder.receiverMessageText.setTextColor(Color.BLACK);
                messageViewHolder.receiverMessageText.setBackgroundResource(R.drawable.received_msg);
                messageViewHolder.receiverMsgTime.setText(messages.getTime() +" - "+messages.getDate());
                messageViewHolder.receiverMessageText.setText(messages.getMessage());
            }
        }
    }
    @Override
    public int getItemCount()
    {
        return userMessagesList.size();
    }
}