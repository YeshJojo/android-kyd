package com.example.map;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
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

import de.hdodenhof.circleimageview.CircleImageView;

public class ContactsFragment extends Fragment
{
    private View ContactsView;
    private RecyclerView myContactsList;

    private DatabaseReference ContacsRef, UsersRef;
    private FirebaseAuth mAuth;
    private String currentUserID;
    Button find;


    public ContactsFragment() { }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ContactsView = inflater.inflate(R.layout.fragment_contacts, container, false);

        myContactsList = ContactsView.findViewById(R.id.contacts_list);
        find = ContactsView.findViewById(R.id.findfriends);
        myContactsList.setLayoutManager(new LinearLayoutManager(getContext()));

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();

        ContacsRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserID);
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        find.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(),FindFriendsActivity.class));
            }
        });
        return ContactsView;
    }


    @Override
    public void onStart()
    {
        super.onStart();
        FirebaseRecyclerOptions options =
                new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(ContacsRef, Contacts.class)
                .build();
        FirebaseRecyclerAdapter<Contacts, ContactsViewHolder> adapter
                = new FirebaseRecyclerAdapter<Contacts, ContactsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ContactsViewHolder holder, final int position, @NonNull Contacts model) {
                final String userIDs = getRef(position).getKey();
                UsersRef.child(userIDs).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            if (dataSnapshot.child("userState").hasChild("state")) {
                                String state = dataSnapshot.child("userState").child("state").getValue().toString();

                                if (state.equals("online")) {
                                    holder.onlineIcon.setVisibility(View.VISIBLE);
                                }
                                else if (state.equals("offline")) {
                                    holder.onlineIcon.setVisibility(View.INVISIBLE);
                                }
                            } else {
                                holder.onlineIcon.setVisibility(View.INVISIBLE);
                            }

                            if (dataSnapshot.hasChild("image")) {
                                String userImage = dataSnapshot.child("image").getValue().toString();
                                String userId = dataSnapshot.child("uid").getValue().toString();
                                String profileName = dataSnapshot.child("name").getValue().toString();
                                String profileStatus = dataSnapshot.child("group").getValue().toString();

                                holder.userName.setText(profileName);
                                holder.userStatus.setText(profileStatus);
                                final StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Profile Images");
                                storageReference.child(userId+".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        Log.d("UriImageChat",uri.toString());
                                        Picasso.get().load(uri).into(holder.profileImage);

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception exception) {
                                        // Handle any errors
                                    }
                                });
                                Picasso.get().load(userImage).placeholder(R.drawable.ic_boy).into(holder.profileImage);
                            }
                            else {
                                String profileName = dataSnapshot.child("name").getValue().toString();
                                String profileStatus = dataSnapshot.child("group").getValue().toString();
                                holder.userName.setText(profileName);
                                holder.userStatus.setText(profileStatus);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view)
                    {
                        String visit_user_id = getRef(position).getKey();
                        Log.d("Key: ",visit_user_id);
                        Intent profileIntent = new Intent(getActivity(), ProfileActivity.class);
                        profileIntent.putExtra("visit_user_id", visit_user_id);
                        profileIntent.putExtra("fromContact", 1);
                        startActivity(profileIntent);
                    }
                });
            }
            @NonNull
            @Override
            public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.users_display_layout, viewGroup, false);
                ContactsViewHolder viewHolder = new ContactsViewHolder(view);
                return viewHolder;
            }
        };
        myContactsList.setAdapter(adapter);
        adapter.startListening();
    }
    public static class ContactsViewHolder extends RecyclerView.ViewHolder
    {
        TextView userName, userStatus;
        CircleImageView profileImage;
        ImageView onlineIcon;
        public ContactsViewHolder(@NonNull View itemView)
        {
            super(itemView);

            userName = itemView.findViewById(R.id.user_profile_name);
            userStatus = itemView.findViewById(R.id.user_status);
            profileImage = itemView.findViewById(R.id.users_profile_image);
            onlineIcon = itemView.findViewById(R.id.user_online_status);
        }
    }
}
