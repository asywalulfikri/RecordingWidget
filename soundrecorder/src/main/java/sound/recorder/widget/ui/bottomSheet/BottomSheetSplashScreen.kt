/*
package sound.recorder.widget.ui.bottomSheet

import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.google.android.gms.tasks.Task
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_COLLAPSED
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.google.gson.Gson
import sound.recorder.widget.R
import sound.recorder.widget.adapter.AudioRecorderAdapter
import sound.recorder.widget.adapter.VideoListAdapter
import sound.recorder.widget.base.BaseBottomSheet
import sound.recorder.widget.databinding.ActivitySplashSdkBinding
import sound.recorder.widget.model.MenuConfig
import sound.recorder.widget.model.Video
import sound.recorder.widget.util.DataSession
import java.util.ArrayList


open class BottomSheetSplashScreen(var mFirebaseRemoteConfig: FirebaseRemoteConfig,private val listener: OnMoveListener) : BaseBottomSheet(),SharedPreferences.OnSharedPreferenceChangeListener {


    interface OnMoveListener {
        fun onMovePage()
    }

    private lateinit var binding : ActivitySplashSdkBinding
    private var currentVersionCode : Int? =null
    private var dataSession : DataSession? =null
    private var jsonName : String? =null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = ActivitySplashSdkBinding.inflate(layoutInflater)

        val displayMetrics = DisplayMetrics()
        requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)
        val screenHeight = displayMetrics.heightPixels
        val screenWidth = displayMetrics.widthPixels


        (dialog as? BottomSheetDialog)?.behavior?.state = STATE_EXPANDED
        (dialog as? BottomSheetDialog)?.behavior?.peekHeight = screenHeight
      //  (dialog as? BottomSheetDialog)?.behavior?.maxHeight = screenHeight
        (dialog as? BottomSheetDialog)?.behavior?.isDraggable = false

        val layoutParams =  (dialog as? BottomSheetDialog)?.window?.attributes
        layoutParams?.width = screenWidth
        dialog?.window?.attributes = layoutParams
        */
/*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            dialog?.window?.let { WindowCompat.setDecorFitsSystemWindows(it, false) }
        } else {
            @Suppress("DEPRECATION")
            dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        }*//*


        getShared().registerOnSharedPreferenceChangeListener(this)
        dataSession = DataSession(requireContext())
        currentVersionCode = dataSession?.getVersionCode()


        if(dataSession?.getSplashScreenType()=="1"){
            binding.backgroundSplash.setBackgroundColor(Color.parseColor("#f8a424"))
            binding.animationView1.visibility = View.VISIBLE
        }else{
            binding.backgroundSplash.setBackgroundColor(Color.parseColor("#3490dc"))
            binding.animationView2.visibility =  View.VISIBLE
        }

        binding.tvTitle.text = dataSession?.getAppName()
        currentVersionCode = dataSession?.getVersionCode()
        jsonName = dataSession?.getJsonName()


        if(isInternetAvailable()){
            if(dataSession?.getJsonName().toString().isNotEmpty()){
                checkVersion()
            }
        }else{
            goToNextPage()
        }

        return binding.root

    }

    fun getShared(): SharedPreferences {
        return DataSession(requireActivity()).getShared()
    }

    override fun onResume() {
        super.onResume()
        getShared().registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        getShared()?.unregisterOnSharedPreferenceChangeListener(this)
    }
    private fun isInternetAvailable(): Boolean {

        val connectivityManager = requireActivity().getSystemService(AppCompatActivity.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    private fun checkVersion() {
        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(10)
            .build()
        mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings)
        mFirebaseRemoteConfig.fetchAndActivate()
            .addOnCompleteListener(requireActivity()
            ) { task: Task<Boolean?> ->
                if (task.isSuccessful) {
                    val json = mFirebaseRemoteConfig.getString(jsonName.toString())
                    val menuConfig = Gson().fromJson(json, MenuConfig::class.java)
                    Log.d("value_json", Gson().toJson(menuConfig) + "---")
                    //Toast.makeText(this,"sukses", Toast.LENGTH_SHORT).show()
                    if(menuConfig.app_info!=null){
                        checkVersionSuccess(menuConfig)
                    }else{
                        goToNextPage()
                    }
                }
            }

    }

    private fun checkVersionSuccess(checkVersionResponse: MenuConfig) {
        val currentVersion = currentVersionCode
        val latestVersion = checkVersionResponse.app_info?.version_code
        val force = checkVersionResponse.app_info?.force_update
        val maintenance = checkVersionResponse.app_info?.maintenance

        if(maintenance==true){
            showUpdateDialog("Sorry..The application is under maintenance, Please try again later")
        }else{
            if(force==false){
                if(currentVersion!!<latestVersion!!){
                    showUpdateDialog("The new version is available in Play Store, please update your application")
                }else{
                    goToNextPage()
                }
            } else if (isLatestVersion(currentVersion!!, latestVersion!!) && force!!) {
                goToNextPage()
            } else {
                showUpdateDialog("Please update your Application")
            }
        }

    }


    private fun isLatestVersion(currentVersion: Int, latestVersion: Int): Boolean {
        return currentVersion >= latestVersion
    }

    private fun showUpdateDialog(message : String) {

        // custom dialog
        val dialog = Dialog(requireContext())
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
            if(message=="Sorry..The application is under maintenance, Please try again later"){
                //triggered
            }else{
                gotoPlayStore()
            }
        }

        if(message=="The new version is available in Play Store, please update your application"){
            btnCancel.visibility = View.VISIBLE
        }
        btnCancel.setOnClickListener {
            dialog.dismiss()
            goToNextPage()
        }
        dialog.show()
    }

    private fun gotoPlayStore() {
        val appPackageName = requireContext().packageName
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$appPackageName")))
        } catch (activityNotFoundException: ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")))
        }
    }

    private fun goToNextPage(){
       //listener.onMovePage()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        setToast(key.toString())
    }

}*/
