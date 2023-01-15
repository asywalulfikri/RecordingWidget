package recording.host

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import recording.host.databinding.ActivityMainBinding
import sound.recorder.widget.WidgetRecord

class MainActivity : AppCompatActivity() {

    private var widgetVoiceHorizontal : WidgetRecord? =null
    private var widgetVoiceVertical : WidgetRecord? =null

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        widgetVoiceHorizontal = WidgetRecord(this)
        widgetVoiceHorizontal?.loadData(false)

       /* widgetVoiceVertical = WidgetRecordVertical(this)
        widgetVoiceVertical?.loadData()*/


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