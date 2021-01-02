package fr.aylan.audio_service.AudioPlayer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;




public class PlayPreviousNotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        Toast.makeText(context, "Play previous", Toast.LENGTH_LONG).show();

        if (MediaService.position == 0) {
            //Tab1Fragment.Companion.highLightItem(MediaService.dataSet.size() - 1, Tab1Fragment.Companion.getList());
            MediaService.position = MediaService.dataSet.size() - 1;
        } else {
            //Tab1Fragment.Companion.highLightItem(MediaService.position - 1, Tab1Fragment.Companion.getList());
            MediaService.position--;
        }

        MediaService.mPlayerAdapter.pause();
        MediaService.mPlayerAdapter.release();
        Intent servIntent = new Intent(context, MediaService.class);
        servIntent.putExtra("to_play", true);
        context.startService(servIntent);

    }

}