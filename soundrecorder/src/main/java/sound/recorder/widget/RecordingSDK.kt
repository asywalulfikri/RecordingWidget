package sound.recorder.widget

import android.content.Context
import org.greenrobot.eventbus.EventBus
import sound.recorder.widget.model.Song
import sound.recorder.widget.util.DataSession


object RecordingSDK {

    private fun initSdkRecording(ctx: Context,admobId : String,bannerId: String,interstitialId: String) {
        DataSession(ctx).addInitiate(true,admobId,bannerId,interstitialId)
        DataSession(ctx).initiateSong(false)
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

    fun addSong(context: Context,listSong :ArrayList<Song>){
        DataSession(context).initiateSong(true)
        EventBus.getDefault().postSticky(listSong)
    }

    fun isHaveSong(context: Context): Boolean{
        return DataSession(context).isContainSong()
    }

}