package recording.host

import android.content.SharedPreferences
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.Fragment
import com.google.android.gms.ads.MobileAds
import com.google.firebase.firestore.FirebaseFirestore
import recording.host.databinding.ActivityMainBinding
import sound.recorder.widget.RecordingSDK
import sound.recorder.widget.base.BaseActivityWidget
import sound.recorder.widget.internet.InternetAvailabilityChecker
import sound.recorder.widget.listener.FragmentListener
import sound.recorder.widget.listener.MusicListener
import sound.recorder.widget.listener.MyFragmentListener
import sound.recorder.widget.listener.MyMusicListener
import sound.recorder.widget.listener.MyPauseListener
import sound.recorder.widget.model.Song
import sound.recorder.widget.ui.fragment.ListRecordFragment
import sound.recorder.widget.ui.fragment.NoteFragmentFirebase
import sound.recorder.widget.ui.fragment.VoiceRecordFragmentVertical
import sound.recorder.widget.util.Constant
import sound.recorder.widget.util.DataSession
import kotlin.system.exitProcess


class MainActivityWidget : BaseActivityWidget(),SharedPreferences.OnSharedPreferenceChangeListener,MusicListener,FragmentListener{


    private var mInternetAvailabilityChecker: InternetAvailabilityChecker? = null
    private lateinit var binding: ActivityMainBinding
    private val listTitle = arrayOf(
        "Jaran Goyang"
    )

    private val pathRaw = arrayOf(
        "android.resource://"+BuildConfig.APPLICATION_ID+"/raw/jaran_goyang"
    )

    private val song : ArrayList<Song> = ArrayList()
    private var sharedPreferences : SharedPreferences? =null
    private var firebaseFirestore: FirebaseFirestore? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
       // window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(binding.root)

        MyMusicListener.setMyListener(this)
        MyFragmentListener.setMyListener(this)


        //showLoadingProgress(6000)

       // showLoadingLayout(5000)
       // binding.llV.addView(RecordWidgetV(this))
        //showLoading(5000)

       // mInternetAvailabilityChecker = InternetAvailabilityChecker.getInstance();
      //  mInternetAvailabilityChecker?.addInternetConnectivityListener(this);

        firebaseFirestore = FirebaseFirestore.getInstance()


        sharedPreferences = DataSession(this).getShared()
        sharedPreferences?.registerOnSharedPreferenceChangeListener(this)

        //setupAnimationNot(binding.musicView)

       /* if(DataSession(this).getAnimation()){
            binding.musicView.visibility = View.VISIBLE
        }else{
            binding.musicView.visibility = View.INVISIBLE
        }*/

        MobileAds.initialize(this)

        RecordingSDK.initSdk(this,"","ca-app-pub-3940256099942544/6300978111","ca-app-pub-3940256099942544/1033173712","ca-app-pub-3940256099942544/5354046379","ca-app-pub-3940256099942544/5224354917","").run()
        RecordingSDK.initSdkColor(this, sound.recorder.widget.R.color.color7,R.color.white)

        for (i in listTitle.indices) {
            val itemSong = Song()
            itemSong.title = listTitle[i]
            itemSong.pathRaw = pathRaw[i]
            song.add(itemSong)
        }
        RecordingSDK.addSong(this,song)

        setupGDPR()

        checkUpdate()


        //setSupportActionBar(binding.toolbar)


     //   recordWidgetVB?.loadData()


       /* val recordWidgetV = RecordWidgetV(this)
        recordWidgetV.loadData()*/

        setupFragment(binding.recordVertical.id,VoiceRecordFragmentVertical())

        binding.btnLanguage.setOnClickListener {
           // showDialogLanguage()
           // showDialogEmail(getString(R.string.app_name),getInfo())
            simpleAnimation(binding.btnLanguage)
        }

        binding.btnVideo.setOnClickListener {
            advanceAnimation(binding.btnVideo)
            /* val bottomSheet = BottomSheetNoteFirebase()
             bottomSheet.show(this.supportFragmentManager, "")*/
            // RecordingSDK.showDialogColorPicker(this)

        }

        binding.btnSetting.setOnClickListener {
           // starAnimation(binding.btnSetting)
           // showDialogLanguage()
           // showArrayLanguage()

        }

        binding.btnNote.setOnClickListener {
            val fragment = NoteFragmentFirebase()

            supportFragmentManager.beginTransaction()
                .add(R.id.fragment_file_viewer, fragment)
                .commit()
        }

        getFirebaseToken()
        setupBackground()


    }

   /* private fun simpleAnimations(view: View , drawable:Int? = null) {
        try {
            var icon  = sound.recorder.widget.R.drawable.star_pink
            if(drawable!=null){
                icon = drawable
            }
            ParticleSystem(this, 100, icon, 800)
                .setSpeedRange(0.1f, 0.25f)
                .oneShot(view, 100)
        }catch (e : Exception){
            setLog(e.message.toString())
        }

    }*/


    private fun getInfo(): String {
        val appInfo = "VC" + BuildConfig.VERSION_CODE
        val androidVersion = "SDK" + Build.VERSION.SDK_INT
        val androidOS = "OS" + Build.VERSION.RELEASE

        return Build.MANUFACTURER + " " + Build.MODEL + " , " + androidOS + ", " + appInfo + ", " + androidVersion
    }


    override fun onResume() {
        super.onResume()
    }

    private fun setupBackground(){
        binding.rlBackground.setBackgroundColor(getSharedPreferenceUpdate().getInt(Constant.keyShared.backgroundColor,-1))
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }


    private fun getSharedPreferenceUpdate() : SharedPreferences{
        return DataSession(this).getSharedUpdate()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if(key==Constant.keyShared.backgroundColor){
            binding.rlBackground.setBackgroundColor(getSharedPreferenceUpdate().getInt(Constant.keyShared.backgroundColor,-1))
        }else if(key==Constant.keyShared.animation){
           /* val animation = getSharedPreferenceUpdate().getBoolean(Constant.keyShared.animation,false)
            if(!animation){
                binding.musicView.visibility =View.INVISIBLE
            }else{
                binding.musicView.visibility =View.VISIBLE
            }*/
        }
    }

    override fun onDestroy() {
        super.onDestroy()
       // mInternetAvailabilityChecker?.removeInternetConnectivityChangeListener(this)
    }

   /* override fun onInternetConnectivityChanged(isConnected: Boolean) {
        if (isConnected) {
           setToastInfo("konek")
        } else {
           setToastInfo("ilang")
        }
    }*/


    override fun onBackPressed() {
        val fragment = supportFragmentManager.findFragmentById(R.id.fragment_file_viewer)

        if (fragment is ListRecordFragment) {
            val consumed = fragment.onBackPressed()
            if (consumed) {
                // The back press event was consumed by the fragment
                return
            }
        }else{
            super.onBackPressed()
            moveTaskToBack(true)
            finish()
            exitProcess(0)
        }
    }

    override fun onMusic(mediaPlayer: MediaPlayer?) {

        if(mediaPlayer!=null){
            if(mediaPlayer.isPlaying){
                //setToastInfo("Lagu Main")
                binding.ivStop.visibility = View.VISIBLE
            }else{
                binding.ivStop.visibility = View.GONE
                //setToastInfo("Berhenti")
            }

            binding.ivStop.setOnClickListener {
                try {
                    mediaPlayer.pause()
                    MyMusicListener.postAction(mediaPlayer)
                }catch (e : Exception){
                    setToastError(e.message.toString())
                }

                try {
                    MyPauseListener.postAction(true)
                }catch (e : Exception){
                    setToastError(e.message.toString())
                }

            }
        }else{
            binding.ivStop.visibility = View.GONE
        }


    }

    override fun openFragment(fragment: Fragment?) {
        setupFragment(binding.fragmentFileViewer.id,fragment)
    }

}