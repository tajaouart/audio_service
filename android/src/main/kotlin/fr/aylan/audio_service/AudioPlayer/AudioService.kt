
package fr.aylan.audio_service.AudioPlayer;

import android.content.Context
import android.content.Intent
import java.util.*

class AudioService {

    fun getDuration(): Int? {
        return MediaService.mPlayerAdapter.getDuration()
    }

    fun getCurrentTime(): Int? {
        return MediaService.mPlayerAdapter.getCurrentTime()
    }


    fun setPlaybackInfoListener() {

    }


    fun loadMedia(url: String) {
    }

    fun releasePlayer() {
        MediaService.mPlayerAdapter.release()
    }

    fun playOrPause() {

        val servIntent = Intent(_getContext(), MediaService::class.java)
        servIntent.putExtra("to_play", !MediaService.mPlayerAdapter.isPlaying)
        _getContext().startService(servIntent)

    }

    fun previous(){
        if (MediaService.position == 0) {
            list?.let { highLightItem(MediaService.dataSet.size - 1, it) }
            MediaService.position = MediaService.dataSet.size - 1
        } else {
            list?.let { highLightItem(MediaService.position - 1, it) }
            MediaService.position--
        }


        MediaService.mPlayerAdapter.pause()
        MediaService.mPlayerAdapter.release()
        val servIntent = Intent(_getContext(), MediaService::class.java)
        servIntent.putExtra("to_play", true)
        _getContext().startService(servIntent)
    }

    fun next(){
        if (MediaService.position == MediaService.dataSet.size - 1) {
            list?.let { highLightItem(0, it) }
            MediaService.position = 0
        } else {
            list?.let { highLightItem(MediaService.position + 1, it) }
            MediaService.position++
        }


        MediaService.mPlayerAdapter.pause()
        MediaService.mPlayerAdapter.release()
        val servIntent = Intent(_getContext(), MediaService::class.java)
        servIntent.putExtra("to_play", true)
        _getContext().startService(servIntent)
    }

    fun _getContext(): Context{
        return App.getInstance().applicationContext
    }


    fun quitNotificationAndService() {
        MediaService.quit(_getContext())
    }

    fun isPlaying():Boolean {
        return MediaService.mPlayerAdapter.isPlaying
    }

    fun seekTo(position: Int){
        MediaService.mPlayerAdapter.seekTo(position)
    }

    fun initializeProgressCallback() {
    }

    fun setDataSet(dataSet: ArrayList<PlayListItem?>) {
        MediaService.dataSet = dataSet
    }


}