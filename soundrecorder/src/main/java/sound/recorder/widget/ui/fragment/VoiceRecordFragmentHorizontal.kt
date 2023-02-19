package sound.recorder.widget.ui.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.media.ToneGenerator
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startForegroundService
import androidx.room.Room
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import sound.recorder.widget.R
import sound.recorder.widget.base.BaseFragmentWidget
import sound.recorder.widget.databinding.WidgetRecordHorizontalBinding
import sound.recorder.widget.db.AppDatabase
import sound.recorder.widget.db.AudioRecord
import sound.recorder.widget.service.BackgroundService
import sound.recorder.widget.tools.Timer
import sound.recorder.widget.ui.ListingActivityWidgetNew
import sound.recorder.widget.ui.bottomSheet.BottomSheet
import sound.recorder.widget.ui.bottomSheet.BottomSheetListSong
import sound.recorder.widget.ui.bottomSheet.BottomSheetSetting
import sound.recorder.widget.util.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.ln

private const val LOG_TAG = "AudioRecordTest"
private const val REQUEST_RECORD_AUDIO_PERMISSION = 200

internal class VoiceRecorderFragmentWidgetHorizontal : BaseFragmentWidget(), BottomSheet.OnClickListener,
    BottomSheetListSong.OnClickListener, Timer.OnTimerUpdateListener,SharedPreferences.OnSharedPreferenceChangeListener {

    private lateinit var fileName: String
    private lateinit var dirPath: String
    private var recorder: MediaRecorder? = null
    private var recordingAudio = false
    private var pauseRecordAudio = false
    private var refreshRate : Long = 60
    private lateinit var timer: Timer

    private lateinit var handler: Handler
    private var _binding: WidgetRecordHorizontalBinding? = null
    private val binding get() = _binding!!

    // Requesting permission to RECORD_AUDIO
    private var permissionToRecordAccepted = false
    private var permissions: Array<String> = arrayOf(Manifest.permission.RECORD_AUDIO)
    private var mp :  MediaPlayer? =null
    private var showBtnStop = false
    private var songIsPlaying = false


    //ScreenRecorder
    var screenRecorder: ScreenRecorder? =null
    var recordingScreen = false
    var pauseRecordScreen = false
    private var sharedPreferences : SharedPreferences? =null
    private var volume : Float? =null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = WidgetRecordHorizontalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Record to the external cache directory for visibility
        if(activity!=null){
            ActivityCompat.requestPermissions(activity as Activity, permissions, REQUEST_RECORD_AUDIO_PERMISSION)

            sharedPreferences = DataSession(requireContext()).getShared()
            sharedPreferences?.registerOnSharedPreferenceChangeListener(this)

            mp = MediaPlayer()
            val progress = sharedPreferences?.getInt(Constant.keyShared.volume,100)
            volume = (1 - ln((ToneGenerator.MAX_VOLUME - progress!!).toDouble()) / ln(
                ToneGenerator.MAX_VOLUME.toDouble())).toFloat()

            setupAds()

            //setupScreenRecorder()

            handler = Handler(Looper.myLooper()!!)

            binding.recordBtn.setOnClickListener {
                if(Build.VERSION.SDK_INT> Build.VERSION_CODES.N){
                    when {
                        pauseRecordAudio -> resumeRecordingAudio()
                        recordingAudio -> pauseRecordingAudio()
                        else -> startPermission()
                    }
                }else{
                    setToast("Your device not support to record audio")
                }
            }

            binding.doneBtn.setOnClickListener {
                stopRecordingAudio()
                showBottomSheet()
            }

            binding.listBtn.setOnClickListener {
                startActivity(Intent(activity, ListingActivityWidgetNew::class.java))
            }

            binding.deleteBtn.setOnClickListener {
                stopRecordingAudio()
                File(dirPath+fileName).delete()
            }

            binding.songBtn.setOnClickListener {
                startPermissionSong()
            }

            binding.deleteBtn.isClickable = false

            binding.settingBtn.setOnClickListener {
                val bottomSheet = BottomSheetSetting()
                bottomSheet.show(requireActivity().supportFragmentManager, LOG_TAG)
            }
        }
    }

    private fun setupScreenRecorder(){

        val intent = Intent(requireContext(), BackgroundService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(requireContext(),intent)
        } else {
            activity?.startService(intent)
        }

        val metrics = DisplayMetrics()
        activity?.windowManager?.defaultDisplay?.getMetrics(metrics)
        screenRecorder = ScreenRecorder(requireActivity().application).apply {
            width = metrics.widthPixels
            height = metrics.heightPixels
            dpi = metrics.densityDpi
        }
    }


    private fun showDialogRecord() {
        // custom dialog
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_alert)
        dialog.setCancelable(true)

        // set the custom dialog components - text, image and button
        val btnAudio = dialog.findViewById<View>(R.id.btn_primary) as Button
        val btnScreenAudio = dialog.findViewById<View>(R.id.btn_cancel) as Button

        btnAudio.setOnClickListener {
            startRecordingAudio()
            dialog.dismiss()
        }


        btnScreenAudio.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ContextCompat.checkSelfPermission(activity as Context, Manifest.permission.FOREGROUND_SERVICE) != PackageManager.PERMISSION_GRANTED) {
                    // Pass any permission you want while launching
                    requestPermissionForeGround.launch(Manifest.permission.FOREGROUND_SERVICE)
                } else {
                    recordingScreen = true
                    startScreenRecorder()
                    dialog.dismiss()
                }
            }
        }
        dialog.show()
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
                startRecordingAudio()
               // showDialogRecord()
            }
        }else{
            startRecordingAudio()
            //showDialogRecord()
        }

    }

    private fun startPermissionSong(){
        if(Build.VERSION.SDK_INT >=33){

            showBottomSheetSong()

        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
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
                showDialogRecord()
            }else{
                showAllowPermission()
            }
        }


    private val requestPermissionForeGround =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            // do something
            if(isGranted){
                screenRecorder?.start(this,requireContext())
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
        setToast("Allow Permission in Setting")
    }



    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionToRecordAccepted = if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        } else {
            false
        }
    }


    private fun startScreenRecorder(){
        screenRecorder?.start(this,requireContext())
        showLayoutStartRecord()

    }

    private fun showLayoutStartRecord(){
        binding.listBtn.visibility = View.GONE
        binding.doneBtn.visibility = View.VISIBLE
        binding.recordText.visibility = View.GONE
        binding.deleteBtn.visibility = View.VISIBLE
        binding.deleteBtn.isClickable = true
       // binding.deleteBtn.setImageResource(R.drawable.ic_delete_enabled)
        binding.recordBtn.setImageResource(R.drawable.ic_pause)

        timer = Timer(this)
        timer.start()
    }


    private fun showLayoutPauseRecord(){
        binding.recordText.visibility = View.VISIBLE
        binding.recordText.text = "Continue"
        binding.recordBtn.setImageResource(R.drawable.ic_record)
        timer.pause()
    }

    private fun showLayoutStopRecord(){
        binding.recordBtn.setImageResource(R.drawable.ic_record)
        binding.recordText.text = "Record"
        binding.recordText.visibility = View.VISIBLE
        binding.listBtn.visibility = View.VISIBLE
        binding.doneBtn.visibility = View.GONE
        binding.deleteBtn.isClickable = false
        binding.deleteBtn.visibility = View.GONE

        binding.playerView.reset()
        try {
            timer.stop()
        }catch (e: Exception){
            setToast(e.message.toString())
        }

        binding.timerView.text = "00:00.00"
    }

    @SuppressLint("SimpleDateFormat")
    private fun startRecordingAudio(){
        showLayoutStartRecord()

        recordingAudio = true

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

        animatePlayerView()

    }


    private fun animatePlayerView(){
        if(recordingAudio && !pauseRecordAudio){
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //screenRecorder?.onActivityResult(requestCode, resultCode, data,requireContext())
    }


    private fun pauseRecordingAudio(){
        if(recorder!=null&&recordingAudio){
            showLayoutPauseRecord()
            pauseRecordAudio = true
            recorder?.apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    pause()
                }
            }
        }
    }

    private fun resumeRecordingAudio(){
        if(recorder!=null&&pauseRecordAudio){
            binding.recordText.visibility = View.GONE
            pauseRecordAudio = false
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

    private fun stopRecordingAudio(){
        if(recorder!=null&&recordingAudio){
            recordingAudio = false
            pauseRecordAudio= false
            recorder?.apply {
                stop()
                release()
            }
            recorder = null
            showLayoutStopRecord()

        }
    }


    @SuppressLint("SetTextI18n")
    private fun pauseRecordingScreen(){
        if(screenRecorder!=null){
            showLayoutPauseRecord()
            pauseRecordScreen = true
            screenRecorder?.apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    pause()
                }
            }
        }
    }


    private fun resumeRecordingScreen(){
        if(screenRecorder!=null){
            binding.recordText.visibility = View.GONE
            pauseRecordScreen = false
            screenRecorder?.apply {
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
    private fun stopRecordingScreen(){
        if(screenRecorder!=null&&screenRecorder?.isRecording==true){
            recordingScreen = false
            pauseRecordScreen= false
            screenRecorder?.apply {
                stop()
                reset()
            }
            recorder = null
            showLayoutStopRecord()

        }
    }

    private fun showBottomSheet(){
        val bottomSheet = BottomSheet(dirPath, fileName, this)
        bottomSheet.show(requireActivity().supportFragmentManager, LOG_TAG)
    }



    @SuppressLint("SetTextI18n")
    override fun onCancelClicked() {
        Toast.makeText(activity, "Audio record deleted", Toast.LENGTH_SHORT).show()
        binding.recordText.text = "Record"
        binding.recordText.visibility = View.VISIBLE

        stopRecordingAudio()
    }

    @SuppressLint("SetTextI18n")
    override fun onOkClicked(filePath: String, filename: String, isChange : Boolean) {
        // add audio record info to database
        val db = Room.databaseBuilder(activity as Activity, AppDatabase::class.java, "audioRecords").build()

        val duration = timer.format().split(".")[0]

        stopRecordingAudio()

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
            volume?.let { mp?.setVolume(it, volume!!) }
            mp?.start()
            songIsPlaying = true
        }
    }


    override fun onPause() {
        super.onPause()
        sharedPreferences?.unregisterOnSharedPreferenceChangeListener(this)
        if (mp != null) {
            mp?.release()
            showBtnStop = false
            songIsPlaying = false
        }

        if(recorder!=null&&recordingAudio){
            recordingAudio = false
            pauseRecordAudio= false
            recorder?.apply {
                release()
            }
            recorder = null
            showLayoutStopRecord()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mp != null) {
            mp?.release()
            showBtnStop = false
            songIsPlaying = false
        }
        if(recorder!=null&&recordingAudio){
            recordingAudio = false
            pauseRecordAudio= false
            recorder?.apply {
                release()
            }
            recorder = null
            showLayoutStopRecord()
        }
    }

    override fun onStopSong() {
        releaseMediaPlayer()
    }

    private fun releaseMediaPlayer() {
        mp?.release()
        mp = null
        songIsPlaying = false
        showBtnStop = false
    }


    override fun onTimerUpdate(duration: String) {
        activity?.runOnUiThread{
            if(recordingAudio)
                binding.timerView.text = duration
        }
    }


    override fun onResume() {
        super.onResume()
        sharedPreferences?.registerOnSharedPreferenceChangeListener(this)
    }



    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if(key==Constant.keyShared.volume){
            if(mp!=null){
                val progress = sharedPreferences?.getInt(Constant.keyShared.volume,100)
                val volume = (1 - ln((ToneGenerator.MAX_VOLUME - progress!!).toDouble()) / ln(
                    ToneGenerator.MAX_VOLUME.toDouble())).toFloat()
                if(songIsPlaying){
                    mp?.setVolume(volume,volume)
                }
            }
        }
    }

}