package sound.recorder.widget.notes

import android.annotation.SuppressLint
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import org.json.JSONObject
import sound.recorder.widget.R
import sound.recorder.widget.notes.NotesAdapter.MyViewHolder
import java.text.ParseException
import java.text.SimpleDateFormat

class NotesAdapter(private val notesList: ArrayList<Note>) :
    RecyclerView.Adapter<MyViewHolder>() {
    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var note: TextView
        var dot: TextView
        var timestamp: TextView
        var title : TextView

        init {
            note = view.findViewById(R.id.note)
            dot = view.findViewById(R.id.dot)
            timestamp = view.findViewById(R.id.timestamp)
            title = view.findViewById(R.id.title)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.note_list_row, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val note = notesList[position]

        try {
            val jsonObject = JSONObject(note.note.toString())
            val value = Gson().fromJson(note.note,Note::class.java)
            // The JSON string is valid
            holder.note.text = value.note
            holder.title.visibility = View.VISIBLE

        } catch (e: Exception) {
            // The JSON string is not valid
            holder.note.text = note.note
            holder.title.visibility = View.GONE
        }

        // Displaying dot from HTML character code
        holder.dot.text = Html.fromHtml("&#8226;")

        // Formatting and displaying timestamp
        holder.timestamp.text = formatDate(note.timestamp)
    }

    override fun getItemCount(): Int {
        return notesList.size
    }

    /**
     * Formatting timestamp to `MMM d` format
     * Input: 2018-02-21 00:15:42
     * Output: Feb 21
     */
    private fun formatDate(dateStr: String): String {
        try {
            @SuppressLint("SimpleDateFormat") val fmt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            val date = fmt.parse(dateStr)
            @SuppressLint("SimpleDateFormat") val fmtOut = SimpleDateFormat("MMM d")
            if (date != null) {
                return fmtOut.format(date)
            }
        } catch (ignored: ParseException) {
        }
        return ""
    }
}