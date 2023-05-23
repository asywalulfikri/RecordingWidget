package sound.recorder.widget.base

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import sound.recorder.widget.util.DataSession
import sound.recorder.widget.util.Toastic


open class BaseBottomSheet : BottomSheetDialogFragment(){

    private var dataSession : DataSession? =null
    var mInterstitialAd: InterstitialAd? = null
    private var isLoad = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dataSession = DataSession(activity as Context)
    }


    fun setLog(message : String){
        Log.d("response", "$message.")
    }



    fun setToastError(activity: Activity?,message : String){
        if(activity!=null){
            Toastic.toastic(activity,
                message = message,
                duration = Toastic.LENGTH_SHORT,
                type = Toastic.SUCCESS,
                isIconAnimated = true
            ).show()
        }
    }

    fun setToastWarning(activity: Activity?,message : String){
        if(activity!=null){
            Toastic.toastic(activity,
                message = message,
                duration = Toastic.LENGTH_SHORT,
                type = Toastic.WARNING,
                isIconAnimated = true
            ).show()
        }

    }

    fun setToastSuccess(activity: Activity?,message : String){
        if(activity!=null){
            Toastic.toastic(
                activity,
                message = message,
                duration = Toastic.LENGTH_SHORT,
                type = Toastic.SUCCESS,
                isIconAnimated = true
            ).show()
        }
    }

    fun setToastInfo(activity: Activity?,message : String){
        if(activity!=null){
            Toastic.toastic(activity,
                message = message,
                duration = Toastic.LENGTH_SHORT,
                type = Toastic.INFO,
                isIconAnimated = true
            ).show()
        }
    }


    fun setupAds(activity: Activity?) {
        if(activity!=null){
            val adRequestInterstitial = AdRequest.Builder().build()
            adRequestInterstitial.isTestDevice(activity)
            InterstitialAd.load(activity as Context,dataSession?.getInterstitialId().toString(), adRequestInterstitial,
                object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(interstitialAd: InterstitialAd) {
                        mInterstitialAd = interstitialAd
                        isLoad = true
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        mInterstitialAd = null
                    }
                })
        }
    }

    fun showInterstitial(activity: Activity?){
        if(activity!=null){
            if(isLoad){
                mInterstitialAd?.show(activity)
            }
        }
    }



}