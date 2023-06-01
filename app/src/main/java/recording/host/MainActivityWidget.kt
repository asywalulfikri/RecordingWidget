package recording.host

import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import com.google.android.gms.ads.MobileAds
import com.google.firebase.firestore.FirebaseFirestore
import recording.host.databinding.ActivityMainBinding
import sound.recorder.widget.*
import sound.recorder.widget.base.BaseActivityWidget
import sound.recorder.widget.model.Song
import sound.recorder.widget.ui.bottomSheet.BottomSheetVideo
import sound.recorder.widget.util.Constant
import sound.recorder.widget.util.DataSession


class MainActivityWidget : BaseActivityWidget(),SharedPreferences.OnSharedPreferenceChangeListener{


    private var recordWidgetHN : RecordWidgetHN? =null
    private var recordWidgetHB : RecordWidgetHB? =null
    private var recordWidgetH : RecordWidgetH? =null
    private var recordWidgetVB : RecordWidgetVB? =null

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
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(binding.root)

        firebaseFirestore = FirebaseFirestore.getInstance()


        sharedPreferences = DataSession(this).getShared()
        sharedPreferences?.registerOnSharedPreferenceChangeListener(this)

        //setupAnimationNot(binding.musicView)

        if(DataSession(this).getAnimation()){
            binding.musicView.visibility = View.VISIBLE
        }else{
            binding.musicView.visibility = View.INVISIBLE
        }

        MobileAds.initialize(this)

        RecordingSDK.initSdk(this,"","ca-app-pub-3940256099942544/6300978111","ca-app-pub-3940256099942544/1033173712","ca-app-pub-3940256099942544/5224354917","").run()
        RecordingSDK.initSdkColor(this, sound.recorder.widget.R.color.color7,R.color.white)

        for (i in listTitle.indices) {
            val itemSong = Song()
            itemSong.title = listTitle[i]
            itemSong.pathRaw = pathRaw[i]
            song.add(itemSong)
        }
        RecordingSDK.addSong(this,song)


        setSupportActionBar(binding.toolbar)

        recordWidgetHN = RecordWidgetHN(this)
        recordWidgetHN?.loadData()

        recordWidgetHB = RecordWidgetHB(this)
        recordWidgetHB?.loadData()


        recordWidgetH = RecordWidgetH(this)
        recordWidgetH?.loadData()

        recordWidgetVB = RecordWidgetVB(this)
        recordWidgetVB?.loadData()


        val recordWidgetV = RecordWidgetV(this)
        recordWidgetV.loadData()

        binding.btnKlik.setOnClickListener {
            val bottomSheet = BottomSheetVideo(firebaseFirestore)
            bottomSheet.show(this.supportFragmentManager, "")
            //RecordingSDK.showDialogColorPicker(this,"background")
            // val intent = Intent(this,ListVideoActivity::class.java)
            //startActivity(intent)
        }

        getFirebaseToken()
        setupBackground()

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
            val animation = getSharedPreferenceUpdate().getBoolean(Constant.keyShared.animation,false)
            if(!animation){
                binding.musicView.visibility =View.INVISIBLE
            }else{
                binding.musicView.visibility =View.VISIBLE
            }
        }
    }


}