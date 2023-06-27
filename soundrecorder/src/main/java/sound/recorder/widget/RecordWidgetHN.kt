package sound.recorder.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import sound.recorder.widget.ui.fragment.VoiceRecorderFragmentWidgetHorizontalNew
import java.lang.Exception

class RecordWidgetHN :  LinearLayout {

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

        try {
            val inflater = LayoutInflater.from(context)
            val view = inflater?.inflate(R.layout.layout_empty_horizontal_new, this, true)
            fragmentManager = (context as AppCompatActivity).supportFragmentManager

            view?.let {
                val myFragment = VoiceRecorderFragmentWidgetHorizontalNew()
                val containerViewId = R.id.recordWidgetHorizontalNew
                fragmentManager?.beginTransaction()?.add(containerViewId, myFragment)?.commitAllowingStateLoss()
            } ?: run {

            }
        } catch (e: Exception) {
            // Handle exception here
            e.printStackTrace()
        } catch (e: IllegalStateException){
            e.printStackTrace()
        } catch (e : IllegalAccessException){
            e.printStackTrace()
        }

    }
}
