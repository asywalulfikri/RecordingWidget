package sound.recorder.widget.ui.bottomSheet

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.core.view.WindowCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.greenrobot.eventbus.EventBus
import sound.recorder.widget.databinding.BottomSheetNotesBinding
import sound.recorder.widget.model.Song
import sound.recorder.widget.notes.DatabaseHelper
import sound.recorder.widget.notes.Note
import sound.recorder.widget.notes.NotesAdapter
import sound.recorder.widget.notes.utils.MyDividerItemDecoration
import sound.recorder.widget.notes.utils.RecyclerTouchListener
import sound.recorder.widget.util.DataSession


internal class BottomSheetNote(var showBtnStop: Boolean, private var listener: OnClickListener) : BottomSheetDialogFragment(),SharedPreferences.OnSharedPreferenceChangeListener {


    //Load Song
    private var listTitleSong: ArrayList<String>? = null
    private var listLocationSong: ArrayList<String>? = null
    private var adapter: ArrayAdapter<String>? = null


    // Step 1 - This interface defines the type of messages I want to communicate to my owner
    interface OnClickListener {
        fun onPlaySong(filePath: String)
        fun onStopSong()
    }

    private lateinit var binding : BottomSheetNotesBinding
    private var sharedPreferences : SharedPreferences? =null

    private val notesList: MutableList<Note> = ArrayList()
    private var db: DatabaseHelper? = null
    private var mAdapter: NotesAdapter? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = BottomSheetNotesBinding.inflate(layoutInflater)
        (dialog as? BottomSheetDialog)?.behavior?.state = STATE_EXPANDED
        (dialog as? BottomSheetDialog)?.behavior?.isDraggable = false

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            dialog?.window?.let { WindowCompat.setDecorFitsSystemWindows(it, false) }
        } else {
            @Suppress("DEPRECATION")
            dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        }

        //dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        sharedPreferences = DataSession(activity as Context).getShared()
        sharedPreferences?.registerOnSharedPreferenceChangeListener(this)


        db = DatabaseHelper(activity)
        db?.allNotes?.let { notesList.addAll(it) }


        return binding.root

    }


    private fun toggleEmptyNotes() {
        if (db!!.notesCount > 0) {
            binding.emptyNotesView.visibility = View.GONE
        } else {
            binding.emptyNotesView.visibility = View.VISIBLE
        }
    }

    private fun songNote() {
        mAdapter = NotesAdapter(notesList)
        val mLayoutManager: RecyclerView.LayoutManager = LinearLayoutManager(activity)
        binding.recyclerView.layoutManager = mLayoutManager
        binding.recyclerView.itemAnimator = DefaultItemAnimator()
        context?.let { MyDividerItemDecoration(it, LinearLayoutManager.VERTICAL, 16) }?.let {
            binding.recyclerView.addItemDecoration(
                it
            )
        }
        binding.recyclerView.adapter = mAdapter
        toggleEmptyNotes()
        binding.recyclerView.addOnItemTouchListener(
            RecyclerTouchListener(activity,
                binding.recyclerView, object : RecyclerTouchListener.ClickListener {

                    override fun onClick(view: View?, position: Int) {
                       // showActionsDialog(position)
                    }

                    override fun onLongClick(view: View?, position: Int) {
                        //showActionsDialog(position)
                    }
                })
        )
    }

    private fun getSong(list : ArrayList<Song>){

    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        EventBus.getDefault().unregister(this)
        super.onStop()
    }

   /* @Subscribe(sticky = true, threadMode = ThreadMode.ASYNC)
    fun onMessageEvent(songListResponse: ArrayList<Song>?) {
        songListResponse?.let { getSong(it) }
    }*/

    override fun onResume() {
        super.onResume()
        sharedPreferences?.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        sharedPreferences?.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {

    }
}