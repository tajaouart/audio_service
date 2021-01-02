package fr.aylan.audio_service.AudioPlayer;

import android.content.Context
import android.media.MediaPlayer
import android.os.Build
import androidx.annotation.RequiresApi
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

/**
 * Exposes the functionality of the [MediaPlayer] and implements the [PlayerAdapter]
 * so that [Home] can control music playback.
 */
class MediaPlayerHolder(context: Context) : PlayerAdapter {

    override fun getDuration(): Int? {
        return mMediaPlayer?.duration
    }

    override fun getCurrentTime(): Int? {
        return mMediaPlayer?.currentPosition
    }

    private val mContext: Context = context.applicationContext
    private var mMediaPlayer: MediaPlayer? = null

    //private var mResourceId: Int = 0
    private var mPlaybackInfoListener: PlaybackInfoListener? = null
    private var mExecutor: ScheduledExecutorService? = null
    private var mSeekbarPositionUpdateTask: Runnable? = null
    private var url: String = ""

    override val isPlaying: Boolean
        get() = mMediaPlayer?.isPlaying ?: false

    /**
     * Once the [MediaPlayer] is released, it can't be used again, and another one has to be
     * created. In the onStop() method of the [Home] the [MediaPlayer] is
     * released. Then in the onStart() of the [Home] a new [MediaPlayer]
     * object has to be created. That's why this method is private, and called by load(int) and
     * not the constructor.
     */
    private fun initializeMediaPlayer() {
        if (mMediaPlayer == null) {
            mMediaPlayer = MediaPlayer()
            mMediaPlayer!!.setOnCompletionListener {
                stopUpdatingCallbackWithPosition(true)
                logToUI("MediaPlayer playback completed")
                MediaService.PlayingFinished(mContext)
                if (mPlaybackInfoListener != null) {
                    mPlaybackInfoListener!!.onStateChanged(PlaybackInfoListener.State.COMPLETED)
                    mPlaybackInfoListener!!.onPlaybackCompleted()
                }
            }
            logToUI("mMediaPlayer = new MediaPlayer()")
        }
    }

    fun setPlaybackInfoListener(listener: PlaybackInfoListener) {
        mPlaybackInfoListener = listener
    }

    // Implements PlaybackControl.
    @RequiresApi(Build.VERSION_CODES.N)
    override fun loadMedia(/*resourceId: Int*/ url: String) {
        //mResourceId = resourceId

        initializeMediaPlayer()

        //val assetFileDescriptor = mContext.resources.openRawResourceFd(mResourceId)
        try {
            logToUI("load() {1. setDataSource}")
            mMediaPlayer!!.setDataSource(url)
        } catch (e: Exception) {
            logToUI(e.toString())
        }

        try {
            logToUI("load() {2. prepare}")
            mMediaPlayer!!.prepare()
        } catch (e: Exception) {
            logToUI(e.toString())
        }

        initializeProgressCallback()
        logToUI("initializeProgressCallback()")
    }

    override fun release() {
        if (mMediaPlayer != null) {
            logToUI("release() and mMediaPlayer = null")
            mMediaPlayer!!.release()
            mMediaPlayer = null
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun play(url: String) {
        this.url = url
        loadMedia(/*/mResourceId*/url)
        if (mMediaPlayer != null && !mMediaPlayer!!.isPlaying) {
            //logToUI(String.format("playbackStart() %s",
            // mContext.resources.getResourceEntryName(mResourceId)))
            mMediaPlayer!!.start()
            if (mPlaybackInfoListener != null) {
                mPlaybackInfoListener!!.onStateChanged(PlaybackInfoListener.State.PLAYING)
                //mPlayButton.background = mContext.getDrawable(R.drawable.ic_pause_black_24dp)

            }
            startUpdatingCallbackWithPosition()
        }
    }


    @RequiresApi(Build.VERSION_CODES.N)
    override fun reset(url: String) {
        if (mMediaPlayer != null) {
            //logToUI("playbackReset()")
            mMediaPlayer!!.reset()
            loadMedia(/*mResourceId*/url)
            if (mPlaybackInfoListener != null) {
                mPlaybackInfoListener!!.onStateChanged(PlaybackInfoListener.State.RESET)
            }
            stopUpdatingCallbackWithPosition(true)
        }
    }

    override fun pause() {
        if (mMediaPlayer != null && mMediaPlayer!!.isPlaying) {
            mMediaPlayer!!.pause()
            if (mPlaybackInfoListener != null) {
                mPlaybackInfoListener!!.onStateChanged(PlaybackInfoListener.State.PAUSED)
            }
            logToUI("playbackPause()")
        }
    }

    override fun seekTo(position: Int) {
        if (mMediaPlayer != null) {
            //logToUI(String.format("seekTo() %d ms", position))
            mMediaPlayer!!.seekTo(position)
        }
    }

    /**
     * Syncs the mMediaPlayer position with mPlaybackProgressCallback via recurring task.
     */
    private fun startUpdatingCallbackWithPosition() {
        if (mExecutor == null) {
            mExecutor = Executors.newSingleThreadScheduledExecutor()
        }
        if (mSeekbarPositionUpdateTask == null) {
            mSeekbarPositionUpdateTask = Runnable { updateProgressCallbackTask() }
        }
        mExecutor!!.scheduleAtFixedRate(
                mSeekbarPositionUpdateTask!!,
                0,
                PLAYBACK_POSITION_REFRESH_INTERVAL_MS.toLong(),
                TimeUnit.MILLISECONDS
        )
    }

    // Reports media playback position to mPlaybackProgressCallback.
    private fun stopUpdatingCallbackWithPosition(resetUIPlaybackPosition: Boolean) {
        if (mExecutor != null) {
            mExecutor!!.shutdownNow()
            mExecutor = null
            mSeekbarPositionUpdateTask = null
            if (resetUIPlaybackPosition && mPlaybackInfoListener != null) {
                mPlaybackInfoListener!!.onPositionChanged(0)
            }
        }
    }

    private fun updateProgressCallbackTask() {
        if (mMediaPlayer != null && mMediaPlayer!!.isPlaying) {
            val currentPosition = mMediaPlayer!!.currentPosition
            if (mPlaybackInfoListener != null) {
                mPlaybackInfoListener!!.onPositionChanged(currentPosition)
            }
        }
    }

    override fun initializeProgressCallback() {
        val duration = mMediaPlayer!!.duration
        if (mPlaybackInfoListener != null) {
            mPlaybackInfoListener!!.onDurationChanged(duration)
            mPlaybackInfoListener!!.onPositionChanged(0)
            logToUI(String.format("firing setPlaybackDuration(%d sec)",
                    TimeUnit.MILLISECONDS.toSeconds(duration.toLong())))
            logToUI("firing setPlaybackPosition(0)")
        }
    }

    private fun logToUI(message: String) {
        if (mPlaybackInfoListener != null) {
            mPlaybackInfoListener!!.onLogUpdated(message)
        }
    }

    companion object {

        const val PLAYBACK_POSITION_REFRESH_INTERVAL_MS = 1000
    }

}