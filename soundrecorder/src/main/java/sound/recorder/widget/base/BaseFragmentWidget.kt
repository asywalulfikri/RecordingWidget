package sound.recorder.widget.base

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import sound.recorder.widget.util.DataSession
import sound.recorder.widget.util.Toastic

open class BaseFragmentWidget : Fragment(){

    private var dataSession : DataSession? =null
    var mInterstitialAd: InterstitialAd? = null
    private var isLoad = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dataSession = DataSession(activity as Context)
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