package com.hitstreamr.hitstreamrbeta;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import de.hdodenhof.circleimageview.CircleImageView;

public class Profile extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "Profile";

    private FirebaseUser current_user;
    private CircleImageView circleImageView;
    private String accountType;

    private Toolbar toolbar;

    private Button mfollowBtn;
    private Button mUnfollowBtn;

    private TextView mfollowers;
    private TextView mfollowing;

    private long followerscount = 0;
    private long followingcount = 0;

    private String userClicked;
    private String userUserID;

    FirebaseDatabase database;
    DatabaseReference myRef;
    DatabaseReference myNewRef;
    DatabaseReference myFollowersRef, myFollowingRef;
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference mStorageRef = storage.getReference();

    Query queryFollowersCount, queryFollowingCount;

    Uri profilePictureDownloadUrl;

    /**
     * Set up and initialize layouts and variables
     *
     * @param savedInstanceState state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        current_user = FirebaseAuth.getInstance().getCurrentUser();

        mfollowBtn = findViewById(R.id.followUser);
        mUnfollowBtn = findViewById(R.id.unFollowUser);

        mfollowers = findViewById(R.id.usersFollowers);
        mfollowing = findViewById(R.id.usersFollowing);

        mUnfollowBtn.setVisibility(View.GONE);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        toolbar.setTitleTextColor(0xFFFFFFFF);

        database = FirebaseDatabase.getInstance();
        //myRef.setValue("Hello, World!");

        mfollowBtn.setOnClickListener(this);
        mUnfollowBtn.setOnClickListener(this);

        getUserType();
        getUsername();

        if (userClicked.equals("")) {
            queryFollowersCount = FirebaseDatabase.getInstance().getReference().child("Following")
                    .child(current_user.getUid());
            queryFollowingCount = FirebaseDatabase.getInstance().getReference().child("Following")
                    .child(current_user.getUid());


            /*myFollowersRef = FirebaseDatabase.getInstance().getReference().child("Following")
                    .child(current_user.getUid());
            myFollowingRef = FirebaseDatabase.getInstance().getReference().child("Following")
                    .child(current_user.getUid());*/
            getCurrentProfile();
        } else {
            //getSearchUser();
            getUserClickedUserId();
        }

        /*getFollowersCount();
        getFollowingCount();*/

    }

    private void getCurrentProfile() {
        mfollowBtn.setVisibility(View.GONE);
        myRef = FirebaseDatabase.getInstance().getReference().child(accountType)
                .child(current_user.getUid());
        ValueEventListener eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String username = dataSnapshot.child("username").getValue(String.class);
                getSupportActionBar().setTitle(username);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        myRef.addListenerForSingleValueEvent(eventListener);

        // Profile Picture
        if (current_user.getPhotoUrl() != null) {
            circleImageView = toolbar.getRootView().findViewById(R.id.profilePictureToolbar);
            circleImageView.setVisibility(View.VISIBLE);
            CircleImageView profileImageView = findViewById(R.id.profileImage);
            Uri photoURL = current_user.getPhotoUrl();
            Glide.with(getApplicationContext()).load(photoURL).into(circleImageView);
            Glide.with(getApplicationContext()).load(photoURL).into(profileImageView);
            getFollowingCount();
            getFollowersCount();
        }
    }

    private void getSearchProfile() {
        Log.e(TAG, "Entered searchprofile");
        myRef = FirebaseDatabase.getInstance().getReference().child(accountType)
                .child(userUserID);
        ValueEventListener eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String username = dataSnapshot.child("username").getValue(String.class);
                getSupportActionBar().setTitle(username);
                Log.e(TAG, "Got username :: " + username);
                getUrlStorage();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        myRef.addListenerForSingleValueEvent(eventListener);
    }

    private void getUrlStorage() {
        Log.e(TAG, "Entered storage");
        mStorageRef.child("profilePictures").child(userUserID).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                // Got the download URL for 'users/me/profile.png'
                profilePictureDownloadUrl = uri;
                Log.e(TAG, "profile picture uri::" + profilePictureDownloadUrl);
                if (profilePictureDownloadUrl != null) {
                    circleImageView = toolbar.getRootView().findViewById(R.id.profilePictureToolbar);
                    circleImageView.setVisibility(View.VISIBLE);
                    CircleImageView profileImageView = findViewById(R.id.profileImage);
                    //Uri photoURL = current_user.getPhotoUrl();
                    Glide.with(getApplicationContext()).load(profilePictureDownloadUrl).into(circleImageView);
                    Glide.with(getApplicationContext()).load(profilePictureDownloadUrl).into(profileImageView);
                    getFollowersCount();
                    getFollowingCount();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
            }
        });
    }

    /**
     * Get the account type of the current user
     */
    private void getUserType() {
        Bundle extras = getIntent().getExtras();

        if (extras.containsKey("TYPE") && getIntent().getStringExtra("TYPE") != null) {
            //type = getIntent().getStringExtra("TYPE");

            if (getIntent().getStringExtra("TYPE").equals(getString(R.string.type_basic))) {
                accountType = "BasicAccounts";
            } else if (getIntent().getStringExtra("TYPE").equals(getString(R.string.type_artist))) {
                // sth
                accountType = "ArtistAccounts";
            } else {
                accountType = "LabelAccounts";
            }
        }
    }

    private void getUsername() {
        Bundle extras = getIntent().getExtras();

        if (extras.containsKey("artistUsername") && getIntent().getStringExtra("artistUsername") != null) {
            userClicked = getIntent().getStringExtra("artistUsername");
            Log.e(TAG, "username clicked is:::" + userClicked);
        } else if (extras.containsKey("basicUsername") && getIntent().getStringExtra("basicUsername") != null) {
            userClicked = getIntent().getStringExtra("basicUsername");
        } else {
            userClicked = "";
        }
    }


    private void getUserClickedUserId() {
        myNewRef = FirebaseDatabase.getInstance().getReference().child("UsernameUserId").child(userClicked);
        myNewRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userUserID = dataSnapshot.child("tempUserId").getValue(String.class);
                Log.e(TAG, "userid is :::" + userUserID);
                Log.e(TAG, "data snapshot values :::" + dataSnapshot);

                if (userUserID.equals(current_user.getUid())) {
                    mfollowBtn.setVisibility(View.GONE);
                }
                /*myFollowersRef = FirebaseDatabase.getInstance().getReference().child("Following")
                        .child(userUserID);
                myFollowingRef = FirebaseDatabase.getInstance().getReference().child("Following")
                        .child(userUserID);*/

                queryFollowersCount = FirebaseDatabase.getInstance().getReference().child("Following")
                        .child(userUserID);
                queryFollowingCount = FirebaseDatabase.getInstance().getReference().child("Following")
                        .child(userUserID);

                getSearchProfile();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
        //myNewRef.addListenerForSingleValueEvent(eventListener);
    }

    private void addFollowers() {

        FirebaseDatabase.getInstance().getReference()
                .child("following")
                .child(current_user.getUid())
                .child(userUserID)
                .child("userid")
                .setValue(userUserID);

        FirebaseDatabase.getInstance().getReference()
                .child("followers")
                .child(userUserID)
                .child(current_user.getUid())
                .child("userid")
                .setValue(current_user.getUid());

        setFollowing();
    }

    private void cancelFollowers() {

        FirebaseDatabase.getInstance().getReference()
                .child("following")
                .child(current_user.getUid())
                .child(userUserID)
                .removeValue();

        FirebaseDatabase.getInstance().getReference()
                .child("followers")
                .child(userUserID)
                .child(current_user.getUid())
                .removeValue();

        setUnFollowing();
    }

    private void setFollowing() {
        mfollowBtn.setVisibility(View.GONE);
        mUnfollowBtn.setVisibility(View.VISIBLE);
    }

    private void setUnFollowing() {
        mfollowBtn.setVisibility(View.VISIBLE);
        mUnfollowBtn.setVisibility(View.GONE);
    }

    private void getFollowersCount() {
        Log.e(TAG, "entered getFollowers count");
        followerscount = 0;
        queryFollowersCount.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot singlesnapshot : dataSnapshot.getChildren()) {
                    followerscount = dataSnapshot.getChildrenCount();
                    followerscount++;
                }
                mfollowers.setText(String.valueOf(followingcount));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void getFollowingCount() {
        followingcount = 0;
        queryFollowingCount.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.e(TAG, "Following datasnapshot:" + dataSnapshot);
                Log.e(TAG, "Following datasnapshot children:" + dataSnapshot.getChildren());

                for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Log.e(TAG, "1st iteration");
                    followingcount = dataSnapshot.getChildrenCount();
                    followerscount++;
                }
                mfollowing.setText(String.valueOf(followingcount));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    /**
     * Handles back button on toolbar
     *
     * @return true if pressed
     */
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onClick(View view) {
        if (view == mfollowBtn) {
            addFollowers();
        }
        if (view == mUnfollowBtn) {
            cancelFollowers();
        }
    }
}
