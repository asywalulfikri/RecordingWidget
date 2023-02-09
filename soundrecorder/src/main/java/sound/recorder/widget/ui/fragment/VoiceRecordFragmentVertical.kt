package sound.recorder.widget.ui.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.room.Room
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import sound.recorder.widget.R
import sound.recorder.widget.base.BaseFragmentWidget
import sound.recorder.widget.databinding.WidgetRecordVerticalBinding
import sound.recorder.widget.db.AppDatabase
import sound.recorder.widget.db.AudioRecord
import sound.recorder.widget.tools.Timer
import sound.recorder.widget.ui.ListingActivityWidgetNew
import sound.recorder.widget.ui.bottomSheet.BottomSheet
import sound.recorder.widget.ui.bottomSheet.BottomSheetListSong
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

private const val LOG_TAG = "AudioRecordTest"
private const val REQUEST_RECORD_AUDIO_PERMISSION = 200

internal class VoiceRecorderFragmentWidgetVertical : BaseFragmentWidget(), BottomSheet.OnClickListener,
    BottomSheetListSong.OnClickListener, Timer.OnTimerUpdateListener {

    private lateinit var fileName: String
    private lateinit var dirPath: String
    private var recorder: MediaRecorder? = null
    private var recording = false
    private var onPause = false
    private var refreshRate : Long = 60
    private lateinit var timer: Timer

    private lateinit var handler: Handler
    private var _binding: WidgetRecordVerticalBinding? = null
    private val binding get() = _binding!!

    // Requesting permission to RECORD_AUDIO
    private var permissionToRecordAccepted = false
    private var permissions: Array<String> = arrayOf(Manifest.permission.RECORD_AUDIO)
    var mp :  MediaPlayer? =null
    var showBtnStop = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = WidgetRecordVerticalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Record to the external cache directory for visibility
        ActivityCompat.requestPermissions(activity as Activity, permissions, REQUEST_RECORD_AUDIO_PERMISSION)
        mp = MediaPlayer()
        setupAds()

        handler = Handler(Looper.myLooper()!!)

        binding.recordBtn.setOnClickListener {
            if(Build.VERSION.SDK_INT> Build.VERSION_CODES.N){
                when {
                    onPause -> resumeRecording()
                    recording -> pauseRecording()
                    else -> startPermission()
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
            startActivity(Intent(activity, ListingActivityWidgetNew::class.java))
        }

        binding.deleteBtn.setOnClickListener {
            stopRecording()
            File(dirPath+fileName).delete()
        }

        binding.songBtn.setOnClickListener {
            startPermissionSong()
        }
        binding.deleteBtn.isClickable = false
    }

    private fun showBottomSheetSong(){
        val bottomSheet = BottomSheetListSong(showBtnStop,this)
        bottomSheet.show(requireActivity().supportFragmentManager, LOG_TAG)
    }

    private fun startPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(activity as Context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                // Pass any permission you want while launching
                requestPermission.launch(Manifest.permission.RECORD_AUDIO)
            }else{
                startRecording()
            }
        }else{
            startRecording()
        }

    }

    private fun startPermissionSong(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(activity as Context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                // Pass any permission you want while launching
                requestPermissionSong.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }else{
                showBottomSheetSong()
            }
        }else{
            showBottomSheetSong()
        }

    }

    private val requestPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            // do something
            if(isGranted){
                startRecording()
            }else{
                showAllowPermission()
            }
        }


    private val requestPermissionSong =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            // do something
            if(isGranted){
                showBottomSheetSong()
            }else{
                showAllowPermission()
            }
        }


    private fun showAllowPermission(){
        if(activity!=null){
            Dexter.withActivity(activity)
                .withPermissions(
                    Manifest.permission.CAMERA
                )
                .withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                        // check if all permissions are granted
                        if (report.areAllPermissionsGranted()) {
                            startRecording()
                        }
                        // check for permanent denial of any permission
                        if (report.isAnyPermissionPermanentlyDenied) {
                            // permission is denied permanently, navigate user to app settings
                            showSettingsDialog(activity)
                        }else{
                            startRecording()
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(permissions: List<PermissionRequest>, token: PermissionToken) {
                        token.continuePermissionRequest()
                    }
                })
                .withErrorListener {
                    Toast.makeText(activity, "Error occurred! ", Toast.LENGTH_SHORT).show()
                }
                .onSameThread()
                .check()
        }

    }



    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionToRecordAccepted = if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        } else {
            false
        }
        //if (!permissionToRecordAccepted) finish()
    }

    @SuppressLint("SimpleDateFormat")
    private fun startRecording(){
        binding.listBtn.visibility = View.GONE
        binding.doneBtn.visibility = View.VISIBLE
        binding.recordText.visibility = View.GONE
        binding.deleteBtn.visibility = View.VISIBLE
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
            setAudioSamplingRate(16000)
            /** END COMMENT **/

            setOutputFile(dirPath+fileName)
            try {
                prepare()
                start()
            } catch (e: IOException) {
                Log.e(LOG_TAG, "prepare() failed")
                setToast(e.message.toString())
            }

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
        if(recorder!=null){
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
    }


    private fun resumeRecording(){
        if(recorder!=null){
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
    }

    @SuppressLint("SetTextI18n")
    private fun stopRecording(){
        if(recorder!=null){
            recording = false
            onPause = false
            recorder?.apply {
                stop()
                release()
            }
            recorder = null
            binding.recordBtn.setImageResource(R.drawable.ic_record)
            binding.recordText.text = "Record"
            binding.recordText.visibility = View.VISIBLE
            binding.listBtn.visibility = View.VISIBLE
            binding.doneBtn.visibility = View.GONE
            binding.deleteBtn.isClickable = false
            binding.deleteBtn.visibility = View.GONE
            binding.deleteBtn.setImageResource(R.drawable.ic_delete_disabled)

            binding.playerView.reset()
            try {
                timer.stop()
            }catch (e: Exception){
                setToast(e.message.toString())
            }

            binding.timerView.text = "00:00.00"
        }
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

    override fun onOkClicked(filePath: String, filename: String,isChange : Boolean) {
        // add audio record info to database
        val db = Room.databaseBuilder(activity as Activity, AppDatabase::class.java, "audioRecords").build()

        val duration = timer.format().split(".")[0]
        stopRecording()

        if(isChange){
            val newFile = File("$dirPath$filename.mp3")
            File(dirPath+fileName).renameTo(newFile)
        }

        GlobalScope.launch {
            db.audioRecordDAO().insert(AudioRecord(filename, filePath, Date().time, duration))
        }

        Toast.makeText(activity,"Successfully saved the recording",Toast.LENGTH_LONG).show()
        binding.recordText.visibility = View.VISIBLE
        binding.recordText.text = "Record"
        showInterstitial()

    }

    override fun onPlaySong(filePath: String) {
        if (mp != null) {
            releaseMediaPlayer()
        }
        mp = MediaPlayer.create(activity, Uri.parse(filePath))
        if (mp != null) {
            showBtnStop = true
            mp?.start()
        }
    }


    override fun onPause() {
        super.onPause()
        if (mp != null) {
            mp?.release()
            showBtnStop = false
        }

        if(recording){
            stopRecording()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mp != null) {
            mp?.release()
            showBtnStop = false
        }

        if(recording){
            stopRecording()
        }
    }

    override fun onStopSong() {
        releaseMediaPlayer()
    }

    private fun releaseMediaPlayer() {
        mp?.release()
        mp = null
        showBtnStop = false
    }


    override fun onTimerUpdate(duration: String) {
        activity?.runOnUiThread{
            if(recording)
                binding.timerView.text = duration
        }
    }

}