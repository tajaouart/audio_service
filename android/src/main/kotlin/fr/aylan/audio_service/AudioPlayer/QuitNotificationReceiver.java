package fr.aylan.audio_service.AudioPlayer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


public class QuitNotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        MediaService.quit(context);
    }
}