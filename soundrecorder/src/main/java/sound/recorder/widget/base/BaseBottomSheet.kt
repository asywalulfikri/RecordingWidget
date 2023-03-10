package sound.recorder.widget.base

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import sound.recorder.widget.util.DataSession


open class BaseBottomSheet : BottomSheetDialogFragment(){

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


    fun setLog(message : String){
        Log.d("response", "$message.")
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