package fr.aylan.audio_service.AudioPlayer;


import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.TaskStackBuilder;

import java.util.ArrayList;


public class MediaService extends Service implements AdapterView.OnItemSelectedListener {


    private static final String TAG = "MediaService";
    public static boolean to_play;
    private static final String currentAudioURL = "";

    private MediaSessionCompat mediaSession;

    public static PlayerAdapter mPlayerAdapter;
    static MediaService MediaServiceActivity;


    public static MediaSessionCompat mMediaSesion;
    public static NotificationManagerCompat notificationManager;
    public static NotificationCompat.Builder notificationPlaying;
    public static NotificationCompat.Builder notificationPaused;
    public Activity activity = null;

    static String title;
    static String message;
    Intent intentAction;
    PendingIntent pIntentlogin;
    PendingIntent pIntentActionCloseNotif;
    Intent intentActionCloseNotif;
    //Play next
    PendingIntent pIntentActionPlayNextNotif;
    Intent intentActionPlayNextNotif;
    //Play Previous
    PendingIntent pIntentActionPlayPreviousNotif;
    Intent intentActionPlayPreviousNotif;

    Bitmap artwork;
    PendingIntent pendingI;
    static Runnable mUpdateTime = null;

    public static ArrayList dataSet = new ArrayList<PlayListItem>();
    public static int position = 0;


    // Quit notification
    public static void quit(Context context) {
        Toast.makeText(context, "Quit Notification", Toast.LENGTH_LONG).show();
        MediaServiceActivity.stopForeground(false);
        notificationManager.cancel(2);
        MediaServiceActivity.onDestroy();
    }

    @Override
    public void onCreate() {
        position = Home.position;
        activity = getActivity();
        //initializeUI();
        initializePlaybackController();
        title = "title";
        message = "message";
        intentAction = new Intent(this, NotificationReceiver.class);
        pIntentlogin = PendingIntent.getBroadcast(this, 1, intentAction, PendingIntent.FLAG_UPDATE_CURRENT);

        // play next
        intentActionPlayNextNotif = new Intent(this, PlayNextNotificationReceiver.class);
        pIntentActionPlayNextNotif = PendingIntent.getBroadcast(this, 3, intentActionPlayNextNotif, PendingIntent.FLAG_UPDATE_CURRENT);

        // play previous
        intentActionPlayPreviousNotif = new Intent(this, PlayPreviousNotificationReceiver.class);
        pIntentActionPlayPreviousNotif = PendingIntent.getBroadcast(this, 3, intentActionPlayPreviousNotif, PendingIntent.FLAG_UPDATE_CURRENT);

        // close notificxation
        intentActionCloseNotif = new Intent(this, QuitNotificationReceiver.class);
        pIntentActionCloseNotif = PendingIntent.getBroadcast(this, 3, intentActionCloseNotif, PendingIntent.FLAG_UPDATE_CURRENT);


        artwork = BitmapFactory.decodeResource(getResources(), R.drawable.camera);


        notificationManager = NotificationManagerCompat.from(this);

        mediaSession = new MediaSessionCompat(this, "tag");
        MediaServiceActivity = this;
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        to_play = intent.getBooleanExtra("to_play", false);
        initializeSeekbar();

        //Bitmap picture = BitmapFactory.decodeResource(getResources(),R.drawable.music_icon);

        if (to_play) {
            play(getApplicationContext());
        } else {
            sendNotificationPause();
        }
        //notificationManager.notify(2, notificationPlaying.build());
        return START_NOT_STICKY;
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private synchronized String createChannel() {
        NotificationManager mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        String name = "snap map fake location ";
        int importance = NotificationManager.IMPORTANCE_LOW;

        NotificationChannel mChannel = new NotificationChannel("snap map channel", name, importance);

        mChannel.enableLights(true);
        mChannel.setLightColor(Color.BLUE);
        if (mNotificationManager != null) {
            mNotificationManager.createNotificationChannel(mChannel);
        } else {
            stopSelf();
        }
        return "snap map channel";
    }

    private void pauseAudio() {
        if (mPlayerAdapter.isPlaying()) {
            mPlayerAdapter.pause();
            Home.mPlayButton.setBackground(getResources().getDrawable(R.drawable.ic_play_arrow_black_24dp));
        } else {
            PlayListItem song = (PlayListItem) dataSet.get(position);
            String url = song.getUrl();
            mPlayerAdapter.play(url);
            Home.mPlayButton.setBackground(getResources().getDrawable(R.drawable.ic_pause_black_24dp));
        }
    }

    public static void play(Context context) {
        startAudio(context);
        title = String.valueOf(((PlayListItem) dataSet.get(position)).getTitle());
        message = milliSecondsToTimer(mPlayerAdapter.getDuration());
        getMediaServiceActivity().sendNotificationPlaying();
    }


    public void sendNotificationPause() {
        String channel;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            channel = createChannel();
        else {
            channel = "";
        }
        Intent mainIntent = new Intent(this, Home.class);
        mainIntent.putExtra("Resume", true);


        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        // Adds the back stack.
        stackBuilder.addParentStack(Home.class);
        // Adds the Intent to the top of the stack.
        stackBuilder.addNextIntent(mainIntent);
        // Gets a PendingIntent containing the entire back stack.
        PendingIntent mainPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        mainIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        notificationPaused = new NotificationCompat.Builder(this, channel)
                .setSmallIcon(R.drawable.music_icon)
                .setContentTitle(title)
                .setContentText(message)
                .setLargeIcon(artwork)
                .setContentIntent(mainPendingIntent)
                .addAction(R.drawable.ic_skip_previous_black_24dp, "Previous", pIntentActionPlayPreviousNotif)
                .addAction(R.drawable.ic_play_arrow_black_24dp, "Pause", pIntentlogin)
                .addAction(R.drawable.ic_skip_next_black_24dp, "Next", pIntentActionPlayNextNotif)
                .addAction(R.drawable.ic_close_black_24dp, "Next", pIntentActionCloseNotif)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                                .setShowActionsInCompactView(0, 1, 2)
                                .setMediaSession(mediaSession.getSessionToken())
                        //.bigPicture(picture)
                        //.bigLargeIcon(null)
                )
                .setSubText("Sub Text")
                .setOnlyAlertOnce(true)
                .setVibrate(new long[]{0L})
                .setPriority(NotificationCompat.PRIORITY_LOW);
        startForeground(2, notificationPaused.build());


        if (mPlayerAdapter.isPlaying())
            pauseAudio();
    }

    public static void onNotificationClicked(Context context) {

        if (mPlayerAdapter.isPlaying()) {
            //generateBigTextStyleNotification();
            PlayListItem song = (PlayListItem) dataSet.get(position);
            String url = song.getUrl();
            mPlayerAdapter.play(url);
            Home.mPlayButton.setBackground(context.getResources().getDrawable(R.drawable.ic_pause_black_24dp));

        } else {
            mPlayerAdapter.pause();
            initializeSeekbar();
            Home.mPlayButton.setBackground(context.getResources().getDrawable(R.drawable.ic_play_arrow_black_24dp));
        }
        mUpdateTime.run();
        try {
            Home.mTVDuration.setText(milliSecondsToTimer(mPlayerAdapter.getDuration()));
            title = String.valueOf(((PlayListItem) dataSet.get(position)).getTitle());
            message = milliSecondsToTimer(mPlayerAdapter.getDuration());
        } catch (Exception e) {

        }


        if (mPlayerAdapter.isPlaying()) {
            getMediaServiceActivity().sendNotificationPlaying();
        } else {
            getMediaServiceActivity().sendNotificationPause();
            initializeSeekbar();
            PlayListItem song = (PlayListItem) dataSet.get(position);
            String url = song.getUrl();
            //mPlayerAdapter.play(url);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //mPlayerAdapter.pause();
            synchronized (mPlayerAdapter) {
                mPlayerAdapter.notifyAll();
            }
            mUpdateTime.run();
            Home.mTVDuration.setText(milliSecondsToTimer(mPlayerAdapter.getDuration()));
        }
    }

    public static MediaService getMediaServiceActivity() {
        return MediaServiceActivity;
    }

    void sendNotificationPlaying() {
        String channel;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            channel = createChannel();
        else {
            channel = "";
        }
        Intent mainIntent = new Intent(this, Home.class);
        mainIntent.putExtra("Resume", true);


        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        // Adds the back stack.
        stackBuilder.addParentStack(Home.class);
        // Adds the Intent to the top of the stack.
        stackBuilder.addNextIntent(mainIntent);
        // Gets a PendingIntent containing the entire back stack.
        PendingIntent mainPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        mainIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        notificationPlaying = new NotificationCompat.Builder(this, channel)
                .setSmallIcon(R.drawable.music_icon)
                .setContentTitle(title)
                .setContentText(message)
                .setLargeIcon(artwork)
                .setContentIntent(pendingI)
                .setContentIntent(mainPendingIntent)
                .addAction(R.drawable.ic_skip_previous_black_24dp, "Previous", pIntentActionPlayPreviousNotif)
                .addAction(R.drawable.ic_pause_black_24dp, "Pause", pIntentlogin)
                .addAction(R.drawable.ic_skip_next_black_24dp, "Next", pIntentActionPlayNextNotif)
                .addAction(R.drawable.ic_close_black_24dp, "Next", pIntentActionCloseNotif)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                                .setShowActionsInCompactView(0, 1, 2, 3)
                                .setMediaSession(mediaSession.getSessionToken())
                        //.bigPicture(picture)
                        //.bigLargeIcon(null)
                )
                .setSubText("Sub Text")
                .setVibrate(new long[]{0L})
                .setOnlyAlertOnce(true)
                .setPriority(NotificationCompat.PRIORITY_LOW);
        startForeground(2, notificationPlaying.build());
    }

    private static void startAudio(Context context) {
        if (mPlayerAdapter.isPlaying()) {
            mPlayerAdapter.pause();
            //Home.mPlayButton.setBackground(context.getResources().getDrawable(R.drawable.ic_play_arrow_black_24dp));
        } else {
            //generateBigTextStyleNotification();
            PlayListItem song = (PlayListItem) dataSet.get(position);
            String url = song.getUrl();
            mPlayerAdapter.play(url);
            //Home.mPlayButton.setBackground(context.getResources().getDrawable(R.drawable.ic_pause_black_24dp));
        }
        mUpdateTime.run();

        //Home.mTVDuration.setText(milliSecondsToTimer(mPlayerAdapter.getDuration()));

    }


    private void updatePlayer(int currentDuration) {
       // Home.mTVTime.setText("" + milliSecondsToTimer((long) currentDuration));
    }

    /**
     * Function to convert milliseconds time to Timer Format
     * Hours:Minutes:Seconds
     */
    public static String milliSecondsToTimer(long milliseconds) {
        String finalTimerString = "";
        String secondsString = "";
        String minutesString = "";

        // Convert total duration into time
        int hours = (int) (milliseconds / (1000 * 60 * 60));
        int minutes = (int) (milliseconds % (1000 * 60 * 60)) / (1000 * 60);
        int seconds = (int) ((milliseconds % (1000 * 60 * 60)) % (1000 * 60) / 1000);
        // Add hours if there
        if (hours > 0) {
            finalTimerString = hours + ":";
        }

        // Prepending 0 to seconds if it is one digit
        if (seconds < 10) {
            secondsString = "0" + seconds;
        } else {
            secondsString = "" + seconds;
        }
        if (minutes < 10) {
            minutesString = "0" + minutes;
        }

        finalTimerString = finalTimerString + minutesString + ":" + secondsString;

        // return timer string
        return finalTimerString;
    }

/*
    private static void initializeSeekbar() {
        if (Home.mSeekbarAudio != null) {
            Home.mSeekbarAudio.setOnSeekBarChangeListener(
                    new SeekBar.OnSeekBarChangeListener() {
                        int userSelectedPosition = 0;

                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {
                            Home.mUserIsSeeking = true;
                        }

                        @Override
                        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                            if (fromUser) {
                                userSelectedPosition = progress;
                            }
                        }

                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {
                            Home.mUserIsSeeking = false;
                            mPlayerAdapter.seekTo(userSelectedPosition);
                        }
                    });
        }

    }
*/

    private void initializePlaybackController() {
        MediaPlayerHolder mMediaPlayerHolder = new MediaPlayerHolder(this);
        Log.d(TAG, "initializePlaybackController: created MediaPlayerHolder");
        //mMediaPlayerHolder.setPlaybackInfoListener(Home.getPlaybackListenerInstance());
        mPlayerAdapter = mMediaPlayerHolder;
        Log.d(TAG, "initializePlaybackController: MediaPlayerHolder progress callback set");
        mUpdateTime = new Runnable() {
            public void run() {
                int currentDuration;
                if (MediaService.mPlayerAdapter.isPlaying()) {
                    currentDuration = MediaService.mPlayerAdapter.getCurrentTime();
                    updatePlayer(currentDuration);
                    //Home.mTVTime.postDelayed(this, 1000);
                } else {
                    //Home.mTVTime.removeCallbacks(this);
                }
            }
        };


    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public Activity getActivity() {
        return activity;
    }

    @Override
    public void onDestroy() {
        try {
            SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("lastPlayedAudioURL", currentAudioURL);
            editor.putInt("time", mPlayerAdapter.getCurrentTime());
            editor.commit();
        } catch (Exception e) {

        }

        //mPlayerAdapter.stop();
        //Home.mPlayButton.setBackground(this.getResources().getDrawable(R.drawable.ic_play_arrow_black_24dp));
        mPlayerAdapter.release();
        super.onDestroy();
    }


    public static void PlayingFinished(Context context) {
        //Home.mPlayButton.setBackground(context.getResources().getDrawable(R.drawable.ic_play_arrow_black_24dp));
        Intent servIntent = new Intent(context, MediaService.class);
        servIntent.putExtra("to_play", false);
        Toast.makeText(context, "Sent " + false, Toast.LENGTH_SHORT).show();
        context.startService(servIntent);
        //Home.mTVTime.setText("00:00");
    }
}
