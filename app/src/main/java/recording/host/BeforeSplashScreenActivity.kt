package recording.host

import android.content.Intent
import android.os.Bundle
import sound.recorder.widget.RecordingSDK
import sound.recorder.widget.base.BaseActivityWidget


class BeforeSplashScreenActivity : BaseActivityWidget() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //val rawPath =  "android.resource://"+BuildConfig.APPLICATION_ID+"/raw/splash_animation"
        RecordingSDK.addInfo(this,BuildConfig.VERSION_CODE,"recording_json",R.raw.splash_animation,"#1D7FFF","")

       // FirebaseApp.initializeApp(this);
       // val intent = Intent(this, SplashScreenSDKActivity::class.java)
       // startActivityForResult(intent,1212)


        //For Info Devices
       /* val appInfo = "VC" + BuildConfig.VERSION_CODE
        val androidVersion = "SDK" + Build.VERSION.SDK_INT
        val androidOS = "OS" + Build.VERSION.RELEASE
        val information =
            Build.MANUFACTURER + " " + Build.MODEL + " , " + androidOS + ", " + appInfo + ", " + androidVersion
        RecordingSDK.openEmail(this,getString(R.string.app_name),"\n\n\n\n"+information)*/


    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode==1212){
            startActivity(Intent(this, MainActivityWidget::class.java))
            finish()
        }
    }
}