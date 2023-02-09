package sound.recorder.widget

import android.app.Application
import android.content.Context
import android.support.multidex.MultiDex

internal class MyApp : Application() {

    override fun attachBaseContext(context: Context) {
        super.attachBaseContext(context)
        MultiDex.install(this)
    }
}