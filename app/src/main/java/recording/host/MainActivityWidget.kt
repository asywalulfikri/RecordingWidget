package recording.host

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import com.google.android.gms.ads.MobileAds
import recording.host.databinding.ActivityMainBinding
import sound.recorder.widget.RecordWidgetH
import sound.recorder.widget.RecordWidgetV
import sound.recorder.widget.RecordingSDK
import sound.recorder.widget.base.BaseActivityWidget
import sound.recorder.widget.model.Song
import sound.recorder.widget.ui.activity.ListVideoActivity


class MainActivityWidget : BaseActivityWidget() {


    private var recordWidgetH : RecordWidgetH? =null
    private var recordWidgetV : RecordWidgetV? =null

    private lateinit var binding: ActivityMainBinding
    private val listTitle = arrayOf(
        "Jaran Goyang"

    )

    private val listLocation = arrayOf(
        R.raw.jaran_goyang
    )

    private val pathRaw = arrayOf(
        "android.resource://"+BuildConfig.APPLICATION_ID+"/raw/jaran_goyang"
    )

    private val song : ArrayList<Song> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(binding.root)

        MobileAds.initialize(this)

        RecordingSDK.initSdk(this,"","ca-app-pub-3940256099942544/6300978111","ca-app-pub-3940256099942544/1033173712").run()

        for (i in listTitle.indices) {
            val itemSong = Song()
            itemSong.title = listTitle[i]
            itemSong.location = listLocation[i]
            itemSong.pathRaw = pathRaw[i]
            song.add(itemSong)
        }
        RecordingSDK.addSong(this,song)


        setSupportActionBar(binding.toolbar)

        recordWidgetH = RecordWidgetH(this)
        recordWidgetH?.loadData()

        recordWidgetV = RecordWidgetV(this)
        recordWidgetV?.loadData()

        binding.btnKlik.setOnClickListener {
            //RecordingSDK.showDialogColorPicker(this,"background")
            val intent = Intent(this,ListVideoActivity::class.java)
            startActivity(intent)
        }

        getFirebaseToken()

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


}