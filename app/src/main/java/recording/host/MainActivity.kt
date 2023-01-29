package recording.host

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.MobileAds
import recording.host.databinding.ActivityMainBinding
import sound.recorder.widget.RecordWidgetH
import sound.recorder.widget.RecordWidgetV
import sound.recorder.widget.RecordingSDK


class MainActivity : AppCompatActivity() {


    private var recordWidgetH : RecordWidgetH? =null
    private var recordWidgetV : RecordWidgetV? =null

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        MobileAds.initialize(this)

        RecordingSDK.initSdk(this,"","ca-app-pub-3940256099942544/6300978111","ca-app-pub-3940256099942544/1033173712").run()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        recordWidgetH = RecordWidgetH(this)
        recordWidgetH?.loadData()

        recordWidgetV = RecordWidgetV(this)
        recordWidgetV?.loadData()

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