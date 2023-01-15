package sound.recorder.widget

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.room.Room
import sound.recorder.widget.db.AppDatabase
import sound.recorder.widget.db.AudioRecord
import sound.recorder.widget.tools.Timer
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import sound.recorder.widget.databinding.WidgetRecordHorizontalBinding
import java.io.File
import java.io.IOException
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

private const val LOG_TAG = "AudioRecordTest"
private const val REQUEST_RECORD_AUDIO_PERMISSION = 200

internal class VoiceRecorderFragmentHorizontal : Fragment(), BottomSheet.OnClickListener, Timer.OnTimerUpdateListener {

    private lateinit var fileName: String
    private lateinit var dirPath: String
    private var recorder: MediaRecorder? = null
    private var recording = false
    private var onPause = false
    private var refreshRate : Long = 60
    private lateinit var timer: Timer

    private lateinit var handler: Handler
    private var _binding: WidgetRecordHorizontalBinding? = null
    private val binding get() = _binding!!

    // Requesting permission to RECORD_AUDIO
    private var permissionToRecordAccepted = false
    private var permissions: Array<String> = arrayOf(Manifest.permission.RECORD_AUDIO)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = WidgetRecordHorizontalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Record to the external cache directory for visibility
        ActivityCompat.requestPermissions(activity as Activity, permissions, REQUEST_RECORD_AUDIO_PERMISSION)

        handler = Handler(Looper.myLooper()!!)

        binding.recordBtn.setOnClickListener {
            if(Build.VERSION.SDK_INT> Build.VERSION_CODES.N){
                when {
                    onPause -> resumeRecording()
                    recording -> pauseRecording()
                    else -> startRecording()
                }
            }else{
                setToast("Your device not support to record audio")
            }
        }

        binding.doneBtn.setOnClickListener {
            stopRecording()
            showBottomSheet()
        }

        binding.listBtn.setOnClickListener {
            startActivity(Intent(activity, ListingActivity::class.java))
        }

        binding.deleteBtn.setOnClickListener {
            stopRecording()
            File(dirPath+fileName).delete()
        }
        binding.deleteBtn.isClickable = false
    }

    private fun setToast(message : String){
        Toast.makeText(activity,message,Toast.LENGTH_SHORT).show()
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionToRecordAccepted = if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        } else {
            false
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun startRecording(){
        binding.listBtn.visibility = View.GONE
        binding.doneBtn.visibility = View.VISIBLE
        binding.recordText.visibility = View.GONE
        binding.deleteBtn.isClickable = true
        binding.deleteBtn.setImageResource(R.drawable.ic_delete_enabled)

        recording = true
        timer = Timer(this)
        timer.start()

        // format file name with date
        val pattern = "yyyy.MM.dd_hh.mm.ss"
        val simpleDateFormat = SimpleDateFormat(pattern)
        val date: String = simpleDateFormat.format(Date())

        dirPath = "${activity?.externalCacheDir?.absolutePath}/"
        fileName = "record_${date}.mp3"

        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            /** START COMMENT
             * These two together enable saving file into mp3 format
             * because android doesn't support mp3 saving explicitly **/
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            /** END COMMENT **/

            setOutputFile(dirPath+fileName)
            try {
                prepare()
            } catch (e: IOException) {
                Log.e(LOG_TAG, "prepare() failed")
            }

            start()
        }

        binding.recordBtn.setImageResource(R.drawable.ic_pause)

        animatePlayerView()

    }

    private fun animatePlayerView(){
        if(recording && !onPause){
            val amp = recorder!!.maxAmplitude
            binding.playerView.updateAmps(amp)

            // write maxmap to a file for visualization in player activity

            handler.postDelayed(
                {
                    kotlin.run { animatePlayerView() }
                }, refreshRate
            )
        }
    }

    private fun pauseRecording(){
        binding.recordText.visibility = View.VISIBLE
        binding.recordText.text = "Continue"
        onPause = true
        recorder?.apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                pause()
            }
        }
        binding.recordBtn.setImageResource(R.drawable.ic_record)
        timer.pause()

    }


    private fun resumeRecording(){
        binding.recordText.visibility = View.GONE
        onPause = false
        recorder?.apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                resume()
            }
        }
        binding.recordBtn.setImageResource(R.drawable.ic_pause)
        animatePlayerView()
        timer.start()
    }

    @SuppressLint("SetTextI18n")
    private fun stopRecording(){
        recording = false
        onPause = false
        recorder?.apply {
            stop()
            release()
        }
        recorder = null
        binding.recordBtn.setImageResource(R.drawable.ic_record)

        binding.listBtn.visibility = View.VISIBLE
        binding.doneBtn.visibility = View.GONE
        binding.deleteBtn.isClickable = false
        binding.deleteBtn.setImageResource(R.drawable.ic_delete_disabled)

        binding.playerView.reset()
        try {
            timer.stop()
        }catch (e: Exception){}

        binding.timerView.text = "00:00.00"
    }

    private fun showBottomSheet(){
        val bottomSheet = BottomSheet(dirPath, fileName, this)
        bottomSheet.show(requireActivity().supportFragmentManager, LOG_TAG)
    }



    override fun onCancelClicked() {
        Toast.makeText(activity, "Audio record deleted", Toast.LENGTH_SHORT).show()
        binding.recordText.text = "Record"
        binding.recordText.visibility = View.VISIBLE
        stopRecording()
    }

    override fun onOkClicked(filePath: String, filename: String) {
        // add audio record info to database
        val db = Room.databaseBuilder(
            activity as Activity,
            AppDatabase::class.java,
            "audioRecords").build()

        val duration = timer.format().split(".")[0]
        stopRecording()

        GlobalScope.launch {
            db.audioRecordDAO().insert(AudioRecord(filename, filePath, Date().time, duration))
        }

        binding.recordText.visibility = View.VISIBLE
        binding.recordText.text = "Record"

    }

    override fun onTimerUpdate(duration: String) {
        activity?.runOnUiThread{
            if(recording)
                binding.timerView.text = duration
        }
    }
}