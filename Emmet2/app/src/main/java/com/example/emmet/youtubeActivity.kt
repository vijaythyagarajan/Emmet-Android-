package com.example.emmet

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.util.Log
import android.view.ViewGroup
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.youtube.player.YouTubeBaseActivity
import com.google.android.youtube.player.YouTubeInitializationResult
import com.google.android.youtube.player.YouTubePlayer
import com.google.android.youtube.player.YouTubePlayerView
import com.google.android.youtube.player.internal.y
import com.google.android.youtube.player.internal.x
import android.view.WindowManager
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.graphics.drawable.ColorDrawable
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.view.View
import android.widget.LinearLayout


var YOUTUBE_VIDEO_ID = "gkdndnh"
const val YOUTUBE_PLAYLIST_ID = "PLTHOlLMWEwVy2ZNmdrwRlRlVfZ8fiR_ms"

class youtubeActivity : YouTubeBaseActivity(), YouTubePlayer.OnInitializedListener  {


    private val playerView by lazy { YouTubePlayerView(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       // setContentView(R.layout.activity_youtube)
        val layout = layoutInflater.inflate(R.layout.activity_youtube, null) as LinearLayout
        setContentView(layout)

        val window = this.window
        window.setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))

        var v = findViewById(R.id.myLayout) as LinearLayout


        playerView.layoutParams = ConstraintLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )


        v.addView(playerView)

        YOUTUBE_VIDEO_ID = intent.getStringExtra("videoId")


        playerView.initialize(getString(R.string.GOOGLE_API_KEY), this)
    }

    override fun onInitializationSuccess(provider: YouTubePlayer.Provider?, player: YouTubePlayer?, wasRestored: Boolean) {
        Toast.makeText(this, "Initialized YoutubePlayer successfully", Toast.LENGTH_LONG).show()

        if (!wasRestored) {
            player?.loadVideo(YOUTUBE_VIDEO_ID)
            player?.setPlayerStateChangeListener(playerStateListener)
            player?.setPlaybackEventListener(playerPlaybackListener)
        } else {
            player?.play()
        }
    }

    override fun onInitializationFailure(provider: YouTubePlayer.Provider?, result: YouTubeInitializationResult?) {
        if (result?.isUserRecoverableError == true) {
            //result.getErrorDialog(this, DIALOG_REQUEST_CODE).show()
        } else {
            Toast.makeText(this, "The YouTubePlayer initialization error $(result)", Toast.LENGTH_LONG).show()
        }
    }


    private val playerPlaybackListener = object : YouTubePlayer.PlaybackEventListener {
        override fun onPlaying() {
            Toast.makeText(this@youtubeActivity, "OnPlaying", Toast.LENGTH_SHORT).show()
        }

        override fun onPaused() {
            Toast.makeText(this@youtubeActivity, "OnPaused", Toast.LENGTH_SHORT).show()
        }

        override fun onSeekTo(p0: Int) {}

        override fun onBuffering(p0: Boolean) {}

        override fun onStopped() {
            Toast.makeText(this@youtubeActivity, "OnStopped", Toast.LENGTH_SHORT).show()
        }
    }

    private val playerStateListener = object: YouTubePlayer.PlayerStateChangeListener {
        override fun onAdStarted() {
            Toast.makeText(this@youtubeActivity, "Ad started", Toast.LENGTH_SHORT).show()
        }

        override fun onLoading() {}

        override fun onVideoStarted() {
            Toast.makeText(this@youtubeActivity, "Video started", Toast.LENGTH_SHORT).show()
        }

        override fun onLoaded(p0: String?) {}

        override fun onVideoEnded() {
            Toast.makeText(this@youtubeActivity, "Video ended", Toast.LENGTH_SHORT).show()
        }

        override fun onError(p0: YouTubePlayer.ErrorReason?) {}

    }
}
