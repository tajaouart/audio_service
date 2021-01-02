package fr.aylan.audio_service.AudioPlayer;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.support.v4.media.session.MediaSessionCompat;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import fr.aylan.audio_service.R;

import static fr.aylan.audio_service.AudioPlayer.App.CHANNEL_2_ID;


public class NotificationReceiver extends BroadcastReceiver {

    MediaPlayer mp;
    boolean playing = false;
    private MediaSessionCompat mediaSession;
    public static MediaSessionCompat mMediaSesion;
    public static NotificationManagerCompat notificationManager;
    public static NotificationCompat.Builder notificationPlaying;
    public static NotificationCompat.Builder notificationPaused;
    String url;
    String title;
    String message;
    PendingIntent pIntentlogin;
    Bitmap artwork;
    PendingIntent pendingI;


    @Override
    public void onReceive(Context context, Intent intent) {
        Intent servIntent = new Intent(context, MediaService.class);
        servIntent.putExtra("to_play", !MediaService.to_play);
        Toast.makeText(context, "Sent " + !MediaService.to_play, Toast.LENGTH_SHORT).show();
        context.startService(servIntent);
    }

    public void pause(Context context) {
        notificationPaused = new NotificationCompat.Builder(context, CHANNEL_2_ID)
                .setSmallIcon(R.drawable.music_icon)
                .setContentTitle(title)
                .setContentText(message)
                .setLargeIcon(artwork)
                .addAction(R.drawable.ic_skip_previous_black_24dp, "Previous", pIntentlogin)
                .addAction(R.drawable.ic_play_arrow_black_24dp, "Pause", pIntentlogin)
                .addAction(R.drawable.ic_skip_next_black_24dp, "Next", pIntentlogin)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                                .setShowActionsInCompactView(0, 1, 2)
                                .setMediaSession(mediaSession.getSessionToken())
                        //.bigPicture(picture)
                        //.bigLargeIcon(null)
                )
                .setSubText("Sub Text")
                .setPriority(NotificationCompat.PRIORITY_LOW);
        //startForeground(2, notificationPlaying.build());
        notificationManager.notify(2, notificationPaused.build());
        mp.stop();
        MediaService.to_play = false;
    }

    public void play(Context context) {
        notificationPlaying = new NotificationCompat.Builder(context, CHANNEL_2_ID)
                .setSmallIcon(R.drawable.music_icon)
                .setContentTitle(title)
                .setContentText(message)
                .setLargeIcon(artwork)
                .setContentIntent(pendingI)
                .addAction(R.drawable.ic_skip_previous_black_24dp, "Previous", pIntentlogin)
                .addAction(R.drawable.ic_pause_black_24dp, "Pause", pIntentlogin)
                .addAction(R.drawable.ic_skip_next_black_24dp, "Next", pIntentlogin)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                                .setShowActionsInCompactView(0, 1, 2)
                                .setMediaSession(mediaSession.getSessionToken())
                        //.bigPicture(picture)
                        //.bigLargeIcon(null)
                )
                .setSubText("Sub Text")
                .setPriority(NotificationCompat.PRIORITY_LOW);
        notificationManager.notify(2, notificationPlaying.build());


        mp = new MediaPlayer();
        audioPlayer(url);


        MediaService.to_play = true;
    }

    public void audioPlayer(String path) {
        try {
            mp.setDataSource(path);
            mp.prepare();
            mp.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}