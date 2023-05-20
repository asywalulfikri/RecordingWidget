package sound.recorder.widget

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import sound.recorder.widget.databinding.LayoutEmptyHorizontalNewBinding
import sound.recorder.widget.ui.fragment.VoiceRecorderFragmentWidgetHorizontalNew

class RecordWidgetHN : LinearLayout {

    private var fragmentManagers: FragmentManager? =null
    private val imkasFragment = VoiceRecorderFragmentWidgetHorizontalNew()
    private var isAdd = false
    private var binding: LayoutEmptyHorizontalNewBinding


    constructor(_context: Context) : super(_context) {
        fragmentManagers = (_context as FragmentActivity).supportFragmentManager
        binding = LayoutEmptyHorizontalNewBinding.inflate(LayoutInflater.from(context))
    }

    constructor(_context: Context, attributeSet: AttributeSet?) : super(_context, attributeSet) {
        fragmentManagers = (_context as FragmentActivity).supportFragmentManager
        binding = LayoutEmptyHorizontalNewBinding.inflate(LayoutInflater.from(context))
        addView(binding.root)
    }

    fun loadData(){
        if(isAdd){
            removeAllViews()
            resetView()
        }else{
            setupViews()
        }
    }


    private fun setupViews(){
        fragmentManagers?.beginTransaction()?.replace(binding.recordWidgetHorizontalNew.id, imkasFragment)?.commitAllowingStateLoss()
        if(!imkasFragment.isAdded){
            addView(binding.recordWidgetHorizontalNew)
            isAdd = true
        }else{
            removeAllViews()
        }

    }

    private fun resetView(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            fragmentManagers?.beginTransaction()?.detach(imkasFragment)?.commitAllowingStateLoss()
            fragmentManagers?.beginTransaction()?.attach(imkasFragment)?.commitNow();
        } else {
            fragmentManagers?.beginTransaction()?.detach(imkasFragment)?.attach(imkasFragment)?.commitAllowingStateLoss()
        }
        addView(binding.recordWidgetHorizontalNew)
    }

    fun setToast(message : String){
        Toast.makeText(context, "$message.",Toast.LENGTH_SHORT).show()
    }



}