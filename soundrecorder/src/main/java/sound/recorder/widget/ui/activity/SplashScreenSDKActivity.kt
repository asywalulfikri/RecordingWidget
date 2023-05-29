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
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.tasks.Task
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.ktx.isFlexibleUpdateAllowed
import com.google.android.play.core.ktx.isImmediateUpdateAllowed
import com.google.firebase.FirebaseApp
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.google.gson.Gson
import kotlinx.coroutines.launch
import org.json.JSONObject
import sound.recorder.widget.R
import sound.recorder.widget.databinding.ActivitySplashSdkBinding
import sound.recorder.widget.model.MenuConfig
import sound.recorder.widget.util.DataSession
import sound.recorder.widget.util.Toastic


@SuppressLint("CustomSplashScreen")
class SplashScreenSDKActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashSdkBinding
    private var jsonName = ""
    private var currentVersionCode : Int? =null
    private var dataSession : DataSession? =null
    private lateinit var appUpdateManager : AppUpdateManager
    private var updateType : Int? =null


    @SuppressLint("NotifyDataSetChanged", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashSdkBinding.inflate(layoutInflater)
        setContentView(binding.root)

    }
    override fun onStart() {
        super.onStart()
        updateData()
    }

    @SuppressLint("SetTextI18n")
    fun updateData(){
        FirebaseApp.initializeApp(applicationContext)
        appUpdateManager = AppUpdateManagerFactory.create(this)
        dataSession = DataSession(this)
        jsonName = dataSession?.getJsonName().toString()


        if(dataSession?.getSplashScreenColor().toString().isNotEmpty()){
            binding.backgroundSplash.setBackgroundColor(Color.parseColor(dataSession?.getSplashScreenColor()))
        }

        if(dataSession?.getAppName().toString().isNotEmpty()){
            binding.tvTitle.text = dataSession?.getAppName() + "\n"+ "v "+ dataSession?.getVersionName().toString()
        }

        currentVersionCode = dataSession?.getVersionCode()

        //Check If data send from Host Empty Or Not
        if(dataSession?.getJsonName().toString().isNotEmpty()){
            if(dataSession?.getVersionCode()!=0||dataSession?.getVersionCode()!=null){
                checkVersion()
            }else{
                goToNextPage()
            }
        }else{
            goToNextPage()
        }
    }

    override fun onResume() {
        super.onResume()

        if(updateType==AppUpdateType.IMMEDIATE){
            appUpdateManager.appUpdateInfo.addOnSuccessListener { info->
                if(info.updateAvailability()==UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS){
                    appUpdateManager.startUpdateFlowForResult(
                        info,
                        updateType!!,
                        this,
                        123
                    )
                }
            }
        }
    }


    private fun checkVersion() {
        val mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(10)
            .setFetchTimeoutInSeconds(1)
            .build()
        mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings)
        mFirebaseRemoteConfig.fetchAndActivate()
            .addOnCompleteListener(this) { task: Task<Boolean?> ->
                if (task.isSuccessful) {
                    val json = mFirebaseRemoteConfig.getString(jsonName)
                    try {
                        JSONObject(json)
                        val menuConfig = Gson().fromJson(json, MenuConfig::class.java)
                        if(menuConfig==null){
                            Log.d("value_json", "empty")
                            goToNextPage()
                        }else{
                            Log.d("value_json", Gson().toJson(menuConfig) + "---"+jsonName)
                            checkVersionSuccess(menuConfig)
                        }

                    } catch (e: Exception) {
                        goToNextPage()
                    }

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
        val checkForUpdate = checkVersionResponse.checkForUpdate
        val releaseNote = checkVersionResponse.releaseNote

        Log.d("infoSDK",
            "App version code now = $currentVersion , App version code live = $latestVersion"
        )

        if(showDialog==true){
            if(maintenance==true){
                //Jika Maintenance true Tidak bisa Buka App Sama sekali
                showUpdateDialog(getString(R.string.dialog_maintenance))
            }else{

                //Mau Check Ga Untuk Update nya??
                if(checkForUpdate==true){

                    //Jika Maintenance false, di cek dulu force update nya
                    if(force==true){
                        //Kalau force update nya true
                        updateType = AppUpdateType.IMMEDIATE
                        appUpdateManager.registerListener(installUpdateListener)
                        checkForUpdateApps()
                    } else {
                        updateType = AppUpdateType.FLEXIBLE
                        appUpdateManager.registerListener(installUpdateListener)
                        checkForUpdateApps()
                    }

                }else{
                    goToNextPage()
                }
            }
        }else{
            goToNextPage()
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode==123){
            if(requestCode!=RESULT_OK){
                setToastError("Something went wrong updating...")
                goToNextPage()
            }else{
                setToastSuccess("Lanjutt")
            }
        }
    }


    private var installUpdateListener = InstallStateUpdatedListener { state->
        if(state.installStatus()== InstallStatus.DOWNLOADED){
            setToastSuccess("Download Successful. Restarting app in 5 second")
        }
        lifecycleScope.launch {

            appUpdateManager.completeUpdate()
        }
    }

    private fun checkForUpdateApps(){
        appUpdateManager.appUpdateInfo.addOnSuccessListener {info->
            val isUpdateAvailable = info.updateAvailability()==UpdateAvailability.UPDATE_AVAILABLE
            Log.d("yamete","isUpdateAvailable = "+isUpdateAvailable+"--")
            val isUpdateAllowed = when( updateType){
                AppUpdateType.FLEXIBLE -> info.isFlexibleUpdateAllowed
                AppUpdateType.IMMEDIATE -> info.isImmediateUpdateAllowed
                else -> false
            }

            Log.d("yamete","updateType = "+isUpdateAllowed)

            if(isUpdateAvailable && isUpdateAllowed){
                appUpdateManager.startUpdateFlowForResult(
                    info,
                    updateType!!,
                    this,
                    123
                )
            }else{
                Log.d("yametexx","updateType = "+isUpdateAllowed)
                goToNextPage()
            }
        }.addOnFailureListener {
            Log.d("yameteas",it.message.toString())
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
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        setResult(RESULT_OK,intent)
        finish()
    }

    fun setToastSuccess(message : String){
        Toastic.toastic(
            context = this,
            message = message,
            duration = Toastic.LENGTH_SHORT,
            type = Toastic.SUCCESS,
            isIconAnimated = true
        ).show()
    }

    fun setToastError(message : String){
        Toastic.toastic(
            context = this,
            message = message,
            duration = Toastic.LENGTH_SHORT,
            type = Toastic.SUCCESS,
            isIconAnimated = true
        ).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        if(updateType== AppUpdateType.FLEXIBLE){
            appUpdateManager.unregisterListener(installUpdateListener)
        }
    }

}