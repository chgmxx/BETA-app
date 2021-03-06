package com.hitstreamr.hitstreamrbeta;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import de.hdodenhof.circleimageview.CircleImageView;
public class VideoUploadActivity extends AppCompatActivity implements View.OnClickListener, contributorAdapter.deleteinterface, VideoUploadCallback{

    private static final String TAG = "MyVideoUploadActivity";

    private static final String VIDEO_TITLE = "title";
    private static final String VIDEO_DESCRIPTION = "description";
    private static final String VIDEO_GENRE = "genre";
    private static final String VIDEO_SUBGENRE = "subGenre";
    private static final String VIDEO_PRIVACY = "privacy";
    private static final String VIDEO_DOWNLOAD_LINK = "url";
    private static final String THUMBNAIL_DOWNLOAD_LINK = "thumbnailUrl";
    private static final String VIDEO_PUB_YEAR = "pubYear";
    private static final String VIDEO_TIME_STAMP = "timestamp";
    private static final String VIDEO_ID = "videoId";
    private static final String VIDEO_VIEWS = "views";
    private static final String USER_ID = "userId";
//    private static final String USER_NAME = "username";
    private static final String VIDEO_CONTRIBUTOR = "contributors";
    private static final String VIDEO_DURATION = "duration";
    private static final String VIDEO_DELETE = "delete";

    private static final String VIDEO_CONTRIBUTOR_NAME = "contributorName";
    private static final String VIDEO_CONTRIBUTOR_PERCENTAGE = "percentage";
    private static final String VIDEO_CONTRIBUTOR_TYPE = "type";
    private static final String VIDEO_CONTRIBUTOR_USERID = "contributorUserId";

    private static final String VIDEO_TITLE_TERMS = "terms";



    //Video View
    private VideoView artistUploadVideo;

    //Buttons
    private Button uploadBtn;
    private Button selectVideoBtn;
    private Button selectThumbBtn;
    private Button contributeBtn;
    private Button addContributorBtn;
    private Button retryUploadBtn;
    private Button ContributorCancelBtn;
    private ImageButton Help;
    private TextView preview;

    //EditText Inputs
    private EditText EdittextTitle;
    private EditText EditTextDescription;

    //private EditText EdittextContributorName;
    private AutoCompleteTextView EdittextContributorName;
    private EditText EdittextContributorPercentage;

    //Spinner Inputs
    private Spinner SpinnerGenre;
    private Spinner SpinnerSubGenre;
    private Spinner SpinnerPrivacy;
    private Spinner SpinnerContributorType;

    //Layout
    private LinearLayout addContributorLayout;
    private LinearLayout videoLayout;
    private LinearLayout videoCancelLayout;

    //Data Lists
    ArrayList<String> contributorPercentageList;
    ArrayList<String> contributorFirestoreList;
    public ArrayList<Contributor> contributorList;
    public contributorAdapter contributorAdapter;
    Map<String, Object> contributorVideo;
    Map<String, Object> artistVideo;

    //List View
    private ListView ContributorValuesLV;

    private static final int VID_REQUEST_CODE = 1234;

    private Uri selectedVideoPath = null;
    private long duration;
    private boolean ContributorSuccess = false;

    private boolean startedUpload;

    private AtomicBoolean videoSelected;

    //Firestore Database
    private FirebaseFirestore db;
    DatabaseReference database;
    FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();

    int i, sum = 0, tempvalue = 0;
    String temp;
    private Intent serviceIntent;

    private ServiceConnection mConnection;
    private VideoUploadService mService;
    private boolean mBound;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_upload);

        // Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle("Upload");
        toolbar.setTitleTextColor(0xFFFFFFFF);

        currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        // Profile Picture
        if (currentFirebaseUser.getPhotoUrl() != null) {
            CircleImageView circleImageView = toolbar.getRootView().findViewById(R.id.profilePictureToolbar);
            circleImageView.setVisibility(View.VISIBLE);
            Uri photoURL = currentFirebaseUser.getPhotoUrl();
            Glide.with(getApplicationContext()).load(photoURL).into(circleImageView);
        }

        //Video Duration
        duration = 0;

        //FireStore Database
        db = FirebaseFirestore.getInstance();

        //Boolean Status Checks
        videoSelected = new AtomicBoolean(false);

        //Layout
        addContributorLayout = findViewById(R.id.add_contributor_layout);
        videoLayout = findViewById(R.id.videoUploadScreenLayout);
        videoCancelLayout = findViewById(R.id.cancelLayout);

        //Buttons
        uploadBtn = findViewById(R.id.uploadVideo);
        selectVideoBtn = findViewById(R.id.selectVideo);
        contributeBtn = findViewById(R.id.ContributorBtn);
        addContributorBtn = findViewById(R.id.AddContributorButton);
        retryUploadBtn = findViewById(R.id.retryVideoUpload);
        ContributorCancelBtn =findViewById(R.id.ContributorCancel);
        Help = (ImageButton) findViewById(R.id.help);
        preview = findViewById(R.id.tapPreview);

        //VideoView
        artistUploadVideo = findViewById(R.id.videoView);

        //Edittext
        EdittextTitle = findViewById(R.id.Title);
        EdittextTitle.setInputType(android.text.InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        EditTextDescription = findViewById(R.id.Description);
        EditTextDescription.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
       // EdittextContributorName = findViewById(R.id.ContributorName);
        EdittextContributorPercentage = findViewById(R.id.ContributorPercentage);


        //Spinner
        SpinnerGenre = findViewById(R.id.Genre);
        SpinnerSubGenre = findViewById(R.id.SubGenre);
        SpinnerPrivacy = findViewById(R.id.Privacy);
        SpinnerContributorType = findViewById(R.id.ContributorTypeSpinner);

        //List View
        ContributorValuesLV = findViewById(R.id.ContributorListView);

        //Data Lists
        contributorPercentageList = new ArrayList<>();
        contributorFirestoreList = new ArrayList<>();
        contributorList = new ArrayList<>();
        contributorVideo = new HashMap<>();
        artistVideo = new HashMap<>();

        //visibility
        addContributorLayout.setVisibility(View.GONE);
        videoCancelLayout.setVisibility(View.GONE);

        //Listeners
        uploadBtn.setOnClickListener(this);
        selectVideoBtn.setOnClickListener(this);
        contributeBtn.setOnClickListener(this);
        addContributorBtn.setOnClickListener(this);
        retryUploadBtn.setOnClickListener(this);
        ContributorCancelBtn.setOnClickListener(this);
        Help.setOnClickListener(this);
        preview.setOnClickListener(this);

        previewVid();


        ContributorValuesLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                contributorList.remove(position);
                contributorAdapter.notifyDataSetChanged();
            }
        });

        database = FirebaseDatabase.getInstance().getReference();

        final ArrayAdapter<String> autoComplete = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1);
        database.child("ArtistAccounts").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                autoComplete.clear();
                for (DataSnapshot suggestionSnapshot : dataSnapshot.getChildren()){
                    String username = suggestionSnapshot.child("username").getValue(String.class);
//                    //Log.e(TAG,"username --"+username);
                    //Add the retrieved string to the list
                    autoComplete.add(username);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        EdittextContributorName= (AutoCompleteTextView)findViewById(R.id.ContributorName);
        EdittextContributorName.setAdapter(autoComplete);
        autoComplete.notifyDataSetChanged();

        EdittextContributorName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(!b) {
                    // on focus off
                    String str = EdittextContributorName.getText().toString();

                    ListAdapter listAdapter = EdittextContributorName.getAdapter();
                    for(int i = 0; i < listAdapter.getCount(); i++) {
                        String temp = listAdapter.getItem(i).toString();
                        if(str.compareTo(temp) == 0) {
                            return;
                        }
                    }

                    EdittextContributorName.setText("");
                    Toast.makeText(VideoUploadActivity.this, "Please choose Contributor name from the suggested list", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //Video Upload Service
        startConnection();
        startVideoUploadService();

        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        boolean secondStart = prefs.getBoolean("secondStart", true);

        if (secondStart) {
            tutorialUpload();
        }

        /* Transloadit Credentials */
        //DELETE ME
        //transloadit = new AndroidTransloadit(BuildConfig.Transloadit_API_KEY, BuildConfig.Transloadit_API_SECRET);
    }

    //First Start upload tutorial
    private void tutorialUpload() {
        TapTargetView.showFor(this,
                TapTarget.forView(findViewById(R.id.selectVideo), "Upload Here", "Get start by selecting your music video. Must be MP4 format.")
                        .tintTarget(false)
                        .cancelable(false)
                        .descriptionTextColor(R.color.colorWhite)
                        .outerCircleColor(R.color.colorPrimary),
                new TapTargetView.Listener() {          // The listener can listen for regular clicks, long clicks or cancels
                    @Override
                    public void onTargetClick(TapTargetView view) {
                        super.onTargetClick(view);      // This call is optional
                        next1();

                        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putBoolean("secondStart", false);
                        editor.apply();
                    }

                });

    }

    private void next1() {
        TapTargetView.showFor(this,                 // `this` is an Activity
                TapTarget.forView(findViewById(R.id.Title), "Title / Description", "Add the title and description of your videos here!")
                        .tintTarget(true)
                        .cancelable(false)
                        .descriptionTextColor(R.color.colorWhite)
                        .outerCircleColor(R.color.colorPrimary),
                new TapTargetView.Listener() {          // The listener can listen for regular clicks, long clicks or cancels
                    @Override
                    public void onTargetClick(TapTargetView view) {
                        super.onTargetClick(view);      // This call is optional
                        next2();
                    }
                });
    }

    private void next2() {
        TapTargetView.showFor(this,                 // `this` is an Activity
                TapTarget.forView(findViewById(R.id.Genre), "Genre", "Categorize your music by selecting the Genre and Sub-genre here!")
                        .tintTarget(true)
                        .cancelable(false)
                        .descriptionTextColor(R.color.colorWhite)
                        .outerCircleColor(R.color.colorPrimary),
                new TapTargetView.Listener() {          // The listener can listen for regular clicks, long clicks or cancels
                    @Override
                    public void onTargetClick(TapTargetView view) {
                        super.onTargetClick(view);      // This call is optional
                        next3();
                    }
                });
    }

    private void next3() {
        TapTargetView.showFor(this,                 // `this` is an Activity
                TapTarget.forView(findViewById(R.id.ContributorBtn), "Contributors", "All any collaberators here. We'll pay them on your behalf!")
                        .tintTarget(false)
                        .cancelable(false)
                        .descriptionTextColor(R.color.colorWhite)
                        .outerCircleColor(R.color.colorPrimary),
                new TapTargetView.Listener() {          // The listener can listen for regular clicks, long clicks or cancels
                    @Override
                    public void onTargetClick(TapTargetView view) {
                        super.onTargetClick(view);      // This call is optional
                        next4();
                    }
                });
    }

    private void next4() {
        TapTargetView.showFor(this,                 // `this` is an Activity
                TapTarget.forView(findViewById(R.id.Privacy), "Privacy Settings", "Go public orkeep it private. The choice is yours!")
                        .tintTarget(true)
                        .cancelable(false)
                        .descriptionTextColor(R.color.colorWhite)
                        .outerCircleColor(R.color.colorPrimary),
                new TapTargetView.Listener() {          // The listener can listen for regular clicks, long clicks or cancels
                    @Override
                    public void onTargetClick(TapTargetView view) {
                        super.onTargetClick(view);      // This call is optional
                        next5();
                    }
                });
    }

    private void next5() {
        TapTargetView.showFor(this,                 // `this` is an Activity
                TapTarget.forView(findViewById(R.id.uploadVideo), "Upload", "Finally, upload the button and go live! That's it, now you ready to upload!")
                        .tintTarget(false)
                        .descriptionTextColor(R.color.colorWhite)
                        .outerCircleColor(R.color.colorPrimary),
                new TapTargetView.Listener() {          // The listener can listen for regular clicks, long clicks or cancels
                    @Override
                    public void onTargetClick(TapTargetView view) {
                        super.onTargetClick(view);      // This call is optional

                    }
                });
    }

    private void previewVid(){

    }

    /*
     * Select Video and Callback from Video
     */
    private void selectVideo() {
        videoSelected.set(false);
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT, android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        intent.setType("video/mp4");
        startActivityForResult(Intent.createChooser(intent, "Select your video"), VID_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == VID_REQUEST_CODE) {
                if (data != null) {
                    if (data.getData() != null) {
                        selectedVideoPath = data.getData();
                        try {

                            selectVideoBtn.setBackgroundColor(0xFF00DDFF);
                            selectVideoBtn.setText(R.string.video_selection);
                            selectVideoBtn.setTextColor(Color.WHITE);
                            String path = data.getData().toString();
                            artistUploadVideo.setVideoPath(path);
                            artistUploadVideo.requestFocus();
                            artistUploadVideo.pause();
                            artistUploadVideo.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                @Override
                                public void onPrepared(MediaPlayer mp) {
                                    //prepare to get duration when ready
                                    duration = artistUploadVideo.getDuration();
                                    //Log.e(TAG, duration+"");
                                    //Log.e(TAG, millisecondsToString(duration));
                                }
                            });
                            // Video has been selected succesfully
                            videoSelected.set(true);
                            //set visibility on text
                            preview.setVisibility(View.VISIBLE);


                        } catch (Exception e) {
                            //#debug
                            e.printStackTrace();
                        }
                    } else {
                        selectVideoBtn.setBackgroundColor(Color.RED);
                        selectVideoBtn.setText(R.string.video_not_selection);
                        // Video has not  been selected succesfully
                        videoSelected.set(false);
                    }
                }
            }
        }
    }

    private void startConnection(){
        /* Video Player Service */
        mConnection = new ServiceConnection() {

            @Override
            public void onServiceConnected(ComponentName className,
                                           IBinder service) {
                // We've bound to LocalService, cast the IBinder and get LocalService instance
                VideoUploadService.LocalBinder binder = (VideoUploadService.LocalBinder) service;
                mService = binder.getService();
                mService.setCallbacks(VideoUploadActivity.this);
                //DELETE ME
                //assembly = transloadit.newAssembly(mService, VideoUploadActivity.this);
                //set template Id
                //assembly.addOption("template_id", BuildConfig.Transloadit_TEMPLATE_ID);
            }

            @Override
            public void onServiceDisconnected(ComponentName arg0) {
                mService = null;
            }
        };
    }

    public void startVideoUploadService(){
        serviceIntent = new Intent(this, VideoUploadService.class);
        startService(serviceIntent);
        (new Handler()).postDelayed(this::binder, 500);
    }

    @Override
    public void unbindUploadService(){
        if(mBound){
            mService.setCallbacks(null);
            unbindService(mConnection);
            mBound = false;
            mService = null;
            mConnection = null;
        }
    }

    @Override
    public void goBack(){
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(!startedUpload){
            if (mService != null) {
                mService.deleteBeforeWork();
            }
        }else{
            unbindUploadService();
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if(!startedUpload){
            if (mService != null) {
                mService.deleteBeforeWork();
            }
        }else{
            unbindUploadService();
        }


    }

    private void binder(){
        //Log.d(TAG, "Bind Service");
        if (bindService(serviceIntent,mConnection, 0)) {
            mBound = true;
        } else {
            Log.e(TAG, "Error: The requested service doesn't " +
                    "exist, or this client isn't allowed access to it.");
        }
    }

    private ArrayList<String> processTitle(String title){
        // ArrayList of characters to remove
        ArrayList<String> remove = new ArrayList<>();
        remove.add(" ");

        ArrayList<String> tmp = new ArrayList<>(Arrays.asList(title.trim().toLowerCase().split("\\s+")));
        tmp.removeAll(remove);

        return tmp;
    }

    private void registerVideo() {
        final String title = EdittextTitle.getText().toString().trim();
        final String description = EditTextDescription.getText().toString().trim();

        if (!validateTitle(title) || !validateDescription(description) || !validateBrowseVideo() || !validateSumPercentage()
                || !validateGenre() || !validateSubGenre()) {
            return;
        } else {
            Log.e("tag", "Enter register video");
            // videoLayout.setVisibility(View.GONE);
            // videoCancelLayout.setVisibility(View.VISIBLE);
            // TextViewVideoFilename.setText(title);
            makeArtistVideoMap();
            if(mService != null){
                mService.setTitle(title);
                mService.setMap(artistVideo);
            }

            startedUpload = true;
            final String storagetitle = EdittextTitle.getText().toString().trim();
            mService.uploadVideo(selectedVideoPath, storagetitle);
        }
    }

    private void makeArtistVideoMap(){
        final String title = EdittextTitle.getText().toString().trim();
        final String description = EditTextDescription.getText().toString().trim();
        final String genre = SpinnerGenre.getSelectedItem().toString().trim();
        final String subGenre = SpinnerSubGenre.getSelectedItem().toString().trim();
        final String privacy = SpinnerPrivacy.getSelectedItem().toString().trim();
        final String CurrentUserID = currentFirebaseUser.getUid();
        final int pubYear = Integer.valueOf(Calendar.getInstance().get(Calendar.YEAR));

        ArrayList<Object> sample = new ArrayList<>();

        //Map<String, Object> sample = new HashMap<>();
        for(i=0; i<contributorFirestoreList.size(); i++) {

            String temp = contributorFirestoreList.get(i);

            String[] tempArray = temp.split(",");

            Map<String, Object> Contributor = new HashMap<>();
            Contributor.put(VIDEO_CONTRIBUTOR_NAME, tempArray[0]);
            Contributor.put(VIDEO_CONTRIBUTOR_PERCENTAGE, tempArray[1]);
            Contributor.put(VIDEO_CONTRIBUTOR_TYPE, tempArray[2]);
            Contributor.put(VIDEO_CONTRIBUTOR_USERID, tempArray[3]);
            sample.add(Contributor);
        }

        artistVideo.put(VIDEO_TITLE, title);
        artistVideo.put(VIDEO_DESCRIPTION, description);
        artistVideo.put(VIDEO_GENRE, genre);
        artistVideo.put(VIDEO_SUBGENRE, subGenre);
        artistVideo.put(VIDEO_PRIVACY, privacy);
        artistVideo.put(VIDEO_DOWNLOAD_LINK, "");
        artistVideo.put(THUMBNAIL_DOWNLOAD_LINK, "");
        artistVideo.put(VIDEO_PUB_YEAR, pubYear);
        artistVideo.put(VIDEO_CONTRIBUTOR, sample);
        artistVideo.put(USER_ID, CurrentUserID);
//        artistVideo.put(USER_NAME, currentFirebaseUser.getDisplayName());
        artistVideo.put(VIDEO_DURATION,millisecondsToString(duration));
        artistVideo.put(VIDEO_TIME_STAMP, null);
        artistVideo.put(VIDEO_ID,null);
        artistVideo.put(VIDEO_VIEWS,0l);
        artistVideo.put(VIDEO_DELETE,"N");


        Map<String, Boolean> terms = new HashMap<>();
        ArrayList<String> res = processTitle(title);
        for(int i = 0; i < res.size(); i++){
            terms.put(res.get(i), true);
        }
        artistVideo.put(VIDEO_TITLE_TERMS,terms);
    }

    private boolean validateBrowseVideo() {
        if (selectedVideoPath != null) {
            return true;
        }
        selectVideoBtn.setBackgroundColor(Color.RED);
        selectVideoBtn.setText(R.string.video_not_selection);
        return false;
    }

    private boolean validateTitle(String title) {
        if (title.isEmpty()) {
            EdittextTitle.setError("Field can't be empty");
            return false;
        } else if (title.length() >= 100) {
            EdittextTitle.setError("Title length can't exceed 100 characters");
        } else if (!(checkAlphaNumeric(title))) {
                EdittextTitle.setError("Title contains unaccepted characters");
                return false;
        } else {
            EdittextTitle.setError(null);
            return true;
        }
        return true;
    }

    private boolean validateDescription(String description) {
        if (description.isEmpty()) {
            EditTextDescription.setError("Field can't be empty");
            return false;
        } else if (description.length() >= 1000) {
            EditTextDescription.setError("Description length can't exceed 1000 characters");
        } else if (!(checkAlphaNumeric(description))) {
            EditTextDescription.setError("Description contains unaccepted characters");
            return false;
        } else {
            EditTextDescription.setError(null);
            return true;
        }
        return true;
    }

    private boolean validateGenre() {
        if (SpinnerGenre.getSelectedItem().toString().trim().equals("--Select--")) {
            Toast.makeText(this, "Genre is not selected", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private boolean validateSubGenre() {
        if (SpinnerSubGenre.getSelectedItem().toString().trim().equals("--Select--")) {
            Toast.makeText(this, "SubGenre is not selected", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    /**
     * Method to validate the Street Address of any unwanted characters
     */
    public boolean checkAlphaNumeric(String s) {

        String AlphaNumeric = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz 1234567890!@#$%^&*()_+-=[]{}|;':',./<>?";
        boolean[] value_for_each_comparison = new boolean[s.length()];

        for (int i = 0; i < s.length(); i++) {
            for (int count = 0; count < AlphaNumeric.length(); count++) {
                if (s.charAt(i) == AlphaNumeric.charAt(count)) {
                    value_for_each_comparison[i] = true;
                    break;
                } else {
                    value_for_each_comparison[i] = false;
                }
            }
        }
        return checkStringCmpvalues(value_for_each_comparison);
    }

    /**
     * Method to support the above Method named checkAlphabet in accomplishing the task
     * to validate the input String for Alphabetical characters only.
     */
    private boolean checkStringCmpvalues(boolean[] boolarray) {
        boolean flag = false;
        for (boolean aBoolarray : boolarray) {
            if (aBoolarray)
                flag = true;
            else {
                flag = false;
                break;
            }
        }
        return flag;
    }

    private boolean validateSumPercentage() {
        for (i = 1; i < contributorPercentageList.size(); i++) {
            temp = contributorPercentageList.get(i);
            tempvalue = Integer.parseInt(temp);
            sum += tempvalue;
        }
        if (sum > 99) {
            Toast.makeText(this, "Percentage exceding 100, please check", Toast.LENGTH_SHORT).show();
            return false;
        } else
        {
            return true;
        }
    }

    private boolean validateContributorName(String Cname) {
        if (Cname.isEmpty()) {
            EdittextContributorName.setError("Field can't be empty");
            return false;
        } else {
            EdittextContributorName.setError(null);
            return true;
        }
    }

    private boolean validatePercentage(String Cpercentage) {
        if (Cpercentage.isEmpty()) {
            EdittextContributorPercentage.setError("Field can't be empty");
            return false;
        } else {
            EdittextContributorPercentage.setError(null);
            return true;
        }
    }

    private boolean validateContributorType() {
        if (SpinnerContributorType.getSelectedItem().toString().trim().equals("Select")) {
            Toast.makeText(this, "Contributor Type is not selected", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
    String aa;

    private void getContributorUserId(){

        FirebaseDatabase.getInstance().getReference("UsernameUserId")
                .child(EdittextContributorName.getText().toString().trim())
                .child("tempUserId")
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    aa = dataSnapshot.getValue(String.class);
                    continueGetContributor();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

    }

    private void continueGetContributor(){
        final String name = EdittextContributorName.getText().toString().trim();
        final String percentage = EdittextContributorPercentage.getText().toString().trim();
        final String type = SpinnerContributorType.getSelectedItem().toString().trim();

        contributorVideo.put(VIDEO_CONTRIBUTOR_NAME, name);
        contributorVideo.put(VIDEO_CONTRIBUTOR_PERCENTAGE, percentage);
        contributorVideo.put(VIDEO_CONTRIBUTOR_TYPE, type);

        Contributor contributor1 = new Contributor(name, percentage, type);
        contributorFirestoreList.add(name + "," + percentage + "," + type + "," + aa);
        contributorList.add(contributor1);
        contributorPercentageList.add(percentage);

        contributorAdapter = new contributorAdapter(this, R.layout.activity_contributor_listview, contributorList, this);
        ContributorValuesLV.setAdapter(contributorAdapter);
        contributorAdapter.notifyDataSetChanged();
        ContributorSuccess = true;
    }

    private void GetContributor() {

        final String name = EdittextContributorName.getText().toString().trim();
        final String percentage = EdittextContributorPercentage.getText().toString().trim();
        final String type = SpinnerContributorType.getSelectedItem().toString().trim();

        if (!validateContributorName(name) | !validatePercentage(percentage) | !validateContributorType()) {
            {
                ContributorSuccess = false;
            }
        } else {

            getContributorUserId();
        }
    }

    private void resetContributor() {
        EdittextContributorName.setText("");
        EdittextContributorPercentage.setText("");
        SpinnerContributorType.setSelection(0);
    }

    @Override
    public void onClick(View view) {
        if (view == contributeBtn) {
            addContributorLayout.setVisibility(View.VISIBLE);
        }

        if (view == addContributorBtn) {
            GetContributor();
            if (ContributorSuccess) {
                resetContributor();
                addContributorLayout.setVisibility(View.GONE);
                Toast.makeText(this, "Contributor added", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Contributor not added", Toast.LENGTH_SHORT).show();
            }
        }

        if (view == selectVideoBtn) {
            selectVideo();
        }

        if (view == uploadBtn) {
            if (videoSelected.get() && !startedUpload){
                registerVideo();
            }else if (videoSelected.get() && !startedUpload){
                Toast.makeText(this, "Current Upload is being processed.", Toast.LENGTH_SHORT).show();
            }
            else
            {
                if (!videoSelected.get()) {
                    Toast.makeText(this, "Please Select a Video.", Toast.LENGTH_SHORT).show();
                }
            }
        }

        if(view == ContributorCancelBtn){
            resetContributor();
            addContributorLayout.setVisibility(View.GONE);
        }
        if (view == Help) {
            Intent helpIntent = new Intent(this, ContributorHelp.class);
            helpIntent.putExtra("TYPE", getIntent().getExtras().getString("TYPE"));
            startActivity(new Intent(helpIntent));

        }
        if (view == preview) {
            if (!artistUploadVideo.isPlaying()) {
                artistUploadVideo.start();
            preview.setText("Pause preview");
            } else {
                artistUploadVideo.pause();
                preview.setText("Resume");
            }
        }

    }

    @Override
    public void deleteposition(int deletePosition) {
        contributorList.remove(deletePosition);
        contributorFirestoreList.remove(deletePosition);
        contributorAdapter.notifyDataSetChanged();
    }

    private String millisecondsToString(long duration){
        long minutes = (duration / 1000) / 60;
        long seconds = (duration / 1000) % 60;
        return String.format("%d:%02d", minutes, seconds);
    }



    /**
     * Handles back button on toolbar
     * @return true if pressed
     */
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}