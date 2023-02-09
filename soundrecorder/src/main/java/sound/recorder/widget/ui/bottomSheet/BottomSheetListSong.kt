package sound.recorder.widget.ui.bottomSheet

import android.content.ContentResolver
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.media.MediaPlayer
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.*
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_HIDDEN
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import sound.recorder.widget.RecordingSDK
import sound.recorder.widget.databinding.BottomSheetSongBinding
import sound.recorder.widget.model.Song
import sound.recorder.widget.util.DataSession


internal class BottomSheetListSong(var showBtnStop: Boolean, private var listener: OnClickListener) : BottomSheetDialogFragment(),SharedPreferences.OnSharedPreferenceChangeListener {


    //Load Song
    private var contentResolver: ContentResolver? = null
    var listTitleSong: ArrayList<String>? = null
    private var listLocationSong: ArrayList<String>? = null
    var adapter: ArrayAdapter<String>? = null


    // Step 1 - This interface defines the type of messages I want to communicate to my owner
    interface OnClickListener {
        fun onPlaySong(filePath: String)
        fun onStopSong()
    }

    private var mp : MediaPlayer? =null
    private lateinit var binding : BottomSheetSongBinding
    private var sharedPreferences : SharedPreferences? =null
    private var lisSong = ArrayList<Song>()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = BottomSheetSongBinding.inflate(layoutInflater)
        (dialog as? BottomSheetDialog)?.behavior?.state = STATE_EXPANDED
        (dialog as? BottomSheetDialog)?.behavior?.isDraggable = false
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        sharedPreferences = DataSession(activity as Context).getShared()
        sharedPreferences?.registerOnSharedPreferenceChangeListener(this)

        if(showBtnStop){
            binding.btnStop.visibility = View.VISIBLE
        }else{
            binding.btnStop.visibility = View.GONE
        }

        binding.btnStop.setOnClickListener {
            listener.onStopSong()
            binding.btnStop.visibility = View.GONE
        }

        binding.btnCLose.setOnClickListener {
            dismiss()
        }


        listTitleSong = ArrayList()
        listLocationSong = ArrayList()

        if(!RecordingSDK.isHaveSong(activity as Context)){
            getAllMediaMp3Files(lisSong)
        }

        return binding.root

    }

    private fun setToast(message : String){
        Toast.makeText(activity, "$message.",Toast.LENGTH_SHORT).show()
    }

    private fun setLog(message : String){
        Log.d("value",Gson().toJson(message))
    }


    private fun stopMusic(){
        if (this.mp != null) {
            mp?.release()
        }
    }


    private fun getAllMediaMp3Files(songList : ArrayList<Song>) {
        setLog(songList.size.toString())
        contentResolver = context?.contentResolver
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val cursor = contentResolver?.query(
            uri,
            null,
            null,
            null,
            null
        )
        if (cursor == null) {
            Toast.makeText(activity, "Something Went Wrong.", Toast.LENGTH_LONG).show()
        } else if (!cursor.moveToFirst()) {
            Toast.makeText(activity, "No Music Found on SD Card.", Toast.LENGTH_LONG).show()
        } else {
            val title    = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
            val location = cursor.getColumnIndex(MediaStore.Audio.Media.DATA)


            MainScope().launch {

                var songTitle1 = ""
                var songLocation1 = ""

                withContext(Dispatchers.Default) {

                    //Process Background 2
                    for (i in songList.indices) {
                        songTitle1 = songList[i].title.toString()
                        songLocation1  = songList[i].pathRaw.toString()
                        listLocationSong?.add(songLocation1)
                        listTitleSong?.add(songTitle1)
                    }
                }

                //Result Process Background 2

                MainScope().launch {

                    //Background Process 1
                    withContext(Dispatchers.Default) {

                        do {
                            var songTitle = ""
                            var songLocation = ""
                            if(title==null){
                                //don't execute anything
                            }else{
                                if(cursor.getString(title)!=null){
                                    songTitle = cursor.getString(title)
                                }
                            }

                            if(location==null){
                                //don't execute anything
                            }else{
                                if(cursor.getString(location)!=null){
                                    songLocation = cursor.getString(location)
                                }
                            }

                            listLocationSong?.add(songLocation)
                            listTitleSong?.add(songTitle)

                        } while (cursor.moveToNext())
                    }
                   updateView()
                }

            }

        }

    }


    private fun updateView(){
        val listSong: Array<String> = listTitleSong!!.toTypedArray()
        adapter =
            ArrayAdapter(activity as Context, android.R.layout.simple_list_item_1, listSong)
        binding.listView.adapter = adapter
        adapter?.notifyDataSetChanged()
        binding.listView.onItemClickListener =
            AdapterView.OnItemClickListener { adapterView: AdapterView<*>?, view: View?, i: Int, l: Long ->
                //dismiss()
                (dialog as? BottomSheetDialog)?.behavior?.state = STATE_HIDDEN
                listener.onPlaySong(listLocationSong?.get(i).toString())
                binding.btnStop.visibility = View.VISIBLE

            }
    }


    private fun isDarkTheme(): Boolean {
        return activity?.resources?.configuration?.uiMode!! and
                Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
    }



    private fun showKeyboard(view: View) {
        if (view.requestFocus()) {
            val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
            imm?.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
        }
    }

    private fun hideKeyboard(view: View) {
        val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        imm?.hideSoftInputFromWindow(view.windowToken, 0)
    }


    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        EventBus.getDefault().unregister(this)
        super.onStop()
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.ASYNC)
    fun onMessageEvent(songListResponse: ArrayList<Song>?) {
        Log.d("valueData3",Gson().toJson(songListResponse))
        songListResponse?.let { getAllMediaMp3Files(it) }
    }


    override fun onResume() {
        super.onResume()
        sharedPreferences?.registerOnSharedPreferenceChangeListener(this)
    }


    override fun onPause() {
        super.onPause()
        sharedPreferences?.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        setLog(key.toString())
    }
}