package sound.recorder.widget.base

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.tasks.Task
import com.google.firebase.messaging.BuildConfig
import com.google.firebase.messaging.FirebaseMessaging
import sound.recorder.widget.util.DataSession
import sound.recorder.widget.util.MusicAnimationView
import java.util.concurrent.atomic.AtomicReference

open class BaseActivityWidget : AppCompatActivity() {

    private var dataSession: DataSession? = null
    private var mInterstitialAd: InterstitialAd? = null
    var id: String? = null
    private var isLoad = false
    private var adRequest : AdRequest? =null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MobileAds.initialize(this) {}
        dataSession = DataSession(this)
        adRequest = AdRequest.Builder().build()
    }

    fun setupAnimationNot(musicAnimation : MusicAnimationView){
        val myImageList = intArrayOf(sound.recorder.widget.R.drawable.music_8, sound.recorder.widget.R.drawable.music_not9, sound.recorder.widget.R.drawable.audio_track)
        musicAnimation.setImages(myImageList).start()
        musicAnimation.start()
    }


    fun animation(): Boolean{
        return DataSession(this).getAnimation()
    }

    fun setupAds(mAdView: AdView) {
        adRequest?.let { mAdView.loadAd(it) }
        adRequest?.let {
            InterstitialAd.load(this, dataSession?.getInterstitialId().toString(), it,
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

    private fun permissionNotification(){
        if (Build.VERSION.SDK_INT >= 33) {
            /*if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                // Pass any permission you want while launching
                requestPermissionNotification.launch(Manifest.permission.POST_NOTIFICATIONS)
            }*/
        }
    }

    private val requestPermissionNotification =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted -> }

    fun setupInterstitial() {
        adRequest?.let {
            InterstitialAd.load(this, dataSession?.getInterstitialId().toString(), it,
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

    protected open fun getFirebaseToken(): String? {
        val tokens = AtomicReference("")
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task: Task<String> ->
                if (!task.isSuccessful) {
                    Log.w("response", "Fetching FCM registration token failed", task.exception)
                    getFirebaseToken()
                    return@addOnCompleteListener
                }

                // Get new FCM registration token
                val tokenFirebase = task.result
                tokens.set(tokenFirebase)
                Log.d("tokenFirebase",tokenFirebase.toString())

            }
        return tokens.get()
    }


    fun showInterstitial(){
        if(isLoad){
            mInterstitialAd?.show(this)
        }
    }

    fun setToast(message : String){
        Toast.makeText(getActivity(),message,Toast.LENGTH_SHORT).show()
    }

    fun setLog(message: String){
        Log.d("response", "$message - ")
    }

    open fun getActivity(): BaseActivityWidget? {
        return this
    }

    open fun rating(){
        val appPackageName = packageName

        try {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=$appPackageName")
                )
            )
        } catch (anfe: ActivityNotFoundException) {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")
                )
            )
        }
    }

    fun isDarkTheme(): Boolean {
        return resources?.configuration?.uiMode!! and
                Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
    }



    fun showKeyboard(view: View) {
        if (view.requestFocus()) {
            val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
            imm?.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
        }
    }

    fun hideKeyboard(view: View) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        imm?.hideSoftInputFromWindow(view.windowToken, 0)
    }

}