package sound.recorder.widget.ui.activity

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.TextView
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.google.gson.Gson
import sound.recorder.widget.R
import sound.recorder.widget.base.BaseActivityWidget
import sound.recorder.widget.databinding.ActivitySplashSdkBinding
//import sound.recorder.widget.databinding.ActivitySplashSdkBinding
import sound.recorder.widget.model.MenuConfig
import sound.recorder.widget.util.DataSession


@SuppressLint("CustomSplashScreen")
class SplashScreenSDKActivity : BaseActivityWidget() {

    private lateinit var binding: ActivitySplashSdkBinding
    private var jsonName = ""
    private var currentVersionCode : Int? =null
    private var dataSession : DataSession? =null

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashSdkBinding.inflate(layoutInflater)
        setContentView(binding.root)

        FirebaseApp.initializeApp(this)

        dataSession = DataSession(this)
        jsonName = dataSession?.getJsonName().toString()


        binding.backgroundSplash.setBackgroundColor(Color.parseColor(dataSession?.getSplashScreenColor()))
        binding.tvTitle.text = dataSession?.getAppName()
        currentVersionCode = dataSession?.getVersionCode()

        checkVersion()

    }


    private fun checkVersion() {
        val mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(10)
            .setFetchTimeoutInSeconds(1)
            .build()
        mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings)
        mFirebaseRemoteConfig.fetchAndActivate()
            .addOnCompleteListener(this
            ) { task: Task<Boolean?> ->
                if (task.isSuccessful) {
                    val json = mFirebaseRemoteConfig.getString(jsonName)
                    val menuConfig = Gson().fromJson(json, MenuConfig::class.java)
                    Log.d("value_json", Gson().toJson(menuConfig) + "---"+jsonName)
                    checkVersionSuccess(menuConfig)
                }else{
                    Log.d("value_json", task.exception?.message.toString() +"---"+jsonName)
                    goToNextPage()
                }
            }

    }

    private fun checkVersionSuccess(checkVersionResponse: MenuConfig) {
        val currentVersion = currentVersionCode
        val latestVersion = checkVersionResponse.versionCode
        val force = checkVersionResponse.forceUpdate
        val maintenance = checkVersionResponse.maintenance
        val showDialog = checkVersionResponse.showDialog

        Log.d("infoSDK",
            "App version code now = $currentVersion , App version code live = $latestVersion"
        )

        if(showDialog==true){
            if(maintenance==true){
                showUpdateDialog(getString(R.string.dialog_maintenance))
            }else{
                if(force==false){
                    if(currentVersion!!<latestVersion!!){
                        showUpdateDialog(getString(R.string.dialog_msg_update_app_version))
                    }else{
                        goToNextPage()
                    }
                } else if (isLatestVersion(currentVersion!!, latestVersion!!) && force!!) {
                    goToNextPage()
                } else {
                    showUpdateDialog(getString(R.string.dialog_msg_update_app))
                }
            }
        }else{
            goToNextPage()
        }

    }


    private fun isLatestVersion(currentVersion: Int, latestVersion: Int): Boolean {
        return currentVersion >= latestVersion
    }

    @SuppressLint("SetTextI18n")
    private fun showUpdateDialog(message : String) {

        // custom dialog
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_alert_update)
        dialog.setCancelable(false)

        // set the custom dialog components - text, image and button
        val tvMessage = dialog.findViewById<View>(R.id.tv_message) as TextView
        val btnPrimary = dialog.findViewById<View>(R.id.btn_primary) as Button
        val btnCancel = dialog.findViewById<View>(R.id.btn_cancel) as Button
        tvMessage.text = message

        // if button is clicked, close the custom dialog
        btnPrimary.setOnClickListener {
            if(message==getString(R.string.dialog_maintenance)){
                val intent = Intent()
                intent.putExtra("exit",true)
                setResult(RESULT_OK,intent)
                finish()
            }else{
                gotoPlayStore()
            }
        }

        if(message==getString(R.string.dialog_msg_update_app_version)){
            btnCancel.visibility = View.VISIBLE
        }else if(message==getString(R.string.dialog_maintenance)){
            btnPrimary.text = "Exit"
        }
        btnCancel.setOnClickListener {
            dialog.dismiss()
            goToNextPage()
        }
        dialog.show()
    }

    private fun gotoPlayStore() {
        val appPackageName = packageName
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$appPackageName")))
        } catch (activityNotFoundException: ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")))
        }
    }

    private fun goToNextPage(){
        val intent = Intent()
        setResult(RESULT_OK,intent)
        finish()

    }
}