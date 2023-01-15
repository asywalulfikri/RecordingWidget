package sound.recorder.widget

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import sound.recorder.widget.databinding.LayoutEmptyBinding

class WidgetRecord : LinearLayout {

    private var fragmentManagers: FragmentManager? =null
    private val widgetHorizontal = VoiceRecorderFragmentHorizontal()
    private val widgetVertical = VoiceRecorderFragmentVertical()
    private var isAdd = false
    private lateinit var widget : Fragment
    private var binding: LayoutEmptyBinding


    constructor(_context: Context) : super(_context) {
        fragmentManagers = (_context as FragmentActivity).supportFragmentManager
        binding = LayoutEmptyBinding.inflate(LayoutInflater.from(_context))
    }

    constructor(_context: Context, attributeSet: AttributeSet?) : super(_context, attributeSet) {
        fragmentManagers = (_context as FragmentActivity).supportFragmentManager
        binding = LayoutEmptyBinding.inflate(LayoutInflater.from(_context))
        addView(binding.root)
    }

    fun loadData(isHorizontal : Boolean){
        if(isHorizontal){
            widget = widgetHorizontal
        }else{
            widget = widgetVertical
        }
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
        fragmentManagers?.beginTransaction()?.replace(binding.imcash.id, widget)?.commit()
        addView(binding.root)
    }

    @SuppressLint("InflateParams")
    private fun setupViews(){
        fragmentManagers?.beginTransaction()?.replace(binding.imcash.id, widget)?.commit()
        if(!widget.isAdded){
            addView(binding.imcash)
            isAdd = true
        }else{
            removeAllViews()
        }

    }

    @SuppressLint("InflateParams")
    private fun resetView(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            fragmentManagers?.beginTransaction()?.detach(widget)?.commitNow();
            fragmentManagers?.beginTransaction()?.attach(widget)?.commitNow();
        } else {
            fragmentManagers?.beginTransaction()?.detach(widget)?.attach(widget)?.commit();
        }
        addView(binding.imcash)
    }

    fun setToast(message : String){
        Toast.makeText(context, "$message.",Toast.LENGTH_SHORT).show()
    }



}