package sound.recorder.widget.ui.activity

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.google.gson.Gson
import sound.recorder.widget.R
import sound.recorder.widget.databinding.ActivitySplashSdkBinding
import sound.recorder.widget.model.MenuConfig
import sound.recorder.widget.util.DataSession


@SuppressLint("CustomSplashScreen")
class SplashScreenSDKActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashSdkBinding
    private var jsonName = ""
    private var currentVersionCode : Int? =null
    private var dataSession : DataSession? =null

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashSdkBinding.inflate(layoutInflater)
        setContentView(binding.root)

        FirebaseApp.initializeApp(this);

        dataSession = DataSession(this)
        jsonName = dataSession?.getJsonName().toString()

        if(dataSession?.getSplashScreenType()=="1"){
            binding.backgroundSplash.setBackgroundColor(Color.parseColor("#f8a424"))
            binding.animationView1.visibility = View.VISIBLE
        }else{
            binding.backgroundSplash.setBackgroundColor(Color.parseColor("#3490dc"))
            binding.animationView2.visibility =  View.VISIBLE
        }

        binding.tvTitle.text = dataSession?.getAppName()
        currentVersionCode = dataSession?.getVersionCode()

        if(isInternetAvailable()){
            checkVersion()
        }else{
            goToNextPage()
        }

    }

    private fun isInternetAvailable(): Boolean {
        val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    private fun checkVersion() {
        val mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(10)
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
                }
            }

    }

    private fun checkVersionSuccess(checkVersionResponse: MenuConfig) {
        val currentVersion = currentVersionCode
        val latestVersion = checkVersionResponse.versionCode
        val force = checkVersionResponse.forceUpdate
        val maintenance = checkVersionResponse.maintenance

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

    }


    private fun isLatestVersion(currentVersion: Int, latestVersion: Int): Boolean {
        return currentVersion >= latestVersion
    }

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
                finish()
            }else{
                gotoPlayStore()
            }
        }

        if(message==getString(R.string.dialog_msg_update_app_version)){
            btnCancel.visibility = View.VISIBLE
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