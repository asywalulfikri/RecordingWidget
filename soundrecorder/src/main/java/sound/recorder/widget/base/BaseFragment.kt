package sound.recorder.widget.base

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import sound.recorder.widget.util.DataSession

open class BaseFragment : Fragment(){

    private var dataSession : DataSession? =null
    var mInterstitialAd: InterstitialAd? = null
    private var isLoad = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dataSession = DataSession(activity as Context)
    }

    fun setToast(message : String){
        Toast.makeText(activity,message,Toast.LENGTH_SHORT).show()
    }

    @SuppressLint("NewApi")
    fun showSettingsDialog(context: Context?) {
        val builder = AlertDialog.Builder(context)
        builder.setCancelable(true)
        builder.setTitle("Permission")
        builder.setMessage(HtmlCompat.fromHtml("You need allow Permission Record Audio", HtmlCompat.FROM_HTML_MODE_LEGACY))
        builder.setPositiveButton("Setting") { dialog, _ ->
            dialog.cancel()
            openSettings(context)
        }
        builder.show()
    }

    private fun openSettings(activity: Context?) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.data = Uri.fromParts("package", activity?.packageName.toString(), null)
        activity?.startActivity(intent)
    }


    fun setupAds() {
        val adRequestInterstitial = AdRequest.Builder().build()
        adRequestInterstitial.isTestDevice(activity as Context)
        InterstitialAd.load(activity as Context,dataSession?.getInterstitialId().toString(), adRequestInterstitial,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    // The mInterstitialAd reference will be null until
                    // an ad is loaded.
                    Log.d("valueShow", "show");
                    mInterstitialAd = interstitialAd
                    isLoad = true
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    // Handle the error
                    Log.d("valueShow", loadAdError.message+"+");
                    mInterstitialAd = null
                }
            })
    }

    fun showInterstitial(){
        if(isLoad){
            mInterstitialAd?.show(activity as Activity)
        }
    }



}