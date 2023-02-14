package sound.recorder.widget.ui.fragment

import android.content.ContentUris
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.fragment_recordings_list.*
import kotlinx.android.synthetic.main.fragment_recordings_list.fab_StartRecording
import sound.recorder.widget.R
import sound.recorder.widget.adapter.RecordingsListAdapter
import sound.recorder.widget.db.StorageManager
import sound.recorder.widget.model.Recording
import sound.recorder.widget.util.RecordingEventListener
import sound.recorder.widget.util.ScreenRecorder
import sound.recorder.widget.util.ScreenRecorderMode

class RecordingsListFragment : Fragment(), View.OnClickListener {

   // val screenRecorder: ScreenRecorder by inject()
   var screenRecorder: ScreenRecorder? =null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_recordings_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        screenRecorder = ScreenRecorder(requireActivity().application)
        val adapter = RecordingsListAdapter(::launchVideo)
        rv_RecordingsList.adapter = adapter

        val recordings = StorageManager(requireActivity().application).getRecordings()
        adapter.setData(recordings)

        fab_StartRecording.setOnClickListener(this)

        screenRecorder?.listener = object : RecordingEventListener {
            override fun onRecordingModeChanged(mode: ScreenRecorderMode) {
                setFabState(mode)
            }
        }
    }

    private fun launchVideo(recording: Recording) {
        val intent = Intent().apply {
            action = Intent.ACTION_VIEW
            setDataAndType(
                ContentUris.withAppendedId(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    recording.id
                ), "video/*"
            )
        }
        startActivity(intent)
    }

    fun setFabState(mode: ScreenRecorderMode) {
        var color = R.color.colorPrimary
        var drawable =
            ContextCompat.getDrawable(requireContext(), R.drawable.ic_baseline_videocam_24)
        when (mode) {
            ScreenRecorderMode.RECORDING, ScreenRecorderMode.PAUSED -> {
                color = android.R.color.holo_red_light
                drawable =
                    ContextCompat.getDrawable(requireContext(), R.drawable.ic_baseline_stop_24)
            }
            ScreenRecorderMode.STOPPED -> {
            }
        }

        fab_StartRecording.setImageDrawable(drawable)
        fab_StartRecording.backgroundTintList =
            ContextCompat.getColorStateList(
                requireContext(),
                color
            )
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.fab_StartRecording -> {
                if (screenRecorder?.isPaused == true) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        screenRecorder?.resume()
                    };
                } else if (screenRecorder?.isRecording == true) {
                    screenRecorder?.stop()
                } else {
                    /*val startRecordingIntent = Intent(requireContext(), StartRecordingActivity::class.java)
                    startActivity(startRecordingIntent)*/
                }
            }
        }
    }
}