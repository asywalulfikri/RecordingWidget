package sound.recorder.widget.ui.bottomSheet

import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.core.view.WindowCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.bottom_sheet_setting.*
import sound.recorder.widget.RecordingSDK
import sound.recorder.widget.databinding.BottomSheetSettingBinding
import sound.recorder.widget.util.Constant
import sound.recorder.widget.util.DataSession


internal class BottomSheetSetting : BottomSheetDialogFragment(),SharedPreferences.OnSharedPreferenceChangeListener {

    private lateinit var binding : BottomSheetSettingBinding
    private var sharedPreferences : SharedPreferences? =null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = BottomSheetSettingBinding.inflate(layoutInflater)
        (dialog as? BottomSheetDialog)?.behavior?.state = STATE_EXPANDED
        (dialog as? BottomSheetDialog)?.behavior?.isDraggable = false

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            dialog?.window?.let { WindowCompat.setDecorFitsSystemWindows(it, false) }
        } else {
            @Suppress("DEPRECATION")
            dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        }


        sharedPreferences = DataSession(requireContext()).getShared()
        sharedPreferences?.registerOnSharedPreferenceChangeListener(this)

        binding.layoutBackground.setOnClickListener {
            RecordingSDK.showDialogColorPicker(requireContext())
            dismiss()
        }

        binding.btnColor.setBackgroundColor(DataSession(requireContext()).getBackgroundColor())

        binding.cbAnimation.isChecked = DataSession(requireContext()).getAnimation()

        binding.cbAnimation.setOnCheckedChangeListener { _, b ->
            if (b) {
                DataSession(requireContext()).saveAnimation(true)
            } else {
                DataSession(requireContext()).saveAnimation(false)
            }
        }

        binding.rlAnimation.setOnClickListener {
            if(binding.cbAnimation.isChecked){
                cbAnimation.isChecked = false
                DataSession(requireContext()).saveAnimation(false)
            }else{
                cbAnimation.isChecked = true
                DataSession(requireContext()).saveAnimation(true)
            }
        }

        setupSeekBar()
        binding.seekBar.progress = DataSession(requireContext()).getVolume()


        return binding.root

    }

    private fun setupSeekBar(){
        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                DataSession(requireContext()).saveVolume(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {

            }
            override fun onStopTrackingTouch(seekBar: SeekBar) {

            }
        })
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if(key== Constant.keyShared.backgroundColor){
            sharedPreferences?.let {
                it.getInt(Constant.keyShared.backgroundColor,-1)
                    .let { it1 -> binding.btnColor.setBackgroundColor(it1) }
            }
        }

    }

    override fun onResume() {
        super.onResume()
        sharedPreferences?.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        sharedPreferences?.unregisterOnSharedPreferenceChangeListener(this)
    }


}