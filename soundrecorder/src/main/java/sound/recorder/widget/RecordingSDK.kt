package sound.recorder.widget

import android.app.Activity
import android.content.Context
import android.util.Log
import org.greenrobot.eventbus.EventBus
import sound.recorder.widget.colorpicker.ColorPicker
import sound.recorder.widget.colorpicker.ColorPicker.OnChooseColorListener
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

    fun showDialogColorPicker(context: Context, name : String){
        val colorPicker = ColorPicker(context as Activity)
        val colors: ArrayList<String> = ArrayList()
        colors.add("#82B926")
        colors.add("#a276eb")
        colors.add("#6a3ab2")
        colors.add("#666666")
        colors.add("#FFFF00")
        colors.add("#3C8D2F")
        colors.add("#FA9F00")
        colors.add("#FF0000")

        colorPicker
            //.setDefaultColorButton(Color.parseColor("#2062af"))
            //.setColors(colors)
            .setColumns(5)
            .setRoundColorButton(true)
            .setOnChooseColorListener(object : OnChooseColorListener {
                override fun onChooseColor(position: Int, color: Int) {

                    if(color!=0){
                        DataSession(context).saveColor(color,name)
                    }
                }

                override fun onCancel() {

                }
            })
            .addListenerButton(
                "newButton"
            ) { v, position, color -> {
                Log.d("position", "" + position)
            } }.show()
    }


}