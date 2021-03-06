package com.hitstreamr.hitstreamrbeta;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.hitstreamr.hitstreamrbeta.Authentication.Splash;

import java.util.Map;
import java.util.Objects;
//TODO APACHE NOTICE
/**
 *
 *
 */
public class HitStreamrFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";
    private final int REQUEST_CODE = 90210;
    private final int FOLLOW_NOTIF = 1234;
    private final int FAVE_NOTIF = 1235;
    private final int REPLY_NOTIF = 1236;
    private final int REPOST_NOTIF = 1237;
    private final int POST_NOTIF = 1237;

    //Preference Constants
    private final String FOLLOW = "newFollow";
    private final String ALL_PUSH = "pushNotifs";
    private final String REPOST = "repostVid";
    private final String NEW_POST = "newPost";
    private final String FAVE = "newFave";
    private final String COMMENT = "newComment";
    private final String REPLY = "replyComment";
    private final String FEATURES = "newFeatures";
    private final String TIPS = "hitstreamrTips";
    private final String SURVEY = "surveys";

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // [START_EXCLUDE]
        // There are two types of messages data messages and notification messages. Data messages
        // are handled
        // here in onMessageReceived whether the app is in the foreground or background. Data
        // messages are the type
        // traditionally used with GCM. Notification messages are only received here in
        // onMessageReceived when the app
        // is in the foreground. When the app is in the background an automatically generated
        // notification is displayed.
        // When the user taps on the notification they are returned to the app. Messages
        // containing both notification
        // and data payloads are treated as notification messages. The Firebase console always
        // sends notification
        // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
        // [END_EXCLUDE]

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());

            if (remoteMessage.getData().containsKey("time") && remoteMessage.getData().get("time").equalsIgnoreCase("short")) {
                // Handle message within 10 seconds
                if(remoteMessage.getData().containsKey("type")){
                    handleNow(remoteMessage.getData().get("type"),remoteMessage);
                }

            } else {
                // For long-running tasks (10 seconds or more) use Firebase Job Dispatcher.
                scheduleJob();
            }

        }
    }
    // [END receive_message]


    // [START on_new_token]

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    @Override
    public void onNewToken(String token) {
        Log.d(TAG, "Refreshed token: " + token);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        //TODO save userid in and use if that is around to update the token
        sendRegistrationToServer(token);
    }
    // [END on_new_token]

    /**
     * Schedule a job using FirebaseJobDispatcher.
     */
    private void scheduleJob() {

    }

    /**
     * Handle time allotted to BroadcastReceivers.
     */
    private void handleNow(String type, RemoteMessage message) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();


            switch (type) {
                case "follow":
                    if(user != null ) {
                        SharedPreferences userDetails = this.getSharedPreferences(user.getUid(), MODE_PRIVATE);
                        if (userDetails.getBoolean(ALL_PUSH, false) && userDetails.getBoolean(FOLLOW, false))
                            sendNotification(type, message.getData(), true);
                    }
                    break;
                case "fave":
                    if(user != null ) {
                        SharedPreferences userDetails = this.getSharedPreferences(user.getUid(), MODE_PRIVATE);
                        if (userDetails.getBoolean(ALL_PUSH, false) && userDetails.getBoolean(FAVE, false))
                            sendNotification(type,message.getData(), true);
                    }
                    break;
                case "reply": {
                    if(user != null ) {
                        SharedPreferences userDetails = this.getSharedPreferences(user.getUid(), MODE_PRIVATE);
                        if (userDetails.getBoolean(ALL_PUSH, false) && userDetails.getBoolean(REPLY, false))
                            sendNotification(type,message.getData(), true);
                    }
                }
                case "repost": {
                    if(user != null ) {
                        SharedPreferences userDetails = this.getSharedPreferences(user.getUid(), MODE_PRIVATE);
                        if (userDetails.getBoolean(ALL_PUSH, false) && (userDetails.getBoolean(REPOST, false) || (userDetails.getBoolean(NEW_POST, false))))
                            sendNotification(type, message.getData(), true);
                    }
                }
                case "post": {
                    if(user != null ) {
                        SharedPreferences userDetails = this.getSharedPreferences(user.getUid(), MODE_PRIVATE);
                        if (userDetails.getBoolean(ALL_PUSH, false) && (userDetails.getBoolean(NEW_POST, false)))
                            sendNotification(type, message.getData(), true);
                    }
                }
                default:
                    break;
            }
            Log.d(TAG, "Short lived task: " + type+ " is done.");

    }

    /**
     * Persist token to third-party servers.
     *
     * Modify this method to associate the user's FCM InstanceID token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private void sendRegistrationToServer(String token) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if(user != null) {
            //if we have a user overwrite their old token
            FirebaseDatabase.getInstance().getReference("FirebaseNotificationTokens")
                    .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                    .child(token)
                    .setValue(true)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()) {
                                Log.d(TAG, "New Token Saved");
                            }
                        }
                    });
        }
    }



    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param message FCM message body received as a Map
     */
    private void sendNotification(String type, Map<String, String> message, boolean loggedIn) {
        //Redirect to Splash in case they aren't logged in
        Intent intent = new Intent(this, Splash.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, REQUEST_CODE, intent,
                PendingIntent.FLAG_ONE_SHOT);

        String channelId = "Default Channel ID";
        switch(type){
            case "follow":
                channelId = getString(R.string.follow_notification_channel_id);
                break;
            case "fave":
                channelId = "New Fave";
                break;
            case "reply": {
                channelId = "New Reply";
                break;
            }
            case "repost": {
                channelId = "New Repost";
                break;
            }
            case "post": {
                channelId = "New Post";
                break;
            }
        }

        //TODO Should this use the Hitstreamer or Prof Pic
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.new_hitstreamr_h_logo_wht_w_)
                        .setContentTitle(message.get("title"))
                        .setContentText(message.get("body"))
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("", "DEFAULT", NotificationManager.IMPORTANCE_DEFAULT);
            switch(type){
                case "follow":
                  channel = new NotificationChannel(channelId,
                            "New Follows",
                            NotificationManager.IMPORTANCE_DEFAULT);
                    break;
                case "fave":
                   channel = new NotificationChannel(channelId,
                            "New Faves",
                            NotificationManager.IMPORTANCE_DEFAULT);
                    break;
                case "reply": {
                    channel = new NotificationChannel(channelId,
                            "New Replies",
                            NotificationManager.IMPORTANCE_DEFAULT);
                    break;
                }
                case "repost": {
                    channel = new NotificationChannel(channelId,
                            "New Reposts",
                            NotificationManager.IMPORTANCE_DEFAULT);
                    break;
                }
                case "post": {
                    channel = new NotificationChannel(channelId,
                            "New Posts",
                            NotificationManager.IMPORTANCE_DEFAULT);
                    break;
                }
            }

            notificationManager.createNotificationChannel(channel);
        }

        switch(type){
            case "follow":
                notificationManager.notify(FOLLOW_NOTIF, notificationBuilder.build());
                break;
            case "fave":
                notificationManager.notify(FAVE_NOTIF, notificationBuilder.build());
                break;
            case "reply":
                notificationManager.notify(REPLY_NOTIF,notificationBuilder.build());
                break;
            case "repost":
                notificationManager.notify(REPOST_NOTIF, notificationBuilder.build());
                break;
            case "post": {
                notificationManager.notify(POST_NOTIF, notificationBuilder.build());
                break;
            }
        }

    }
}

