package sound.recorder.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import sound.recorder.widget.ui.fragment.VoiceRecordFragmentHorizontal

class RecordWidgetH @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private var fragmentManager: FragmentManager? = null

    init {
        init()
    }

    private fun init() {
        try {
            val inflater = LayoutInflater.from(context)
            inflater.inflate(R.layout.layout_empty_horizontal, this, true)
            fragmentManager = (context as AppCompatActivity).supportFragmentManager

            val containerViewId = R.id.recordWidgetHorizontal
            val myFragment = VoiceRecordFragmentHorizontal()

            fragmentManager?.beginTransaction()
                ?.replace(containerViewId, myFragment)
                ?.commitNowAllowingStateLoss()

        } catch (e: Exception) {
            // Handle exception here
            e.printStackTrace()
        }
    }
}
