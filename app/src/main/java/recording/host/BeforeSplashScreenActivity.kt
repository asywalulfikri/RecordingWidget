package recording.host

import android.content.Intent
import android.os.Bundle
import android.util.DisplayMetrics
import com.google.firebase.FirebaseApp
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import sound.recorder.widget.RecordingSDK
import sound.recorder.widget.base.BaseActivityWidget
import sound.recorder.widget.ui.activity.SplashScreenSDKActivity
import sound.recorder.widget.ui.bottomSheet.BottomSheetSplashScreen
import sound.recorder.widget.ui.bottomSheet.BottomSheetVideo


class BeforeSplashScreenActivity : BaseActivityWidget(),BottomSheetSplashScreen.OnMoveListener {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this);
        RecordingSDK.addInfo(this,BuildConfig.VERSION_CODE,getString(R.string.app_name),"recording_json","1")

        val intent = Intent(this, SplashScreenSDKActivity::class.java)
        startActivityForResult(intent,1212)

       /* val mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
        val bottomSheet = BottomSheetSplashScreen(mFirebaseRemoteConfig,this)
        bottomSheet.show(this.supportFragmentManager, "")*/


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

    override fun onMovePage() {
        startActivity(Intent(this, MainActivityWidget::class.java))
        finish()
    }
}