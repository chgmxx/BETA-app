package com.hitstreamr.hitstreamrbeta;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.audio.AudioRendererEventListener;
import com.google.android.exoplayer2.decoder.DecoderCounters;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.android.exoplayer2.video.VideoRendererEventListener;
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

import de.hdodenhof.circleimageview.CircleImageView;

public class VideoPlayer extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "PlayerActivity";

    // bandwidth meter to measure and estimate bandwidth
    private static final DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();

    //ExoPlayer
    private ExoPlayer player;
    private PlayerView playerView;
    private ComponentListener componentListener;

    //Layout
    private LinearLayout DescLayout;

    //ImageButton
    private ImageButton collapseDecriptionBtn;
    private ImageButton likeBtn;

    //TextView
    private TextView TextViewVideoDescription;
    private TextView TextViewTitle;
    private TextView artistNameBold;
    private TextView artistName;

    //CircleImageView
    private CircleImageView artistProfPic;
    StorageReference artistProfReference;

    //Video URI
    private Uri videoUri;

    FirebaseUser currentFirebaseUser;
    FirebaseDatabase database;
    DatabaseReference myRef;


    private long playbackPosition;
    private int currentWindow;
    private boolean playWhenReady = true;

    private boolean collapseVariable = false;

    Video vid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        vid = getIntent().getParcelableExtra("VIDEO");

        currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("VideoLikes");

        componentListener = new ComponentListener();
        playerView = findViewById(R.id.artistVideoPlayer);

        //Linear Layout
        DescLayout = findViewById(R.id.DescriptionLayout);

        //Profile Picture
        artistProfPic = findViewById(R.id.artistProfilePicture);

        //ImageButton
        collapseDecriptionBtn = findViewById(R.id.collapseDescription);
        likeBtn = findViewById(R.id.fave);

        //TextView
        TextViewVideoDescription = findViewById(R.id.videoDescription);
        TextViewVideoDescription.setText(vid.getDescription());

        TextViewTitle = findViewById(R.id.videoPlayerTitle);
        TextViewTitle.setText(vid.getTitle());

        artistNameBold = findViewById(R.id.artistNameBold);
        artistName = findViewById(R.id.Artist);
        artistName.setText(vid.getUsername());
        artistNameBold.setText(vid.getUsername());

        artistProfReference = FirebaseStorage.getInstance().getReferenceFromUrl("gs://hitstreamr-beta.appspot.com/profilePictures/" + vid.getUsername());

        if (artistProfReference == null) {
            Glide.with(getApplicationContext()).load(R.mipmap.ic_launcher_round).into(artistProfPic);
        } else {
            Glide.with(getApplicationContext()).load(artistProfPic).into(artistProfPic);
        }

        //Listners
        collapseDecriptionBtn.setOnClickListener(this);
        likeBtn.setOnClickListener(this);

        //videoUri = Uri.parse("https://firebasestorage.googleapis.com/v0/b/hitstreamr-beta.appspot.com/o/videos%2FHJsb8mUO2lgueTaCrs7JgIbxmJ82%2Framanuja?alt=media&token=59489ad2-977e-496a-864b-61816539220a");
        //videoUri = Uri.parse("https://firebasestorage.googleapis.com/v0/b/hitstreamr-beta.appspot.com/o/videos%2F0p4OHsSkWuMMAJzPCqmQXxtzkGt2%2Fmp4%2FmusicvideoB?alt=media&token=01fe7238-a40c-4eaf-b4a4-6a6e4baef2a5");
        //videoUri = Uri.parse("https://firebasestorage.googleapis.com/v0/b/hitstreamr-beta.appspot.com/o/videos%2F9UeYFJxKToThqNwmZdeqbI8gOaA2%2Fmp4%2Fbeliever?alt=media&token=eb45446e-54bf-4c22-9c91-26e72d5211e4");
        videoUri = Uri.parse("https://firebasestorage.googleapis.com/v0/b/hitstreamr-beta.appspot.com/o/videos%2F9UeYFJxKToThqNwmZdeqbI8gOaA2%2Fmp4%2Fscreentest3?alt=media&token=bf2437ba-81ff-4ee3-bf58-f57dbe6dae23");
    }

    @Override
    public void onStart() {
        super.onStart();
        if (Util.SDK_INT > 23) {
            initializePlayer();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        hideSystemUi();
        if ((Util.SDK_INT <= 23 || player == null)) {
            initializePlayer();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (Util.SDK_INT <= 23) {
            releasePlayer();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (Util.SDK_INT > 23) {
            releasePlayer();
        }
    }

    private void initializePlayer() {
        if (player == null) {
            // a factory to create an AdaptiveVideoTrackSelection
            TrackSelection.Factory adaptiveTrackSelectionFactory =
                    new AdaptiveTrackSelection.Factory(BANDWIDTH_METER);
            // using a DefaultTrackSelector with an adaptive video selection factory
            player = ExoPlayerFactory.newSimpleInstance(new DefaultRenderersFactory(this),
                    new DefaultTrackSelector(adaptiveTrackSelectionFactory), new DefaultLoadControl());
            player.addListener(componentListener);
            //player.addVideoDebugListener(componentListener);
            //player.addAudioDebugListener(componentListener);
            playerView.setPlayer(player);
            player.setPlayWhenReady(playWhenReady);
            player.seekTo(currentWindow, playbackPosition);
        }
        DefaultHttpDataSourceFactory dataSourceFactory = new DefaultHttpDataSourceFactory("exoplayer_video");
        ExtractorMediaSource mediaSource = new ExtractorMediaSource.Factory(dataSourceFactory).createMediaSource(videoUri);
        //MediaSource mediaSource = buildMediaSource(Uri.parse(getString(R.string.media_url_dash)));
        player.prepare(mediaSource, true, false);
        playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);
        //playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT);
        //player.setVideoScalingMode(C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
    }

    private void releasePlayer() {
        if (player != null) {
            playbackPosition = player.getCurrentPosition();
            currentWindow = player.getCurrentWindowIndex();
            playWhenReady = player.getPlayWhenReady();
            player.removeListener(componentListener);
            //player.removeVideoDebugListener(componentListener);
            //player.removeAudioDebugListener(componentListener);
            player.release();
            player = null;
        }
    }

    @SuppressLint("InlinedApi")
    private void hideSystemUi() {
        playerView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }

    private void likeVideo() {
        Log.e(TAG, "Your video Id is:" + vid.getVideoId());
        Toast.makeText(VideoPlayer.this, "You liked" + vid.getVideoId(), Toast.LENGTH_SHORT).show();

        //String temp = vid.getVideoId();
        String ttt = "true";

        FirebaseDatabase.getInstance()
                .getReference("VideoLikes")
                .child(vid.getUserId())
                .child("userID")
                .setValue(ttt)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        finished();
                    }
                });
    }

    private void finished() {
        FirebaseDatabase.getInstance().getReference("VideoLikes")
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long value = dataSnapshot.getChildrenCount();
                Log.e(TAG, "Value is: " + value);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
        Toast.makeText(VideoPlayer.this, "You liked" + vid.getVideoId(), Toast.LENGTH_SHORT).show();
    }


    private class ComponentListener extends Player.DefaultEventListener implements
            VideoRendererEventListener, AudioRendererEventListener {

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            String stateString;
            switch (playbackState) {
                case Player.STATE_IDLE:
                    stateString = "ExoPlayer.STATE_IDLE      -";
                    break;
                case Player.STATE_BUFFERING:
                    stateString = "ExoPlayer.STATE_BUFFERING -";
                    break;
                case Player.STATE_READY:
                    stateString = "ExoPlayer.STATE_READY     -";
                    break;
                case Player.STATE_ENDED:
                    stateString = "ExoPlayer.STATE_ENDED     -";
                    break;
                default:
                    stateString = "UNKNOWN_STATE             -";
                    break;
            }
            Log.d(TAG, "changed state to " + stateString + " playWhenReady: " + playWhenReady);
        }

        // Implementing VideoRendererEventListener.

        @Override
        public void onVideoEnabled(DecoderCounters counters) {
            // Do nothing.
        }

        @Override
        public void onVideoDecoderInitialized(String decoderName, long initializedTimestampMs, long initializationDurationMs) {
            // Do nothing.
        }

        @Override
        public void onVideoInputFormatChanged(Format format) {
            // Do nothing.
        }

        @Override
        public void onDroppedFrames(int count, long elapsedMs) {
            // Do nothing.
        }

        @Override
        public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {
            // Do nothing.
        }

        @Override
        public void onRenderedFirstFrame(Surface surface) {
            // Do nothing.
        }

        @Override
        public void onVideoDisabled(DecoderCounters counters) {
            // Do nothing.
        }

        // Implementing AudioRendererEventListener.

        @Override
        public void onAudioEnabled(DecoderCounters counters) {
            // Do nothing.
        }

        @Override
        public void onAudioSessionId(int audioSessionId) {
            // Do nothing.
        }

        @Override
        public void onAudioDecoderInitialized(String decoderName, long initializedTimestampMs, long initializationDurationMs) {
            // Do nothing.
        }

        @Override
        public void onAudioInputFormatChanged(Format format) {
            // Do nothing.
        }

        @Override
        public void onAudioSinkUnderrun(int bufferSize, long bufferSizeMs, long elapsedSinceLastFeedMs) {
            // Do nothing.
        }

        @Override
        public void onAudioDisabled(DecoderCounters counters) {
            // Do nothing.
        }
    }

    @Override
    public void onClick(View view) {
        if (view == collapseDecriptionBtn) {
            if (!collapseVariable) {
                TextViewVideoDescription.setVisibility(View.GONE);
                collapseVariable = true;
            } else if (collapseVariable) {
                TextViewVideoDescription.setVisibility(View.VISIBLE);
                collapseVariable = false;
            }
        }

        if (view == likeBtn) {
            //Toast.makeText(VideoPlayer.this, "You liked", Toast.LENGTH_SHORT).show();
            likeVideo();
        }
    }
}