package sound.recorder.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import sound.recorder.widget.ui.fragment.VoiceRecorderFragmentWidgetVertical

class RecordWidgetV : LinearLayout {

    private var fragmentManager: FragmentManager? =null

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        val inflater = LayoutInflater.from(context)
        val view = inflater?.inflate(R.layout.layout_empty_vertical, this, true)
        fragmentManager = (context as AppCompatActivity).supportFragmentManager

        view?.let {
            val myFragment = VoiceRecorderFragmentWidgetVertical()
            val containerViewId = R.id.recordWidgetVertical
            fragmentManager?.beginTransaction()?.add(containerViewId, myFragment)?.commitAllowingStateLoss()
        } ?: run {

        }
    }

    fun setToast(message : String){
        Toast.makeText(context, "$message.",Toast.LENGTH_SHORT).show()
    }
}