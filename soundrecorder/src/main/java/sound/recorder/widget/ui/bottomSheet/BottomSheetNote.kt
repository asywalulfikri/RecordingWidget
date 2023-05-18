package sound.recorder.widget.ui.bottomSheet

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.view.WindowCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import org.json.JSONObject
import sound.recorder.widget.R
import sound.recorder.widget.base.BaseBottomSheet
import sound.recorder.widget.databinding.BottomSheetNotesBinding
import sound.recorder.widget.model.MyEventBus
import sound.recorder.widget.model.MyListener
import sound.recorder.widget.notes.DatabaseHelper
import sound.recorder.widget.notes.Note
import sound.recorder.widget.notes.NotesAdapter
import sound.recorder.widget.notes.utils.MyDividerItemDecoration
import sound.recorder.widget.notes.utils.RecyclerTouchListener


class BottomSheetNote : BaseBottomSheet() {

    private lateinit var binding : BottomSheetNotesBinding

    private val notesList: ArrayList<Note> = ArrayList()
    private var db: DatabaseHelper? = null
    private var mAdapter: NotesAdapter? = null
    var callback: MyListener? = null
    var valueNote : String? =null


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

        songNote()

        binding.fab.setOnClickListener {
            showNoteDialog(false, null, -1)
        }

        binding.ivClose.setOnClickListener {
            dismiss()
        }


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
        db = DatabaseHelper(requireContext())
        notesList.addAll(db!!.allNotes)

        mAdapter = NotesAdapter(notesList)
        val mLayoutManager: RecyclerView.LayoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.layoutManager = mLayoutManager
        binding.recyclerView.itemAnimator = DefaultItemAnimator()
        binding.recyclerView.addItemDecoration(
            MyDividerItemDecoration(
                requireContext(),
                LinearLayoutManager.VERTICAL,
                16
            )
        )
        binding.recyclerView.adapter = mAdapter
        toggleEmptyNotes()
        binding.recyclerView.addOnItemTouchListener(
            RecyclerTouchListener(requireContext(),
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
        val colors = arrayOf<CharSequence>("1. Use Note", "2. Edit Note", "3. Delete Note")
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Choose option")
        builder.setItems(colors) { dialog, which ->
            when (which) {
                0 -> {
                    useNote(notesList[position])
                }
                1 -> {
                    showNoteDialog(true, notesList[position], position)
                }
                else -> {
                    deleteNote(position)
                }
            }
        }
        builder.show()
    }

    private fun deleteNote(position: Int) {
        // deleting the note from db
        db?.deleteNote(notesList[position])

        // removing the note from the list
        notesList.removeAt(position)
        mAdapter?.notifyItemRemoved(position)
        toggleEmptyNotes()
    }

    private fun useNote(note: Note) {
        MyEventBus.postActionCompleted(note)
        dismiss()
    }


    private fun showNoteDialog(shouldUpdate: Boolean, note: Note?, position: Int) {
        val layoutInflaterAndroid = LayoutInflater.from(requireContext())
        @SuppressLint("InflateParams") val view =
            layoutInflaterAndroid.inflate(R.layout.note_dialog, null)
        val alertDialogBuilderUserInput = AlertDialog.Builder(requireContext())
        alertDialogBuilderUserInput.setView(view)
        val inputNote = view.findViewById<EditText>(R.id.note)
        val inputTitle = view.findViewById<EditText>(R.id.title)
        val dialogTitle = view.findViewById<TextView>(R.id.dialog_title)
        dialogTitle.text = if (!shouldUpdate) getString(R.string.lbl_new_note_title) else getString(R.string.lbl_edit_note_title)

        if (shouldUpdate && note != null) {

            try {
                val jsonObject = JSONObject(note.note.toString())
                val value = Gson().fromJson(note.note,Note::class.java)
                // The JSON string is valid
                inputNote.setText(value.note)
                inputTitle.setText(value.title)

            } catch (e: Exception) {
                // The JSON string is not valid
                inputNote.setText(note.note)
                inputTitle.setText(note.title)
            }

        }
        alertDialogBuilderUserInput
            .setCancelable(false)
            .setPositiveButton(
                if (shouldUpdate) "update" else "save"
            ) { dialogBox, id -> }
            .setNegativeButton(
                "cancel"
            ) { dialogBox, id -> dialogBox.cancel() }
        val alertDialog = alertDialogBuilderUserInput.create()
        alertDialog.show()
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(
            View.OnClickListener {
                // Show toast message when no text is entered
                if (TextUtils.isEmpty(inputNote.text.toString())) {
                    setToastWarning("Enter Note!")
                    return@OnClickListener
                }else if (TextUtils.isEmpty(inputNote.text.toString())) {
                    setToastWarning("Enter Note Title!")
                    return@OnClickListener
                }else {
                    alertDialog.dismiss()
                }

                val note1 = Note()
                note1.title = inputTitle.text.toString()
                note1.note = inputNote.text.toString()
                val input = Gson().toJson(note1)

                // check if user updating note
                if (shouldUpdate && note != null) {
                    // update note by it's id
                    updateNote(input,inputTitle.text.toString(), position)
                } else {
                    // create new note
                    createNote(input,inputTitle.text.toString())
                }
            })
    }


    @SuppressLint("NotifyDataSetChanged")
    private fun createNote(note: String,title : String) {
        // inserting note in db and getting
        // newly inserted note id
        val id = db?.insertNote(note,title)

        // get the newly inserted note from db
        val n = db?.getNote(id!!)
        if (n != null) {
            // adding new note to array list at 0 position
            notesList.add(0, n)
            setToastSuccess("Note Success Add")
            // refreshing the list
            mAdapter?.notifyDataSetChanged()
            toggleEmptyNotes()


        }
    }

    private fun updateNote(note: String,title : String, position: Int) {
        val n = notesList[position]
        // updating note text
        n.note = note
        n.title = title

        // updating note in db
        db!!.updateNote(n)

        // refreshing the list
        notesList[position] = n
        mAdapter!!.notifyItemChanged(position)
        toggleEmptyNotes()
    }
}