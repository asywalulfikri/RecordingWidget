package recording.host

import android.content.Intent
import android.os.Bundle
import sound.recorder.widget.RecordingSDK
import sound.recorder.widget.base.BaseActivityWidget
import sound.recorder.widget.ui.activity.SplashScreenSDKActivity


class BeforeSplashScreenActivity : BaseActivityWidget() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        RecordingSDK.addInfo(this,BuildConfig.VERSION_CODE,getString(R.string.app_name),"recording_json","#000000",true)
        val intent = Intent(this, SplashScreenSDKActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
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
        if(requestCode==1212 && resultCode == RESULT_OK){
            if(data?.hasExtra("exit")==true){
                finish()
            }else{
                startActivity(Intent(this, MainActivityWidget::class.java))
                finish()
            }
        }
    }
}