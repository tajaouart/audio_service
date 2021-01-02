package fr.aylan.audio_service.AudioPlayer;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;


public class App extends Application {
    public static final String CHANNEL_ID = "channelService";
    public static final String CHANNEL_2_ID = "channel1";

    private static App instance;

    public App() {
        instance = this;
    }

    public static App getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        createNotificationChannels();
    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "MediaService Channel 1",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            //serviceChannel.setDescription("This is Channel 1");


            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }
}