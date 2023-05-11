package sound.recorder.widget

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import org.greenrobot.eventbus.EventBus
import sound.recorder.widget.colorpicker.ColorPicker
import sound.recorder.widget.colorpicker.ColorPicker.OnChooseColorListener
import sound.recorder.widget.model.Song
import sound.recorder.widget.util.Constant
import sound.recorder.widget.util.DataSession


object RecordingSDK {

    private fun initSdkRecording(ctx: Context,admobId : String,bannerId: String,interstitialId: String) {
        DataSession(ctx).addInitiate(true,admobId,bannerId,interstitialId)
        DataSession(ctx).initiateSong(false)
    }

    fun initSdkColor(context: Context,colorWidget : Int,colorRunningText: Int) {
        DataSession(context).addColor(colorWidget,colorRunningText)
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

    fun addInfo(context: Context,versionCode : Int, jsonName : String,rawPath : String,splashScreenColor : String, titleColor : String){
        DataSession(context).setInfoApp(versionCode,jsonName,rawPath,splashScreenColor,titleColor)
    }

    fun isHaveSong(context: Context): Boolean{
        return DataSession(context).isContainSong()
    }


    fun openEmail(context: Context,appName : String,info : String){
        val emailIntent = Intent(Intent.ACTION_SEND)
        emailIntent.type = "text/plain"
        emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf("feedbackmygame@gmail.com"))
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Feedback $appName")
        emailIntent.putExtra(Intent.EXTRA_TEXT, info)
        emailIntent.type = "message/rfc822"

        try {
            context.startActivity(
                Intent.createChooser(
                    emailIntent,
                    "Send email using..."
                )
            )
        } catch (ex: ActivityNotFoundException) {
            Toast.makeText(context,"No email clients installed.",Toast.LENGTH_SHORT).show()
        }
    }

    fun showDialogColorPicker(context: Context){
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
                        DataSession(context).saveColor(color,Constant.keyShared.backgroundColor)
                    }
                }

                override fun onCancel() {

                }
            })
            .addListenerButton(
                "newButton"
            ) { _, position, _ -> {
                Log.d("position", "" + position)
            } }.show()
    }


}