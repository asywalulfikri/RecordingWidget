package sound.recorder.widget.ui.bottomSheet

import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.core.view.WindowCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.firestore.FirebaseFirestore
import sound.recorder.widget.adapter.VideoListAdapter
import sound.recorder.widget.base.BaseBottomSheet
import sound.recorder.widget.databinding.ActivitySplashSdkBinding
import sound.recorder.widget.model.Video
import java.util.ArrayList


open class BottomSheetSplashScreen(var firestore: FirebaseFirestore?) : BaseBottomSheet() {

    private var mAdapter: VideoListAdapter? = null
    private var mPage = 1
    private var mVideoList = ArrayList<Video>()

    // Step 1 - This interface defines the type of messages I want to communicate to my owner
    interface OnClickListener {
        fun onPlayVideo(filePath: String)
    }

    private lateinit var binding : ActivitySplashSdkBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = ActivitySplashSdkBinding.inflate(layoutInflater)
        (dialog as? BottomSheetDialog)?.behavior?.state = STATE_EXPANDED
        (dialog as? BottomSheetDialog)?.behavior?.isDraggable = false

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            dialog?.window?.let { WindowCompat.setDecorFitsSystemWindows(it, false) }
        } else {
            @Suppress("DEPRECATION")
            dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        }

        return binding.root

    }

}