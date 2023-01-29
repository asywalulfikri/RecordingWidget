package sound.recorder.widget

import android.content.Context
import sound.recorder.widget.util.DataSession


object RecordingSDK {

    private fun initSdkRecording(ctx: Context,admobId : String,bannerId: String,interstitialId: String) {
        DataSession(ctx).addInitiate(true,admobId,bannerId,interstitialId)
    }
    fun initSdk(context: Context,admobId: String, bannerId: String, interstitialId: String): RecordingSDK {

        initSdkRecording(
            context,
            admobId,
            bannerId,
            interstitialId
        )
        return this
    }
    fun run(): RecordingSDK {
        return this
    }
}