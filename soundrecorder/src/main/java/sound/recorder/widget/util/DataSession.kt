package sound.recorder.widget.util

import android.content.Context
import android.content.SharedPreferences
import sound.recorder.widget.model.Song

internal class DataSession(private val mContext: Context) {

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

    fun addInitiate(status : Boolean,admobId : String, bannerId : String, interstitialId : String){
        val editor = sharedPref.edit()
        editor.putBoolean("initiate", status)
        editor.putString(admobIdName, admobId)
        editor.putString(bannerIdName, bannerId)
        editor.putString(interstitialIdName, interstitialId)
        editor.apply()
    }

    fun isContainSong(): Boolean {
        return sharedPref.getBoolean("addSong", false)
    }

    fun initiateSong(status: Boolean){
        val editor = sharedPref.edit()
        editor.putBoolean("addSong", status)
        editor.apply()
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
