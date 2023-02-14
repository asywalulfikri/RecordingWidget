package sound.recorder.widget.adapter


import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_recording.view.*
import sound.recorder.widget.databinding.ItemRecordingBinding
import sound.recorder.widget.model.Recording
import sound.recorder.widget.util.Utils
import kotlin.time.ExperimentalTime


class RecordingsListAdapter(private var onItemClick : (recording: Recording) -> Unit) : RecyclerView.Adapter<RecordingsListAdapter.RecordingItemVH>() {
    private var recordingsList = listOf<Recording>()

    inner class RecordingItemVH(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordingItemVH {
        val recordingItem = ItemRecordingBinding.inflate(LayoutInflater.from(parent.context))
        return RecordingItemVH(recordingItem.root)
    }

    @SuppressLint("SetTextI18n")
    @ExperimentalTime
    override fun onBindViewHolder(holder: RecordingItemVH, position: Int) {
        val recording = recordingsList[position]
        holder.itemView.txt_FileName.text = recording.name
        holder.itemView.txtDate.text = Utils.getFormattedDate(recording.dateAdded)
        holder.itemView.txtDurationSize.text = "${Utils.getFormattedDuration(recording.duration)} (${Utils.getFormattedSize(recording.size)})"

        holder.itemView.setOnClickListener {
            onItemClick(recording)
        }
    }

    override fun getItemCount(): Int {
        return recordingsList.size
    }

    fun setData(recordings: List<Recording>) {
        recordingsList = recordings
        notifyDataSetChanged()
    }

}