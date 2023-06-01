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
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.room.Room
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import sound.recorder.widget.R
import sound.recorder.widget.databinding.WidgetRecordHorizontalBlackBinding
import sound.recorder.widget.db.AppDatabase
import sound.recorder.widget.db.AudioRecord
import sound.recorder.widget.tools.Timer
import sound.recorder.widget.ui.activity.ListingMusicActivity
import sound.recorder.widget.ui.bottomSheet.BottomSheet
import sound.recorder.widget.ui.bottomSheet.BottomSheetListSong
import sound.recorder.widget.ui.bottomSheet.BottomSheetNote
import sound.recorder.widget.ui.bottomSheet.BottomSheetSetting
import sound.recorder.widget.util.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.ln

private const val LOG_TAG = "AudioRecordTest"

class VoiceRecorderFragmentWidgetHorizontalBlack : Fragment, BottomSheet.OnClickListener,
    BottomSheetListSong.OnClickListener, Timer.OnTimerUpdateListener,SharedPreferences.OnSharedPreferenceChangeListener {

    private var fileName =  ""
    private var dirPath = ""
    private var recorder: MediaRecorder? = null
    private var recordingAudio = false
    private var pauseRecordAudio = false
    private var refreshRate : Long = 60
    private lateinit var timer: Timer

    private lateinit var handler: Handler
    private var _binding: WidgetRecordHorizontalBlackBinding? = null
    private val binding get() = _binding!!

    private var mp :  MediaPlayer? =null
    private var showBtnStop = false
    private var songIsPlaying = false
    var mInterstitialAd: InterstitialAd? = null
    var rewardedAd : RewardedAd? =null
    private var isLoadInterstitial = false
    private var isLoadReward = false

    //ScreenRecorder
    var screenRecorder: ScreenRecorder? =null
    var recordingScreen = false
    var pauseRecordScreen = false
    private var sharedPreferences : SharedPreferences? =null
    private var volumes : Float? =null
    private var showNote : Boolean? =null

    constructor() : super() {
        // Required empty public constructor
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = WidgetRecordHorizontalBlackBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Record to the external cache directory for visibility
        if(activity!=null){

            sharedPreferences = DataSession(requireActivity()).getShared()
            sharedPreferences?.registerOnSharedPreferenceChangeListener(this)
            showNote = DataSession(requireActivity()).getShowNote()

            val progress = sharedPreferences?.getInt(Constant.keyShared.volume,100)
            volumes = (1 - ln((ToneGenerator.MAX_VOLUME - progress!!).toDouble()) / ln(
                ToneGenerator.MAX_VOLUME.toDouble())).toFloat()

            setupInterstitial()
            setupReward()

            if(showNote==true){
                binding.noteBtn.visibility = View.VISIBLE
            }else{
                binding.noteBtn.visibility = View.GONE
            }

            handler = Handler(Looper.myLooper()!!)

            binding.recordBtn.setOnClickListener {
                if(Build.VERSION.SDK_INT> Build.VERSION_CODES.N){
                    when {
                        pauseRecordAudio -> resumeRecordingAudio()
                        recordingAudio -> pauseRecordingAudio()
                        else -> startPermission()
                    }
                }else{
                    setToastError(activity,"Your device not support to record audio")
                }
            }

            binding.doneBtn.setOnClickListener {
                stopRecordingAudio("")
                showBottomSheet()
            }

            binding.listBtn.setOnClickListener {
                startActivity(Intent(activity, ListingMusicActivity::class.java))
            }

            binding.deleteBtn.setOnClickListener {
                stopRecordingAudio("The recording has been cancelled")
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

            binding.noteBtn.setOnClickListener {
                val bottomSheet = BottomSheetNote()
                bottomSheet.show(requireActivity().supportFragmentManager, LOG_TAG)
            }
        }
    }

    private fun showDialogRecord() {
        // custom dialog
        val dialog = Dialog(requireActivity())
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


        btnScreenAudio.visibility = View.GONE
        btnScreenAudio.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ContextCompat.checkSelfPermission(activity as Context, Manifest.permission.FOREGROUND_SERVICE) != PackageManager.PERMISSION_GRANTED) {
                    // Pass any permission you want while launching
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        requestPermissionForeGround.launch(Manifest.permission.FOREGROUND_SERVICE)
                    }
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
        if(activity!=null){
            val bottomSheet = BottomSheetListSong(showBtnStop,this)
            bottomSheet.show(requireActivity().supportFragmentManager, LOG_TAG)
        }
    }

    private fun startPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                // Pass any permission you want while launching
                requestPermission.launch(Manifest.permission.RECORD_AUDIO)
            }else{
                startRecordingAudio()

            }
        }else{
            startRecordingAudio()
        }

    }

    private fun startPermissionSong(){
        if(Build.VERSION.SDK_INT >=Build.VERSION_CODES.TIRAMISU){
            if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                showBottomSheetSong()
            } else {
                requestPermissionSong.launch(Manifest.permission.READ_MEDIA_AUDIO)
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(activity as Context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
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
                startRecordingAudio()
            }else{
                showAllowPermission()
            }
        }


    private val requestPermissionForeGround =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            // do something
            if(isGranted){
                screenRecorder?.start(this,requireActivity())
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
        setToastInfo(activity,"Allow Permission in Setting Audio In Setting")
    }


    private fun startScreenRecorder(){
        screenRecorder?.start(this,requireActivity())
        showLayoutStartRecord()

    }

    private fun showLayoutStartRecord(){
        binding.listBtn.visibility = View.GONE
        binding.doneBtn.visibility = View.VISIBLE
        binding.recordText.visibility = View.GONE
        binding.deleteBtn.visibility = View.VISIBLE
        binding.deleteBtn.isClickable = true
        binding.recordBtn.setImageResource(R.drawable.ic_pause)

        timer = Timer(this)
        timer.start()
    }


    @SuppressLint("SetTextI18n")
    private fun showLayoutPauseRecord(){
        binding.recordText.visibility = View.VISIBLE
        binding.recordText.text = "Continue"
        binding.recordBtn.setImageResource(R.drawable.transparant_bg)
        timer.pause()
    }

    @SuppressLint("SetTextI18n")
    private fun showLayoutStopRecord(){
        binding.recordBtn.setImageResource(R.drawable.transparant_bg)
        binding.recordText.text = "Record"
        binding.recordText.visibility = View.VISIBLE
        binding.listBtn.visibility = View.VISIBLE
        binding.doneBtn.visibility = View.GONE
        binding.deleteBtn.isClickable = false
        binding.deleteBtn.visibility = View.GONE

        binding.playerView.reset()
        try {
            timer.stop()
        }catch (e: IllegalStateException) {
            // Handle IllegalStateException (e.g., recording already started)
            e.printStackTrace()
            setToastError(activity,e.message.toString())

            // Perform error handling or show appropriate message to the user
        } catch (e: IOException) {
            // Handle IOException (e.g., failed to prepare or write to file)
            e.printStackTrace()
            setToastError(activity,e.message.toString())
            // Perform error handling or show appropriate message to the user
        } catch (e: Exception) {
            // Handle other exceptions
            e.printStackTrace()
            setToastError(activity,e.message.toString())
            // Perform error handling or show appropriate message to the user
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

        try {
            recorder = MediaRecorder()
            recorder?.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setOutputFile(dirPath + fileName)
                prepare()
                start()
                animatePlayerView()
                setToastInfo(activity,"Recorded Started")
            }
        } catch (e: IllegalStateException) {
            // Handle IllegalStateException (e.g., recording already started)
            e.printStackTrace()
            setToastError(activity,e.message.toString())
            // Perform error handling or show appropriate message to the user
        } catch (e: IOException) {
            // Handle IOException (e.g., failed to prepare or write to file)
            e.printStackTrace()
            setToastError(activity,e.message.toString())
            // Perform error handling or show appropriate message to the user
        } catch (e: Exception) {
            // Handle other exceptions
            e.printStackTrace()
            setToastError(activity,e.message.toString())
            // Perform error handling or show appropriate message to the user
        }
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

    private fun pauseRecordingAudio(){
        if(recorder!=null&&recordingAudio){
            try {
                recorder?.apply {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        pause()
                        showLayoutPauseRecord()
                        pauseRecordAudio = true
                        setToastInfo(activity,"Recording Paused")
                    }
                }
            } catch (e: IllegalStateException) {
                // Handle IllegalStateException (e.g., recording already started)
                e.printStackTrace()
                setToastError(activity,e.message.toString())

                // Perform error handling or show appropriate message to the user
            } catch (e: IOException) {
                // Handle IOException (e.g., failed to prepare or write to file)
                e.printStackTrace()
                setToastError(activity,e.message.toString())
                // Perform error handling or show appropriate message to the user
            } catch (e: Exception) {
                // Handle other exceptions
                e.printStackTrace()
                setToastError(activity,e.message.toString())
                // Perform error handling or show appropriate message to the user
            }
        }
    }

    private fun resumeRecordingAudio(){
        if(recorder!=null&&pauseRecordAudio){
            try {
                recorder?.apply {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        resume()
                        setToastInfo(activity,"Recording Resumed")
                        binding.recordText.visibility = View.GONE
                        pauseRecordAudio = false

                        binding.recordBtn.setImageResource(R.drawable.ic_pause)
                        animatePlayerView()
                        timer.start()
                    }
                }

            } catch (e: IllegalStateException) {
                // Handle IllegalStateException (e.g., recording already started)
                e.printStackTrace()
                setToastError(activity,e.message.toString())

                // Perform error handling or show appropriate message to the user
            } catch (e: IOException) {
                // Handle IOException (e.g., failed to prepare or write to file)
                e.printStackTrace()
                setToastError(activity,e.message.toString())
                // Perform error handling or show appropriate message to the user
            } catch (e: Exception) {
                // Handle other exceptions
                e.printStackTrace()
                setToastError(activity,e.message.toString())
                // Perform error handling or show appropriate message to the user
            }

        }
    }

    private fun stopRecordingAudio(message : String){
        if(recorder!=null&&recordingAudio){
            try {
                recorder?.apply {
                    stop()
                    reset()
                    release()
                    recordingAudio = false
                    pauseRecordAudio= false
                    showLayoutStopRecord()
                    if(message.isNotEmpty()){
                        setToastInfo(activity,message)
                    }
                }

                recorder = null
            } catch (e: IllegalStateException) {
                // Handle IllegalStateException (e.g., recording already started)
                e.printStackTrace()
                setToastError(activity,e.message.toString())

                // Perform error handling or show appropriate message to the user
            } catch (e: IOException) {
                // Handle IOException (e.g., failed to prepare or write to file)
                e.printStackTrace()
                setToastError(activity,e.message.toString())
                // Perform error handling or show appropriate message to the user
            } catch (e: Exception) {
                // Handle other exceptions
                e.printStackTrace()
                setToastError(activity,e.message.toString())
                // Perform error handling or show appropriate message to the user
            }

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

    @SuppressLint("SetTextI18n")
    private fun stopRecordingScreen(){

    }

    private fun showBottomSheet(){
        val bottomSheet = BottomSheet(dirPath, fileName, this)
        bottomSheet.show(requireActivity().supportFragmentManager, LOG_TAG)
    }



    @SuppressLint("SetTextI18n")
    override fun onCancelClicked() {
        setToastSuccess(activity,"The recording has been cancelled")
        binding.recordText.text = "Record"
        binding.recordText.visibility = View.VISIBLE
        stopRecordingAudio("")
    }

    @OptIn(DelicateCoroutinesApi::class)
    @SuppressLint("SetTextI18n")
    override fun onOkClicked(filePath: String, filename: String, isChange : Boolean) {
        // add audio record info to database
        if(activity!=null){
            val db = Room.databaseBuilder(requireActivity(), AppDatabase::class.java, "audioRecords").build()

            val duration = timer.format().split(".")[0]

            stopRecordingAudio("")

            if(isChange){
                val newFile = File("$dirPath$filename.mp3")
                File(dirPath+fileName).renameTo(newFile)
            }

            GlobalScope.launch {
                db.audioRecordDAO().insert(AudioRecord(filename, filePath, Date().time, duration))
            }
            setToastSuccess(activity,"Successfully saved the recording")

            binding.recordText.visibility = View.VISIBLE
            binding.recordText.text = "Record"
            //showInterstitial(activity)
            showRewardAds()
        }

    }

    override fun onPlaySong(filePath: String) {
        if(activity!=null){
            if(mp!=null){
               mp.apply {
                   mp?.release()
                   mp = null
               }
            }
            val handler = Handler(Looper.getMainLooper())
            handler.postDelayed({
                try {
                    mp = MediaPlayer()
                    mp?.apply {
                        setDataSource(requireActivity(),Uri.parse(filePath))
                        volumes?.let { setVolume(it, volumes!!) }
                        setOnPreparedListener{
                            mp?.start()
                        }
                        mp?.prepareAsync()
                        showBtnStop = true
                        songIsPlaying = true

                    }
                } catch (e: IOException) {
                    setToastError(activity,e.message.toString())
                } catch (e: IllegalStateException) {
                    setToastError(activity,e.message.toString())
                }catch (e : Exception){
                    setToastError(activity,e.message.toString())
                }
            }, 100)

        }
    }


    override fun onPause() {
        super.onPause()
        sharedPreferences?.unregisterOnSharedPreferenceChangeListener(this)
        if (mp != null) {
            mp.apply {
                mp?.release()
                showBtnStop = false
                songIsPlaying = false
            }
        }

        if(recorder!=null&&recordingAudio){
            recorder?.apply {
                release()
                recordingAudio = false
                pauseRecordAudio= false
            }
            recorder = null
            showLayoutStopRecord()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mp != null) {
            mp?.apply {
                release()
                showBtnStop = false
                songIsPlaying = false
            }
        }
        if(recorder!=null&&recordingAudio){
            recorder?.apply {
                release()
                recordingAudio = false
                pauseRecordAudio= false
            }
            recorder = null
            showLayoutStopRecord()
        }
    }

    override fun onStopSong() {
        if(activity!=null&&mp!=null){
            try {
                mp?.apply {
                    stop()
                    reset()
                    release()
                    mp = null
                    songIsPlaying = false
                    showBtnStop = false
                }
            } catch (e: IOException) {
                setToastError(activity,e.message.toString())
            } catch (e: IllegalStateException) {
                setToastError(activity,e.message.toString())
            }catch (e : Exception){
                setToastError(activity,e.message.toString())
            }
        }
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


    fun setToastError(activity: Activity?,message : String){
        if(activity!=null){
            Toastic.toastic(activity,
                message = message,
                duration = Toastic.LENGTH_SHORT,
                type = Toastic.SUCCESS,
                isIconAnimated = true
            ).show()
        }
    }

    private fun setToastSuccess(activity: Activity?, message : String){
        if(activity!=null){
            Toastic.toastic(
                activity,
                message = message,
                duration = Toastic.LENGTH_SHORT,
                type = Toastic.SUCCESS,
                isIconAnimated = true
            ).show()
        }
    }

    private fun setToastInfo(activity: Activity?, message : String){
        if(activity!=null){
            Toastic.toastic(activity,
                message = message,
                duration = Toastic.LENGTH_SHORT,
                type = Toastic.INFO,
                isIconAnimated = true
            ).show()
        }
    }

    private fun setupInterstitial() {
        if(activity!=null){
            val adRequestInterstitial = AdRequest.Builder().build()
            InterstitialAd.load(requireActivity(),DataSession(requireActivity()).getInterstitialId(), adRequestInterstitial,
                object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(interstitialAd: InterstitialAd) {
                        mInterstitialAd = interstitialAd
                        isLoadInterstitial = true
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        mInterstitialAd = null
                    }
                })
        }
    }

    private fun setupReward(){
        if(activity!=null){
            val adRequest = AdRequest.Builder().build()
            RewardedAd.load(requireActivity(),DataSession(requireActivity()).getRewardId(), adRequest, object : RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    rewardedAd = null
                }
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    isLoadReward = true
                    rewardedAd?.fullScreenContentCallback = object: FullScreenContentCallback() {
                        override fun onAdClicked() {
                            // Called when a click is recorded for an ad.
                            Log.d("yametere", "Ad was clicked.")
                        }

                        override fun onAdDismissedFullScreenContent() {
                            // Called when ad is dismissed.
                            // Set the ad reference to null so you don't show the ad a second time.
                            Log.d("yametere", "Ad dismissed fullscreen content.")
                            rewardedAd = null
                        }

                        override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                            // Called when ad fails to show.
                            Log.d("yametere", "Ad failed to show fullscreen content.")
                            rewardedAd = null
                        }

                        override fun onAdImpression() {
                            // Called when an impression is recorded for an ad.
                            Log.d("yametere", "Ad recorded an impression.")
                        }

                        override fun onAdShowedFullScreenContent() {
                            // Called when ad is shown.
                            Log.d("yametere","Ad showed fullscreen content.")
                        }
                    }
                }
            })
        }
    }

    private fun showInterstitial(){
        if(activity!=null){
            if(isLoadInterstitial){
                mInterstitialAd?.show(requireActivity())
            }
        }
    }

    private fun showRewardAds(){
        if(activity!=null){
            if(isLoadReward){
                Log.d("yametere", "show")
                rewardedAd?.let { ad ->
                    ad.show(requireActivity()) { rewardItem ->
                        // Handle the reward.
                        val rewardAmount = rewardItem.amount
                        val rewardType = rewardItem.type
                        Log.d("yametere", "User earned the reward.$rewardAmount--$rewardType")
                    }
                } ?: run {
                    Log.d("yametere", "The rewarded ad wasn't ready yet.")
                    showInterstitial()
                }
            }else{
                Log.d("yametere", "nall")
            }
        }else{
            Log.d("yametere", "null")
        }
    }
}