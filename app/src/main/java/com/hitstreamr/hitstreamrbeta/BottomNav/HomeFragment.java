package com.hitstreamr.hitstreamrbeta.BottomNav;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupMenu;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.common.base.Strings;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.hitstreamr.hitstreamrbeta.Library;
import com.hitstreamr.hitstreamrbeta.NewReleaseAdapter;
import com.hitstreamr.hitstreamrbeta.NewReleases;
import com.hitstreamr.hitstreamrbeta.R;
import com.hitstreamr.hitstreamrbeta.TrendingAdapter;
import com.hitstreamr.hitstreamrbeta.TrendingVideos;
import com.hitstreamr.hitstreamrbeta.Video;
import com.hitstreamr.hitstreamrbeta.VideoPlayer;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.EventListener;

public class HomeFragment extends Fragment implements PopupMenu.OnMenuItemClickListener {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseUser current_user;
    private CollectionReference trendingNowRef = db.collection("Videos");
    private TrendingAdapter adapter;
    private RecyclerView recyclerView_Trending;
    private Button trendingMoreBtn, newReleaseBtn;
    public final String TAG = "HomePage";
    private CollectionReference newReleaseRef = db.collection("Videos");
    RequestManager glideRequests;
    private NewReleaseAdapter newReleaseadapter;
    private RecyclerView recyclerView_newRelease;
    private ArrayList<String> userGenreList;
    private ArrayList<Video> UserGenreVideos;
    private ArrayList<Video> UserNewVideos;
    private ItemClickListener mListener;
    private String CreditVal;


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        current_user = FirebaseAuth.getInstance().getCurrentUser();
        userGenreList = new ArrayList<>();
        UserGenreVideos = new ArrayList<>();
        UserNewVideos = new ArrayList<>();

        recyclerView_Trending = view.findViewById(R.id.trendingNowRCV);
        trendingMoreBtn = view.findViewById(R.id.trendingMore);
        setupRecyclerView();

        trendingMoreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent trending = new Intent(getContext(), TrendingVideos.class);
                startActivity(trending);
            }
        });

        glideRequests = Glide.with(this);
        recyclerView_newRelease = (RecyclerView) view.findViewById(R.id.freshReleasesRCV);
        newReleaseBtn = view.findViewById(R.id.newReleaseMore);
        getUserGenre();

        newReleaseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent newReleaseVideo = new Intent(getContext(), NewReleases.class);
                newReleaseVideo.putExtra("TYPE", getArguments().getString("TYPE"));
                startActivity(newReleaseVideo);
            }
        });

        FirebaseDatabase.getInstance().getReference("Credits")
                .child(current_user.getUid()).child("creditvalue")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String currentCredit = dataSnapshot.getValue(String.class);
                        if(!Strings.isNullOrEmpty(currentCredit)){

                            CreditVal = currentCredit;
                        }
                        else
                            CreditVal = "0";

                        // Log.e(TAG, "Profile credit val inside change" + CreditVal);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

        mListener = new ItemClickListener() {
            @Override
            public void onResultClick(Video video) {
                Intent videoPlayerIntent = new Intent(getActivity(), VideoPlayer.class);
                videoPlayerIntent.putExtra("TYPE", getArguments().getString("TYPE"));
                videoPlayerIntent.putExtra("VIDEO", video);
                videoPlayerIntent.putExtra("CREDIT", CreditVal);
                startActivity(videoPlayerIntent);
            }

            @Override
            public void onOverflowClick(Video title, View v) { showOverflow(v);
            }
        };
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);

    }


    private void setupRecyclerView(){
        Query query = trendingNowRef.orderBy("views", Query.Direction.DESCENDING).limit(10);

        FirestoreRecyclerOptions<Video> options = new FirestoreRecyclerOptions.Builder<Video>()
                .setQuery(query, Video.class)
                .build();

        adapter = new TrendingAdapter(options);
        recyclerView_Trending.hasFixedSize();
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        recyclerView_Trending.setLayoutManager(layoutManager);
        recyclerView_Trending.setAdapter(adapter);

    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();

    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.fave_result:
                break;
            case R.id.addLibrary_result:
                break;

        }
        return true;
    }

    public interface ItemClickListener {
        void onResultClick(Video title);
        void onOverflowClick(Video title, View v);
    }

    public void showOverflow(View v) {
        PopupMenu popupMenu = new PopupMenu(getContext(), v);
        popupMenu.setOnMenuItemClickListener(this);
        popupMenu.inflate(R.menu.video_overflow_menu);
        popupMenu.show();
    }

    public void getUserGenre(){
        FirebaseDatabase.getInstance().getReference("SelectedGenres")
                .child(current_user.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for(DataSnapshot each : dataSnapshot.getChildren()){
                                userGenreList.add(String.valueOf(each.getValue()));

                            }
                            // Log.e(TAG, "Home genre List : " + userGenreList);
                        }
                        getFreshReleases();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });

    }

    public void getFreshReleases(){

        Query queryRef = newReleaseRef.orderBy("timestamp", Query.Direction.DESCENDING);

        queryRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    if (userGenreList.size() > 0) {
                       for (int itr = 0; itr < userGenreList.size(); itr++) {
                            if (userGenreList.get(itr).contains( document.get("genre").toString().toLowerCase())) {
                                UserGenreVideos.add(document.toObject(Video.class));
                            } else if (userGenreList.get(itr).contains(document.get("subGenre").toString().toLowerCase())) {
                                UserGenreVideos.add(document.toObject(Video.class));
                            }
                        }
                    }
                    else
                    {
                        UserGenreVideos.add(document.toObject(Video.class));
                    }


                }
                callToAdapter();
            }
        });


    }

    private void callToAdapter(){

        if(UserGenreVideos.size() > 10){
            UserNewVideos = new ArrayList (UserGenreVideos.subList(0,10));
        }
        else
        {
            UserNewVideos = new ArrayList (UserGenreVideos);
        }

        recyclerView_newRelease.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        newReleaseadapter = new NewReleaseAdapter( R.layout.thumbnail_categoried_video, UserNewVideos, mListener, glideRequests);
        newReleaseadapter.notifyDataSetChanged();
        recyclerView_newRelease.setAdapter(newReleaseadapter);

    }

}
