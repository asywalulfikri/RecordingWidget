package sound.recorder.widget.base

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.Window
import android.view.inputmethod.InputMethodManager
import android.widget.RadioButton
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import org.json.JSONObject
import sound.recorder.widget.R
import sound.recorder.widget.notes.Note
import sound.recorder.widget.util.DataSession
import sound.recorder.widget.util.Toastic
import java.util.Locale
import java.util.concurrent.atomic.AtomicReference


open class BaseActivityWidget : AppCompatActivity() {

    private var dataSession: DataSession? = null
    private var mInterstitialAd: InterstitialAd? = null
    var id: String? = null
    private var isLoad = false
    private var rewardedAd: RewardedAd? = null
    private var isLoadReward = false
    private var isLoadInterstitialReward = false
    private var rewardedInterstitialAd : RewardedInterstitialAd? =null
    var language = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        MobileAds.initialize(this) {}
        dataSession = DataSession(this)

        language = dataSession?.getLanguage().toString()
        if(language.isEmpty()){
            if(getCurrentLanguage().lowercase()=="indonesia"){
                setLocale("id")
            }else{
                setLocale("en")
            }
        }else{
            setLocale(language)
        }
    }


    @SuppressLint("SetTextI18n")
    fun showDialogLanguage() {

        // custom dialog
        var type = ""
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_choose_language)
        dialog.setCancelable(true)

        // set the custom dialog components - text, image and button
        val rbIndonesia = dialog.findViewById<View>(R.id.rbIndonesia) as RadioButton
        val rbEnglish = dialog.findViewById<View>(R.id.rbEnglish) as RadioButton
        val btnSave = dialog.findViewById<View>(R.id.btn_submit) as AppCompatTextView



        if(language.isEmpty()){
            if(getCurrentLanguage().lowercase()=="indonesia"){
                rbIndonesia.isChecked = true
            }else{
                rbEnglish.isChecked = true
            }
        }else{
            if(dataSession?.getLanguage()=="en"){
                rbEnglish.isChecked = true
            }else{
                rbIndonesia.isChecked = true
            }
        }


        // if button is clicked, close the custom dialog
        btnSave.setOnClickListener {

            if(rbIndonesia.isChecked){
                type = "id"
            }

            if(rbEnglish.isChecked){
                type = "en"
            }


            if(type.isNotEmpty()){
                dataSession?.setLanguage(type)
                changeLanguage(type)
            }

            dialog.dismiss()
        }

        dialog.show()
    }


    private fun changeLanguage(type : String) {
        val locale = Locale(type) // Ganti "en" dengan kode bahasa yang diinginkan
        Locale.setDefault(locale)

        val configuration = Configuration()
        configuration.locale = locale

        resources.updateConfiguration(configuration, resources.displayMetrics)
        this.recreate()
    }


    private fun getCurrentLanguage(): String {
        val locale: Locale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            this.resources.configuration.locales[0]
        } else {
            this.resources.configuration.locale
        }
        return locale.displayLanguage
    }

    private fun setLocale(language : String) {
        val locale = Locale(language) // Ganti "en" dengan kode bahasa yang diinginkan
        Locale.setDefault(locale)

        val config = Configuration()
        config.locale = locale
        resources.updateConfiguration(config, resources.displayMetrics)
    }



    @SuppressLint("SetTextI18n")
    fun showLoadingLayout(long : Long) {

        // custom dialog
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.loading_layout)
        dialog.setCancelable(false)
        dialog.show()


        val handler = Handler()
        handler.postDelayed({
            dialog.dismiss()
        },
            long)


    }
    fun isInternetConnected(context: Context): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val networkCapabilities = connectivityManager.activeNetwork ?: return false
            val actNw = connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false

            return when {
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                // for other device how are able to connect with Ethernet
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                // for check internet over Bluetooth
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> true
                else -> false
            }
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo ?: return false
            @Suppress("DEPRECATION")
            return networkInfo.isConnected
        }
    }


    fun getNoteValue(note: Note) : String{
        val valueNote = try {
            val jsonObject = JSONObject(note.note.toString())
            val value = Gson().fromJson(note.note, Note::class.java)
            // The JSON string is valid
            value.note.toString()

        } catch (e: Exception) {
            // The JSON string is not valid
            note.note
        }

        return  valueNote
    }

    fun getTitleValue(note: Note) : String{
        var valueNote = ""
        valueNote = try {
            val jsonObject = JSONObject(note.note.toString())
            val value = Gson().fromJson(note.title, Note::class.java)
            // The JSON string is valid
            value.note.toString()

        } catch (e: Exception) {
            // The JSON string is not valid
            "No title"
        }

        return  valueNote
    }

    fun setupBanner(mAdView: AdView){
        val adRequest = AdRequest.Builder().build()
        mAdView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                Log.d("AdMob", "Ad loaded successfully")
            }

            override fun onAdFailedToLoad(p0: LoadAdError) {
                Log.d("AdMob", "Ad failed to load:"+ p0.message)
            }

            override fun onAdOpened() {
                Log.d("AdMob", "Ad opened")
            }

            override fun onAdClicked() {
                Log.d("AdMob", "Ad clicked")
            }

            override fun onAdClosed() {
                Log.d("AdMob", "Ad closed")
            }
        }

        mAdView.loadAd(adRequest)




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
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { _ -> }

    fun setupInterstitial() {
        val adRequest = AdRequest.Builder().build()
        adRequest.let {
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


    fun setupRewardInterstitial(){
        RewardedInterstitialAd.load(this, DataSession(this).getRewardInterstitialId(),
            AdRequest.Builder().build(), object : RewardedInterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedInterstitialAd) {
                    //Log.d(TAG, "Ad was loaded.")
                    rewardedInterstitialAd = ad
                    Log.d("yameteres", ad.rewardItem.type.toString()+"--")
                    isLoadInterstitialReward = true
                    rewardedInterstitialAd?.fullScreenContentCallback = object: FullScreenContentCallback() {
                        override fun onAdClicked() {
                            // Called when a click is recorded for an ad.
                            Log.d("yametere", "Ad was clicked.")
                        }

                        override fun onAdDismissedFullScreenContent() {
                            // Called when ad is dismissed.
                            // Set the ad reference to null so you don't show the ad a second time.
                            Log.d("yametere", "Ad dismissed fullscreen content.")
                            rewardedInterstitialAd = null
                        }

                        override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                            // Called when ad fails to show.
                            Log.d("yametere", "Ad failed to show fullscreen content.")
                            rewardedInterstitialAd = null
                        }

                        override fun onAdImpression() {
                            // Called when an impression is recorded for an ad.
                            Log.d("yametere", "Ad recorded an impression.")
                        }

                        override fun onAdShowedFullScreenContent() {
                            // Called when ad is shown.
                            Log.d("yametere","Ad showed fullscreen content.")
                        }
                    }
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    // Log.d(TAG, adError?.toString())
                    Log.d("yameterex",adError.message.toString())
                    rewardedInterstitialAd = null
                }
            })

    }

    fun showRewardInterstitial(){
        if(isLoadInterstitialReward){
            Log.d("yametere", "show")
            rewardedInterstitialAd?.let { ad ->
                ad.show(this) { rewardItem ->
                    // Handle the reward.
                    val rewardAmount = rewardItem.amount
                    val rewardType = rewardItem.type
                    Log.d("yametere", "User earned the reward.$rewardAmount--$rewardType")
                }
            } ?: run {
                Log.d("yametere", "The rewarded ad wasn't ready yet.")
                showInterstitial()
            }
        }
    }

    fun setupReward(){
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(this,DataSession(this).getRewardId(), adRequest, object : RewardedAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                rewardedAd = null
            }
            override fun onAdLoaded(ad: RewardedAd) {
                rewardedAd = ad
                isLoadReward = true
                rewardedAd?.fullScreenContentCallback = object: FullScreenContentCallback() {
                    override fun onAdClicked() {
                        // Called when a click is recorded for an ad.
                        Log.d("yametere", "Ad was clicked.")
                    }

                    override fun onAdDismissedFullScreenContent() {
                        // Called when ad is dismissed.
                        // Set the ad reference to null so you don't show the ad a second time.
                        Log.d("yametere", "Ad dismissed fullscreen content.")
                        rewardedAd = null
                    }

                    override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                        // Called when ad fails to show.
                        Log.d("yametere", "Ad failed to show fullscreen content.")
                        rewardedAd = null
                    }

                    override fun onAdImpression() {
                        // Called when an impression is recorded for an ad.
                        Log.d("yametere", "Ad recorded an impression.")
                    }

                    override fun onAdShowedFullScreenContent() {
                        // Called when ad is shown.
                        Log.d("yametere","Ad showed fullscreen content.")
                    }
                }
            }
        })
    }

    fun showRewardAds(){
        if(isLoadReward){
            Log.d("yametere", "show")
            rewardedAd?.let { ad ->
                ad.show(this) { rewardItem ->
                    // Handle the reward.
                    val rewardAmount = rewardItem.amount
                    val rewardType = rewardItem.type
                    Log.d("yametere", "User earned the reward.$rewardAmount--$rewardType")
                }
            } ?: run {
                Log.d("yametere", "The rewarded ad wasn't ready yet.")
                showInterstitial()
            }
        }else{
            Log.d("yametere", "nall")
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

    fun showReward(){
        if(isLoadReward){
            rewardedAd?.let { ad ->
                ad.show(this) { rewardItem ->
                    // Handle the reward.
                    val rewardAmount = rewardItem.amount
                    val rewardType = rewardItem.type
                }
            } ?: run {
                showInterstitial()
            }
        }else{
        }
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

    fun setToastWarning(message : String){
        Toastic.toastic(
            context = this,
            message = message,
            duration = Toastic.LENGTH_SHORT,
            type = Toastic.WARNING,
            isIconAnimated = true
        ).show()
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

    fun setToastInfo(message : String){
        Toastic.toastic(
            context = this,
            message = message,
            duration = Toastic.LENGTH_SHORT,
            type = Toastic.INFO,
            isIconAnimated = true
        ).show()
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