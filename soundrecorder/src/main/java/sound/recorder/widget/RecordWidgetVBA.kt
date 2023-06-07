package sound.recorder.widget

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import sound.recorder.widget.databinding.LayoutEmptyVerticalBasuriBinding
import sound.recorder.widget.databinding.LayoutEmptyVerticalBlackBinding
import sound.recorder.widget.ui.fragment.VoiceRecorderFragmentWidgetVerticalBasuri
import sound.recorder.widget.ui.fragment.VoiceRecorderFragmentWidgetVerticalBlack

class RecordWidgetVBA : LinearLayout {

    private var fragmentManagers: FragmentManager? =null
    private val imkasFragment = VoiceRecorderFragmentWidgetVerticalBasuri()
    private var isAdd = false
    private var binding: LayoutEmptyVerticalBasuriBinding


    constructor(_context: Context) : super(_context) {
        fragmentManagers = (_context as FragmentActivity).supportFragmentManager
        binding = LayoutEmptyVerticalBasuriBinding.inflate(LayoutInflater.from(context))
    }

    constructor(_context: Context, attributeSet: AttributeSet?) : super(_context, attributeSet) {
        fragmentManagers = (_context as FragmentActivity).supportFragmentManager
        binding = LayoutEmptyVerticalBasuriBinding.inflate(LayoutInflater.from(context))
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

    private fun setupViewsAgain(){
        isAdd = true
        fragmentManagers?.beginTransaction()?.replace(binding.recordWidgetVerticalBasuri.id, imkasFragment)?.commit()
        addView(binding.root)
    }


    private fun setupViews(){
        fragmentManagers?.beginTransaction()?.replace(binding.recordWidgetVerticalBasuri.id, imkasFragment)?.commitAllowingStateLoss()
        if(!imkasFragment.isAdded){
            addView(binding.recordWidgetVerticalBasuri)
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
        addView(binding.recordWidgetVerticalBasuri)
    }

    fun setToast(message : String){
        Toast.makeText(context, "$message.",Toast.LENGTH_SHORT).show()
    }



}