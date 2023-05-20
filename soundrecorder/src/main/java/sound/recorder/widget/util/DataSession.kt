package sound.recorder.widget.util

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import sound.recorder.widget.R

open class DataSession(private val mContext: Context) {

    companion object {
        const val TAG = "Preferences"
        lateinit var sharedPref: SharedPreferences
    }

    var bannerIdName = "bannerId"
    var admobIdName = "admobId"
    var interstitialIdName = "interstitialIdName"

    init {
        sharedPref = mContext.getSharedPreferences("recordingWidget", 0)
    }

    fun getShared(): SharedPreferences{
        return mContext.getSharedPreferences("recordingWidget", 0)
    }

    fun getSharedUpdate(): SharedPreferences {
        return mContext.getSharedPreferences("recordingWidget", 0)
    }

    fun getAnimation(): Boolean{
        return sharedPref.getBoolean(Constant.keyShared.animation, false)
    }

    fun getVersionCode(): Int{
        return sharedPref.getInt("versionCode", 0)
    }

    fun getVersionName(): String?{
        return sharedPref.getString("versionName", "")
    }

    fun getSplashScreenColor(): String?{
        return sharedPref.getString("backgroundSplashScreen","")
    }

    fun getAppName(): String?{
        return sharedPref.getString("appName","")
    }

    fun getShowNote(): Boolean{
        return sharedPref.getBoolean("showNote",false)
    }

    fun getJsonName(): String?{
        return sharedPref.getString("jsonName","")
    }

    fun getVolume(): Int{
        return sharedPref.getInt(Constant.keyShared.volume, 100)
    }

    fun addInitiate(status : Boolean,admobId : String, bannerId : String, interstitialId : String){
        val editor = sharedPref.edit()
        editor.putBoolean("initiate", status)
        editor.putString(admobIdName, admobId)
        editor.putString(bannerIdName, bannerId)
        editor.putString(interstitialIdName, interstitialId)
        editor.apply()
    }

    fun setInfoApp(versionCode : Int,versionName : String,appName: String, jsonName : String,splashScreenType: String,showNote: Boolean){
        val editor = sharedPref.edit()
        editor.putInt("versionCode", versionCode)
        editor.putString("backgroundSplashScreen", splashScreenType)
        editor.putString("appName",appName)
        editor.putString("versionName",versionName)
        editor.putString("jsonName",jsonName)
        editor.putBoolean("showNote",showNote)

        editor.apply()
    }


    fun addColor(colorWidget : Int, colorRunningText: Int){
        val editor = sharedPref.edit()
        if(colorWidget!=0){
            editor.putInt(Constant.keyShared.colorWidget, colorWidget)
        }
        if(colorRunningText!=0){
            editor.putInt(Constant.keyShared.colorRunningText, colorRunningText)
        }
        editor.apply()
    }

    fun getColorWidget(): Int{
        return sharedPref.getInt(Constant.keyShared.colorWidget,R.color.color7)
    }

    fun getColorRunningText(): Int{
        return sharedPref.getInt(Constant.keyShared.colorRunningText, R.color.white)
    }

    fun isContainSong(): Boolean {
        return sharedPref.getBoolean("addSong", false)
    }

    fun initiateSong(status: Boolean){
        val editor = sharedPref.edit()
        editor.putBoolean("addSong", status)
        editor.apply()
    }

    fun saveColor(color : Int,name : String){
        val editor = sharedPref.edit()
        editor.putInt(name,color)
        editor.apply()
    }

    fun saveAnimation(value : Boolean){
        val editor = sharedPref.edit()
        editor.putBoolean(Constant.keyShared.animation,value)
        editor.apply()
    }

    fun saveVolume(value : Int){
        val editor = sharedPref.edit()
        editor.putInt(Constant.keyShared.volume,value)
        editor.apply()
    }

    fun getBackgroundColor(): Int{
        return sharedPref.getInt(Constant.keyShared.backgroundColor, -1)
    }

    fun getBannerId(): String {
        return sharedPref.getString(bannerIdName, "").toString()
    }

    fun getInterstitialId(): String {
        return sharedPref.getString(interstitialIdName, "").toString()
    }

    fun getAdmobId(): String {
        return sharedPref.getString(admobIdName, "").toString()
    }
}
