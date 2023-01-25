package sound.recorder.widget

import android.media.MediaPlayer
import android.media.PlaybackParams
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.SeekBar
import androidx.core.content.res.ResourcesCompat
import sound.recorder.widget.databinding.ActivityPlayerBinding

internal class PlayerActivity : AppCompatActivity() {

    private val delay = 100L
    private lateinit var runnable : Runnable
    private lateinit var handler : Handler
    private lateinit var mediaPlayer : MediaPlayer
    private var playbackSpeed :Float = 1.0f
    private lateinit var binding: ActivityPlayerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        val filePath = intent.getStringExtra("filepath")
        val filename = intent.getStringExtra("filename")

        binding.tvFilename.text = filename

        mediaPlayer = MediaPlayer()
        mediaPlayer.apply {
            setDataSource(filePath)
            prepare()
        }
        binding.seekBar.max = mediaPlayer.duration

        handler = Handler(Looper.getMainLooper())
        playPausePlayer()

        mediaPlayer.setOnCompletionListener {
            stopPlayer()
        }

        binding.btnPlay.setOnClickListener {
            playPausePlayer()
        }

        binding.btnForward.setOnClickListener {
            mediaPlayer.seekTo(mediaPlayer.currentPosition + 1000)
            binding.seekBar.progress += 1000
        }

        binding.btnBackward.setOnClickListener {
            mediaPlayer.seekTo(mediaPlayer.currentPosition - 1000)
            binding.seekBar.progress -= 1000

        }

        binding.seekBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                if(p2) mediaPlayer.seekTo(p1)
            }
            override fun onStartTrackingTouch(p0: SeekBar?) {}

            override fun onStopTrackingTouch(p0: SeekBar?) {}

        })

        binding.chip.setOnClickListener {
            when(playbackSpeed){
                0.5f -> playbackSpeed += 0.5f
                1.0f -> playbackSpeed += 0.5f
                1.5f -> playbackSpeed += 0.5f
                2.0f -> playbackSpeed = 0.5f
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mediaPlayer.playbackParams = PlaybackParams().setSpeed(playbackSpeed)
            }
            binding.chip.text = "x $playbackSpeed"
        }
    }

    private fun playPausePlayer(){
        if(!mediaPlayer.isPlaying){
            mediaPlayer.start()
            binding.btnPlay.background = ResourcesCompat.getDrawable(resources, R.drawable.ic_pause_circle, theme)

            runnable = Runnable {
                val progress = mediaPlayer.currentPosition
                Log.d("progress", progress.toString())
                binding.seekBar.progress = progress

                val amp = 80 + Math.random()*300
                binding.playerView.updateAmps(amp.toInt())

                handler.postDelayed(runnable, delay)
            }
            handler.postDelayed(runnable, delay)
        }else{
            mediaPlayer.pause()
            binding.btnPlay.background = ResourcesCompat.getDrawable(resources, R.drawable.ic_play_circle, theme)

            handler.removeCallbacks(runnable)
        }
    }

    private fun stopPlayer(){
        binding.btnPlay.background = ResourcesCompat.getDrawable(resources, R.drawable.ic_play_circle, theme)
        handler.removeCallbacks(runnable)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        mediaPlayer.stop()
        mediaPlayer.release()
        handler.removeCallbacks(runnable)
    }
}