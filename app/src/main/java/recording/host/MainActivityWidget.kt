package recording.host

import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import com.google.android.gms.ads.MobileAds
import com.google.firebase.firestore.FirebaseFirestore
import recording.host.databinding.ActivityMainBinding
import sound.recorder.widget.RecordWidgetH
import sound.recorder.widget.RecordWidgetHB
import sound.recorder.widget.RecordWidgetHN
import sound.recorder.widget.RecordWidgetV
import sound.recorder.widget.RecordWidgetVBA
import sound.recorder.widget.RecordingSDK
import sound.recorder.widget.base.BaseActivityWidget
import sound.recorder.widget.internet.InternetAvailabilityChecker
import sound.recorder.widget.internet.InternetConnectivityListener
import sound.recorder.widget.model.Song
import sound.recorder.widget.ui.bottomSheet.BottomSheetVideo
import sound.recorder.widget.util.Constant
import sound.recorder.widget.util.DataSession
import sound.recorder.widget.util.PleaseWaitDialog


class MainActivityWidget : BaseActivityWidget(),SharedPreferences.OnSharedPreferenceChangeListener,
    InternetConnectivityListener {


    private var mInternetAvailabilityChecker: InternetAvailabilityChecker? = null
    private var recordWidgetHN : RecordWidgetHN? =null
    private var recordWidgetHB : RecordWidgetHB? =null
    private var recordWidgetH : RecordWidgetH? =null
    private var recordWidgetV : RecordWidgetV? =null
    private var recordWidgetVB : RecordWidgetVBA? =null

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
    private var pleaseWaitDialog : PleaseWaitDialog? =null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(binding.root)

        pleaseWaitDialog = PleaseWaitDialog(this)
        pleaseWaitDialog?.setTitle("tunggu")
        pleaseWaitDialog?.isCancelable = false
        showLoading(5000)

        mInternetAvailabilityChecker = InternetAvailabilityChecker.getInstance();
        mInternetAvailabilityChecker?.addInternetConnectivityListener(this);

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

        RecordingSDK.initSdk(this,"","ca-app-pub-3940256099942544/6300978111","ca-app-pub-3940256099942544/1033173712","ca-app-pub-3940256099942544/5354046379","ca-app-pub-3940256099942544/5224354917","").run()
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
       // recordWidgetHN?.loadData()

        recordWidgetHB = RecordWidgetHB(this)
       // recordWidgetHB?.loadData()

        recordWidgetV = RecordWidgetV(this)
       // recordWidgetV?.loadData()
        binding.llV.addView(recordWidgetV)


        recordWidgetH = RecordWidgetH(this)
       // recordWidgetH?.loadData()

        recordWidgetVB = RecordWidgetVBA(this)
     //   recordWidgetVB?.loadData()


       /* val recordWidgetV = RecordWidgetV(this)
        recordWidgetV.loadData()*/

        binding.btnKlik.setOnClickListener {
            if(isInternetConnected(this)){
                setToastInfo("konek")
            }else{
                setToastInfo("tidak konek")
            }
           /* val bottomSheet = BottomSheetVideo(firebaseFirestore)
            bottomSheet.show(this.supportFragmentManager, "")*/
            //RecordingSDK.showDialogColorPicker(this,"background")
            // val intent = Intent(this,ListVideoActivity::class.java)
            //startActivity(intent)
        }

        getFirebaseToken()
        setupBackground()

    }

    private fun showLoading(long : Long){
        pleaseWaitDialog?.show()
        // Audio file loaded successfully
        val handler = Handler()
        handler.postDelayed({

            //progressDialog?.dismiss()
            //  binding.layoutDialog.visibility = View.GONE
            pleaseWaitDialog?.dismiss()

        },
            long)

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

    override fun onDestroy() {
        super.onDestroy()
        mInternetAvailabilityChecker?.removeInternetConnectivityChangeListener(this)
    }

    override fun onInternetConnectivityChanged(isConnected: Boolean) {
        if (isConnected) {
           setToastInfo("konek")
        } else {
           setToastInfo("ilang")
        }
    }


}