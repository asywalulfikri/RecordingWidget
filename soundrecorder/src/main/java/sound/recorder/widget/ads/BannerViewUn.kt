package sound.recorder.widget.ads

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import sound.recorder.widget.R

class BannerViewUn : LinearLayout {

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
        val view = inflater?.inflate(R.layout.layout_empty_horizontal, this, true)
        fragmentManager = (context as AppCompatActivity).supportFragmentManager

        view?.let {
            val myFragment = BannerUn()
            val containerViewId = R.id.recordWidgetHorizontal
            fragmentManager?.beginTransaction()?.add(containerViewId, myFragment)?.commitAllowingStateLoss()
        } ?: run {

        }
    }

}