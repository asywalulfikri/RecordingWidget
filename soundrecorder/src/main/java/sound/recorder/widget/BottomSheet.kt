package sound.recorder.widget

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.textfield.TextInputEditText
import java.io.File


internal class BottomSheet: BottomSheetDialogFragment {

    // Step 1 - This interface defines the type of messages I want to communicate to my owner
    interface OnClickListener {
        // These methods are the different events and
        // need to pass relevant arguments related to the event triggered
        fun onCancelClicked()
        fun onOkClicked(filePath: String, filename: String)
    }

    // Step 2 - This variable represents the listener passed in by the owning object
    // The listener must implement the events interface and passes messages up to the parent.
    private lateinit var listener: OnClickListener

    private lateinit var filename: String
    private lateinit var dirPath: String

    constructor(dirPath: String, filename : String, listener: OnClickListener){
        this.dirPath = dirPath
        this.filename = filename
        this.listener = listener
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.bottom_sheet, container)
        val editText = view.findViewById<TextInputEditText>(R.id.filenameInput)

        (dialog as? BottomSheetDialog)?.behavior?.state = STATE_EXPANDED

        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        if(isDarkTheme()){
            editText.setTextColor(Color.parseColor("#000000"))
        }

        // set edittext to filename
        filename = filename.split(".mp3")[0]
        editText.setText(filename)

       // showKeyboard(editText)

        // deal with OK button
        view.findViewById<Button>(R.id.okBtn).setOnClickListener {
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

        return view

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