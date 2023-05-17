package sound.recorder.widget.ads

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.unity3d.ads.IUnityAdsInitializationListener
import com.unity3d.ads.UnityAds
import com.unity3d.services.banners.BannerErrorInfo
import com.unity3d.services.banners.BannerView
import com.unity3d.services.banners.BannerView.IListener
import com.unity3d.services.banners.UnityBannerSize
import sound.recorder.widget.base.BaseFragmentWidget
import sound.recorder.widget.databinding.BannerAdsBinding
import sound.recorder.widget.util.*


internal class BannerUn : BaseFragmentWidget(),
    IUnityAdsInitializationListener {


    private var _binding: BannerAdsBinding? = null
    private val binding get() = _binding!!
    private var dataSession : DataSession? =null



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = BannerAdsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Record to the external cache directory for visibility

        if(activity!=null) {
            dataSession = DataSession(requireContext())
            UnityAds.initialize(requireContext(), DataSession(requireContext()).getUnId(),false, this)
            val topBanner = BannerView(requireActivity(), dataSession?.getBannerUnit(), UnityBannerSize(320, 50))
            // Set the listener for banner lifecycle events:
            topBanner.listener = bannerListener
            loadBannerAd(topBanner)
        }
    }


    private val bannerListener: IListener = object : IListener {
        override fun onBannerLoaded(bannerAdView: BannerView) {
            Log.v("load banner success", "onBannerLoaded: " + bannerAdView.placementId)
        }

        override fun onBannerFailedToLoad(bannerAdView: BannerView, errorInfo: BannerErrorInfo) {
            Log.e(
                "load banner failed",
                "Unity Ads failed to load banner for " + bannerAdView.placementId + " with error: [" + errorInfo.errorCode + "] " + errorInfo.errorMessage
            )
            // Note that the BannerErrorInfo object can indicate a no fill (refer to the API documentation).
        }

        override fun onBannerClick(bannerAdView: BannerView) {
            // Called when a banner is clicked.
            Log.v("load banner is click", "onBannerClick: " + bannerAdView.placementId)
        }

        override fun onBannerLeftApplication(bannerAdView: BannerView) {
            // Called when the banner links out of the application.
            Log.v("load banner left", "onBannerLeftApplication: " + bannerAdView.placementId)
        }
    }

    private fun loadBannerAd(bannerView: BannerView) {
        bannerView.load()
        binding.linearAds.addView(bannerView)
    }

    override fun onInitializationComplete() {

    }

    override fun onInitializationFailed(error: UnityAds.UnityAdsInitializationError?, message: String?) {

    }
}