package com.gilliswerrebrouck.messagingapp.view;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.gilliswerrebrouck.messagingapp.R;
import com.gilliswerrebrouck.messagingapp.model.User;
import com.gilliswerrebrouck.messagingapp.utils.FirebaseUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class NewMessageActivity extends AppCompatActivity {

    @BindView(R.id.users)
    RecyclerView usersRecyclerView;
    @BindView(R.id.no_users)
    TextView noUsers;

    private UsersRecycleViewAdapter adapter;

    private List<String> uidArr = new ArrayList<>();
    private List<String> userStatusArr = new ArrayList<>();
    private List<String> usernameArr = new ArrayList<>();

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private User user = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_message);
        setTitle("Users");

        ButterKnife.bind(this);

        firebaseAuth = FirebaseAuth.getInstance();

        adapter = new UsersRecycleViewAdapter(uidArr, userStatusArr, usernameArr);
        usersRecyclerView.setAdapter(adapter);

        DividerItemDecoration decoration = new DividerItemDecoration(usersRecyclerView.getContext(), DividerItemDecoration.VERTICAL);
        usersRecyclerView.addItemDecoration(decoration);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        usersRecyclerView.setLayoutManager(layoutManager);
        usersRecyclerView.setItemAnimator(new DefaultItemAnimator());

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() == null) {
                    Intent login = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(login);
                    finish();
                }
            }
        };

        user = new User(firebaseAuth.getCurrentUser());

        userListeners();
    }

    private void userListeners() {
        FirebaseUtils.getUsersRef().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                uidArr.clear();
                userStatusArr.clear();
                usernameArr.clear();

                Iterator usersIterator = dataSnapshot.getChildren().iterator();

                while(usersIterator.hasNext()){
                    DataSnapshot dsUser = (DataSnapshot) usersIterator.next();

                    if(!user.getUid().equals(dsUser.getKey())) {
                        uidArr.add(uidArr.size(), dsUser.getKey());
                        usernameArr.add(usernameArr.size(), dsUser.child("username").getValue().toString());
                        userStatusArr.add(userStatusArr.size(), dsUser.child("status").getValue().toString());

                        adapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    // make a row for a message
    class UserViewHolder extends RecyclerView.ViewHolder {
        ImageView userImage;
        TextView username;
        TextView userStatus;
        TextView userStatusBack;

        public UserViewHolder(View row) {
            super(row);

            userImage = (ImageView) row.findViewById(R.id.userImage);
            username = (TextView) row.findViewById(R.id.username);
            userStatus = (TextView) row.findViewById(R.id.userStatus);
            userStatusBack = (TextView) row.findViewById(R.id.userStatusBack);

            row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();

                    Intent message = new Intent(getApplicationContext(), MessageActivity.class);
                    message.putExtra("uid", uidArr.get(position));
                    startActivity(message);
                    finish();
                }
            });
        }
    }

    // implement MessageViewHolder
    class UsersRecycleViewAdapter extends RecyclerView.Adapter<UserViewHolder> {
        private List<String> uidArr = new ArrayList<String>();
        private List<String> userStatusArr = new ArrayList<String>();
        private List<String> usernameArr = new ArrayList<String>();

        public UsersRecycleViewAdapter(List<String> uidArr, List<String> userStatusArr, List<String> usernameArr) {
            this.uidArr = uidArr;
            this.userStatusArr = userStatusArr;
            this.usernameArr = usernameArr;
        }

        @Override
        public UserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View viewRow = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_user, parent, false);
            UserViewHolder userViewHolder = new UserViewHolder(viewRow);

            return userViewHolder;
        }

        @Override
        public void onBindViewHolder(UserViewHolder holder, int position) {
            if (!uidArr.isEmpty() && uidArr.size() > position
                    &&!userStatusArr.isEmpty() && userStatusArr.size() > position
                    && !usernameArr.isEmpty() && usernameArr.size() > position) {

                if(userStatusArr.get(position).equals("online"))
                    holder.userStatus.getBackground().setTint(getColor(R.color.userStatusOnline));
                else if (userStatusArr.get(position).equals("offline"))
                    holder.userStatus.getBackground().setTint(getColor(R.color.userStatusOffline));
                else
                    holder.userStatus.getBackground().setTint(getColor(R.color.userStatusUnknown));

                holder.userStatusBack.getBackground().setTint(getColor(R.color.background));

                holder.username.setText(usernameArr.get(position));
            }

            if(uidArr.isEmpty()){
                noUsers.setVisibility(View.VISIBLE);
            } else {
                noUsers.setVisibility(View.GONE);
            }
        }

        @Override
        public int getItemCount() {
            return uidArr.size();
        }
    }
}