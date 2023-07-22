package sound.recorder.widget.ui.bottomSheet

import android.app.Activity
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
import com.google.firebase.firestore.FirebaseFirestore
import sound.recorder.widget.databinding.BottomSheetNotesBinding
import sound.recorder.widget.model.MyEventBus
import sound.recorder.widget.notes.Note
import sound.recorder.widget.notes.NotesAdapter
import sound.recorder.widget.notes.utils.MyDividerItemDecoration
import sound.recorder.widget.notes.utils.RecyclerTouchListener
import sound.recorder.widget.util.Toastic


class BottomSheetNoteFirebase : BottomSheetDialogFragment {

    private lateinit var binding : BottomSheetNotesBinding

    private val notesList: ArrayList<Note> = ArrayList()
    private var mAdapter: NotesAdapter? = null
    private val db = FirebaseFirestore.getInstance()
    private val collectionPath = "not"

    constructor() : super() {
        // Empty constructor required for DialogFragment
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = BottomSheetNotesBinding.inflate(layoutInflater)
        if(activity!=null&&requireActivity()!=null){
            (dialog as? BottomSheetDialog)?.behavior?.state = STATE_EXPANDED
            (dialog as? BottomSheetDialog)?.behavior?.isDraggable = false

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                dialog?.window?.let { WindowCompat.setDecorFitsSystemWindows(it, false) }
            } else {
                @Suppress("DEPRECATION")
                dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
            }

            fetchDocumentsFromCollection()

            binding.fab.visibility = View.GONE

            binding.ivClose.setOnClickListener {
                dismiss()
            }
        }
        return binding.root

    }

    private fun fetchDocumentsFromCollection() {
        db.collection(collectionPath)
            .get()
            .addOnSuccessListener { querySnapshot ->
                // Process the list of documents here

                for (document in querySnapshot) {
                    if (document.exists()) {
                        val data = document.data

                        val note = Note()
                        note.title = data["title"] as String
                        note.note = data["note"] as String
                        // Add more fields as needed
                        notesList.add(note)

                    }
                }

                songNote()
                // Here, you have the list of documents in 'documentList'
            }
            .addOnFailureListener { exception ->
                Toast.makeText(requireActivity(),exception.message,Toast.LENGTH_SHORT).show()
                // Handle any errors that occurred while retrieving data
            }
    }


    private fun toggleEmptyNotes() {
        if (notesList.size > 0) {
            binding.emptyNotesView.visibility = View.GONE
        } else {
            binding.emptyNotesView.visibility = View.VISIBLE
        }
    }

    private fun songNote() {
        mAdapter = NotesAdapter(notesList)
        val mLayoutManager: RecyclerView.LayoutManager = LinearLayoutManager(requireActivity())
        binding.recyclerView.layoutManager = mLayoutManager
        binding.recyclerView.itemAnimator = DefaultItemAnimator()
        binding.recyclerView.addItemDecoration(
            MyDividerItemDecoration(
                requireActivity(),
                LinearLayoutManager.VERTICAL,
                16
            )
        )
        binding.recyclerView.adapter = mAdapter
        toggleEmptyNotes()
        binding.recyclerView.addOnItemTouchListener(
            RecyclerTouchListener(requireActivity(),
                binding.recyclerView, object : RecyclerTouchListener.ClickListener {
                    override fun onClick(view: View?, position: Int) {
                        showActionsDialog(position)
                    }

                    override fun onLongClick(view: View?, position: Int) {
                        showActionsDialog(position)
                    }
                })
        )
    }

    private fun showActionsDialog(position: Int) {
        notesList[position]
        MyEventBus.postActionCompleted(notesList[position])
        dismiss()
    }


    fun setToastSuccess(activity: Activity?, message : String){
        if(activity!=null){
            Toastic.toastic(
                activity,
                message = message,
                duration = Toastic.LENGTH_SHORT,
                type = Toastic.SUCCESS,
                isIconAnimated = true
            ).show()
        }
    }

    private fun setToastWarning(activity: Activity?, message : String){
        if(activity!=null){
            Toastic.toastic(activity,
                message = message,
                duration = Toastic.LENGTH_SHORT,
                type = Toastic.WARNING,
                isIconAnimated = true
            ).show()
        }

    }
}