package sound.recorder.widget

import android.content.ContentResolver
import android.content.Context
import android.content.res.Configuration
import android.media.MediaPlayer
import android.os.Bundle
import android.provider.MediaStore
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


internal class BottomSheetListSong: BottomSheetDialogFragment {


    //Load SOng
    private var contentResolver: ContentResolver? = null
    var listTitleSong: ArrayList<String>? = null
    var listLocationSong: ArrayList<String>? = null
    var adapter: ArrayAdapter<String>? = null
    var showBtnStop = false


    // Step 1 - This interface defines the type of messages I want to communicate to my owner
    interface OnClickListener {
        fun onPlaySong(filePath: String)
        fun onStopSong()
    }

    // Step 2 - This variable represents the listener passed in by the owning object
    // The listener must implement the events interface and passes messages up to the parent.
    private lateinit var listener: OnClickListener
    private var mp : MediaPlayer? =null

    constructor(showBtnStop : Boolean,listener: OnClickListener) {
        this.listener = listener
        this.showBtnStop = showBtnStop
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.bottom_sheet_song, container)

        (dialog as? BottomSheetDialog)?.behavior?.state = STATE_EXPANDED
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)


        //initiate view
        val listView = view.findViewById<ListView>(R.id.listView)
        val btnStop  = view.findViewById<Button>(R.id.stop)


        if(showBtnStop){
            btnStop.visibility = View.VISIBLE
        }else{
            btnStop.visibility = View.GONE
        }

        btnStop.setOnClickListener {
            listener.onStopSong()
            btnStop.visibility = View.GONE
        }


        listTitleSong = ArrayList()
        listLocationSong = ArrayList()

        getAllMediaMp3Files(listView,btnStop)


        // set edittext to filename
        //  editText.setText(filename)

        // showKeyboard(editText)

        // deal with OK button
        /* view.findViewById<Button>(R.id.okBtn).setOnClickListener {
            // hide keyboard
            hideKeyboard(view)

            // update filename if need
            val updatedFilename = editText.text.toString()
            if(updatedFilename != filename){
                val newFile = File("$dirPath$updatedFilename.mp3")
                File(dirPath+filename).renameTo(newFile)
            }

            // add entry to db

            // dismiss dialog
            dismiss()

            // fire ok callback
            listener.onOkClicked("$dirPath$updatedFilename.mp3", updatedFilename)
        }

        // deal with cancel button
        view.findViewById<Button>(R.id.cancelBtn).setOnClickListener {
            // hide keyboard
            hideKeyboard(view)
            // delete file from storage
            File(dirPath+filename).delete()

            // dismiss dialog
            dismiss()

            // fire cancel callback
            listener.onCancelClicked()
        }
*/


        return view

    }

    private fun setToast(message : String){
        Toast.makeText(activity,message,Toast.LENGTH_SHORT).show()
    }


    private fun stopMusic(){
        if (this.mp != null) {
            mp?.release()
        }
    }


    private fun getAllMediaMp3Files(listView: ListView, btnStop : Button) {
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

            do {
                val songTitle: String = cursor.getString(title)
                val songLocation : String = cursor.getString(location)

                // Adding Media File Names to ListElementsArrayList.
                listLocationSong?.add(songLocation)
                listTitleSong?.add(songTitle)
            } while (cursor.moveToNext())
        }

        // converting arraylist to array
        val listSong: Array<String> = listTitleSong!!.toTypedArray()
        adapter =
            ArrayAdapter(activity as Context, android.R.layout.simple_list_item_1, listSong)
        listView.adapter = adapter
        listView.onItemClickListener =
            AdapterView.OnItemClickListener { adapterView: AdapterView<*>?, view: View?, i: Int, l: Long ->
                //dismiss()
                (dialog as? BottomSheetDialog)?.behavior?.state = STATE_HIDDEN
                listener.onPlaySong(listLocationSong?.get(i).toString())
                btnStop.visibility = View.VISIBLE

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

}