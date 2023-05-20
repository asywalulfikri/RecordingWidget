package sound.recorder.widget

import android.annotation.SuppressLint
import android.app.Application
import com.facebook.ads.AudienceNetworkAds


@SuppressLint("Registered")
open class MyApp : Application(){
    override fun onCreate() {
        super.onCreate()
        AudienceNetworkAds.initialize(this)
    }

}