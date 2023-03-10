package sound.recorder.widget

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import sound.recorder.widget.databinding.LayoutEmptyHorizontalBinding
import sound.recorder.widget.ui.fragment.VoiceRecorderFragmentWidgetHorizontal

class RecordWidgetH : LinearLayout {

    private var fragmentManagers: FragmentManager? =null
    private val imkasFragment = VoiceRecorderFragmentWidgetHorizontal()
    private var isAdd = false
    private var binding: LayoutEmptyHorizontalBinding


    constructor(_context: Context) : super(_context) {
        fragmentManagers = (_context as FragmentActivity).supportFragmentManager
        binding = LayoutEmptyHorizontalBinding.inflate(LayoutInflater.from(context))
    }

    constructor(_context: Context, attributeSet: AttributeSet?) : super(_context, attributeSet) {
        fragmentManagers = (_context as FragmentActivity).supportFragmentManager
        binding = LayoutEmptyHorizontalBinding.inflate(LayoutInflater.from(context))
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

    @SuppressLint("InflateParams")
    private fun setupViewsAgain(){
        isAdd = true
       /* fragmentTransaction = fragmentManagers?.beginTransaction()
        binding.imcash.id.let { fragmentTransaction?.add(it, imkasFragment) }
        fragmentTransaction?.commit()*/
        fragmentManagers?.beginTransaction()?.replace(binding.recordWidgetHorizontal.id, imkasFragment)?.commit()
        addView(binding.root)
    }

    @SuppressLint("InflateParams")
    private fun setupViews(){
        fragmentManagers?.beginTransaction()?.replace(binding.recordWidgetHorizontal.id, imkasFragment)?.commit()
        if(!imkasFragment.isAdded){
            addView(binding.recordWidgetHorizontal)
            isAdd = true
        }else{
            removeAllViews()
        }

    }

    @SuppressLint("InflateParams")
    private fun resetView(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            fragmentManagers?.beginTransaction()?.detach(imkasFragment)?.commitNow();
            fragmentManagers?.beginTransaction()?.attach(imkasFragment)?.commitNow();
        } else {
            fragmentManagers?.beginTransaction()?.detach(imkasFragment)?.attach(imkasFragment)?.commit();
        }
        addView(binding.recordWidgetHorizontal)
    }

    fun setToast(message : String){
        Toast.makeText(context, "$message.",Toast.LENGTH_SHORT).show()
    }



}